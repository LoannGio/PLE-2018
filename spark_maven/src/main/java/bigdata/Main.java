package bigdata;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.ToolRunner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.input.PortableDataStream;
import org.apache.spark.rdd.RDD;
import scala.Tuple2;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {

        ToolRunner.run(HBaseConfiguration.create(), new HBaseCreate(), args);

		/*
		Foreach zoomLevel, number of zoomLevel-1 images to create the new one

		360x180 => 180x90 => 90x45 => 30x15 => 10x5 => 2x1
		*/
        int nbTilesXMax = 360;
        int nbTilesYMax = 180;
        ArrayList<ZoomInfos> zoomInfos = new ArrayList<ZoomInfos>();
        zoomInfos.add(new ZoomInfos(1, nbTilesXMax, nbTilesYMax, 1));
        zoomInfos.add(new ZoomInfos(2, nbTilesXMax / 2, nbTilesYMax / 2, 2));
        zoomInfos.add(new ZoomInfos(3, nbTilesXMax / 4, nbTilesYMax / 4, 2));
        zoomInfos.add(new ZoomInfos(4, nbTilesXMax / 12, nbTilesYMax / 12, 3));
        zoomInfos.add(new ZoomInfos(5, nbTilesXMax / 36, nbTilesYMax / 36, 3));
        zoomInfos.add(new ZoomInfos(6, nbTilesXMax / 180, nbTilesYMax / 180, 5));

        SparkConf conf = new SparkConf().setAppName("Projet Spark");
        JavaSparkContext context = new JavaSparkContext(conf);

        Configuration hbaseConf = HBaseConfiguration.create();
        hbaseConf.set("hbase.zookeeper.quorum", Infos.IP_RIPOUX);
        hbaseConf.set("hbase.mapred.outputtable", new String(Infos.TABLE_NAME));
        hbaseConf.set("mapreduce.outputformat.class", "org.apache.hadoop.hbase.mapreduce.TableOutputFormat");

        /*****************************/

        JavaPairRDD<String, PortableDataStream> files = context.binaryFiles(Infos.HDFS_DIRECTORY_RIPOUX);
        JavaPairRDD<String, Dem3Infos> dem3RDD = files.mapToPair(stringPortableDataStreamTuple2 -> {
            byte[] pixels = stringPortableDataStreamTuple2._2.toArray();
            String filename = stringPortableDataStreamTuple2._1.split("/")[stringPortableDataStreamTuple2._1.split(("/")).length - 1];
            filename = filename.split("\\.")[0];

            Dem3Infos dem3Infos = new Dem3Infos();
            dem3Infos.hgt2dem3infos(pixels, filename, zoomInfos.get(0).ZoomLevel);

            return new Tuple2<>(stringPortableDataStreamTuple2._1, dem3Infos);
        });

        JavaPairRDD<ImmutableBytesWritable, Put> rddToSave = dem3RDD.mapToPair(dem3 -> new Tuple2<ImmutableBytesWritable, Put>(new ImmutableBytesWritable(), dem3._2.getPut()));

        rddToSave.saveAsNewAPIHadoopDataset(hbaseConf);

        /*****************************/

        JavaRDD<MyRDDInfos> myRDD;
        ArrayList<MyRDDInfos> infosToParallelize;

        for(int i = 1; i < zoomInfos.size(); i++){
            infosToParallelize = new ArrayList<MyRDDInfos>();
            for (int x = 0; x < zoomInfos.get(i).NbTilesX; x++) {
                for (int y = 0; y < zoomInfos.get(i).NbTilesY; y++) {
                    infosToParallelize.add(new MyRDDInfos(x,y,zoomInfos.get(i)));
                }
            }
            myRDD = context.parallelize(infosToParallelize);
            JavaPairRDD<Integer, Dem3Infos> dem3RDDbis = myRDD.mapToPair(rddInfos -> {
                Dem3Infos res = new Dem3Infos();
                int ratio = rddInfos.ZoomInfos.RatioToPrevZoom;
                int z = rddInfos.ZoomInfos.ZoomLevel;
                int x = rddInfos.X;
                int y = rddInfos.Y;

                Dem3Infos[] imgToAggregate = new Dem3Infos[ratio * ratio];
                JavaPairRDD<ImmutableBytesWritable, Result> GetResults = context.newAPIHadoopRDD(hbaseConf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
                context.new
                Dem3Infos tmp;
                int tmpx, tmpy;
                String tmp_rowkey;
                int previousZoomLevel = z -1;


                return new Tuple2<Integer, Dem3Infos>(z, res);
            });


            JavaPairRDD<ImmutableBytesWritable, Put> rddToSave2 = dem3RDDbis.mapToPair(dem3 -> new Tuple2<ImmutableBytesWritable, Put>(new ImmutableBytesWritable(), dem3.getPut()));

            rddToSave2.saveAsNewAPIHadoopDataset(hbaseConf);
            JavaPairRDD<ImmutableBytesWritable, Result> GetResults = context.newAPIHadoopRDD(hbaseConf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);

            JavaRDD<Dem3Infos> dem3Results = GetResults.map(tuple -> {
                Dem3Infos tmp = new Dem3Infos();
                if(tuple._2.isEmpty()){
                    //No dem3 found, create water
                    tmp.LatMin = y;
                    tmp.LatMax = y +1;
                    tmp.LongMin = x;
                    tmp.LongMax = x +1;
                    tmp.RowKey = tmp_rowkey;
                    int lenght2dto1d = Infos.DEFAULT_LENGTH * Infos.DEFAULT_LENGTH;
                    String hv = lenght2dto1d + "x" + 0;
                    tmp.HeightValues.add(hv);
                }else{
                    tmp.RowKey = tmp_rowkey;
                    tmp.LatMin = Integer.valueOf(new String(res.getValue(Infos.FAMILY_DEM3, Infos.QUALIFIER_LATMIN)));
                    tmp.LatMax = Integer.valueOf(new String(res.getValue(Infos.FAMILY_DEM3, Infos.QUALIFIER_LATMAX)));
                    tmp.LongMin = Integer.valueOf(new String(res.getValue(Infos.FAMILY_DEM3, Infos.QUALIFIER_LONGMIN)));
                    tmp.LongMax = Integer.valueOf(new String(res.getValue(Infos.FAMILY_DEM3, Infos.QUALIFIER_LONGMAX)));

                    String hv = Bytes.toString(res.getValue(Infos.FAMILY_DEM3, Infos.QUALIFIER_HEIGHTVALUES));

                    for(String s : hv.split(", ")){
                        tmp.HeightValues.add(s);
                    }
                }
            });
        }
        context.close();*/
    }
}

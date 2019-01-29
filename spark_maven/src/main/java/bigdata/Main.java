package bigdata;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.util.ToolRunner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.input.PortableDataStream;



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
        JavaPairRDD<String, PortableDataStream> files = context.binaryFiles(Infos.HDFS_DEM3_RIPOUX);


        /* JOB POUR LE NIVEAU DE ZOOM MAXIMAL */

        files.map(fileData -> {
            byte[] pixels = fileData._2.toArray();
            String filename = fileData._1.split("/")[fileData._1.split(("/")).length - 1];
            filename = filename.split("\\.")[0];
            return MaxZoomCalculator.hgt2dem3infos(pixels, filename, zoomInfos.get(0).ZoomLevel);
        }).foreach(dem3 -> {
            ToolRunner.run(HBaseConfiguration.create(), new HBaseAdd(), dem3.toStrings());

        });

        /*// NE PAS DÉCOMMENTER - TENTATIVE DE REFACTORING DU CODE QUI A ÉCHOUÉ (ON NE SAIT PAS POURQUOI MAIS LE JOB SE RELANCE AU LIEU DE SE FINIR)
        Configuration hbaseConf = HBaseConfiguration.create();
        hbaseConf.set("hbase.zookeeper.quorum", Infos.IP_YOUNG);
        hbaseConf.set("hbase.mapred.outputtable", new String(Infos.TABLE_NAME));
        hbaseConf.set("mapreduce.outputformat.class", "org.apache.hadoop.hbase.mapreduce.TableOutputFormat");

        /*JavaPairRDD<String, PortableDataStream> files = context.binaryFiles(Infos.HDFS_DIRECTORY_YOUNG);
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
        */

        /* JOB POUR LES NIVEAUX DE ZOOM INFÉRIEURS */

        JavaRDD<MyRDDInfos> myRDD;
        ArrayList<MyRDDInfos> infosToParallelize;

        for(int i = 3; i < zoomInfos.size(); i++){
            infosToParallelize = new ArrayList<MyRDDInfos>();
            for (int x = 0; x < zoomInfos.get(i).NbTilesX; x++) {
                for (int y = 0; y < zoomInfos.get(i).NbTilesY; y++) {
                    infosToParallelize.add(new MyRDDInfos(x,y,zoomInfos.get(i)));
                }
            }
            myRDD = context.parallelize(infosToParallelize);
            myRDD.foreach(RDDinfos -> {
                ToolRunner.run(HBaseConfiguration.create(), new GenerateAnyZoomLevel(), RDDinfos.toStrings());
            });
        }

    }
}

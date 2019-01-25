package bigdata;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.util.ToolRunner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.input.PortableDataStream;
import org.apache.spark.rdd.RDD;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {

        //ToolRunner.run(HBaseConfiguration.create(), new HBaseCreate(), args);

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
        /*JavaPairRDD<String, PortableDataStream> files = context.binaryFiles("hdfs://young:9000/user/raw_data/dem3/");


        files.map(fileData -> {
            byte[] pixels = fileData._2.toArray();
            String filename = fileData._1.split("/")[fileData._1.split(("/")).length - 1];
            filename = filename.split("\\.")[0];
            return MaxZoomCalculator.hgt2dem3infos(pixels, filename, zoomInfos.get(0).ZoomLevel);
        }).foreach(dem3 -> {
            ToolRunner.run(HBaseConfiguration.create(), new HBaseAdd(), dem3.toStrings());

        });
*/
        ArrayList<MyRDDInfos> infosToParallelize = new ArrayList<MyRDDInfos>();
        for (int i = 1; i < 2; i++) {
            for (int x = 0; x < zoomInfos.get(i).NbTilesX; x++) {
                for (int y = 0; y < zoomInfos.get(i).NbTilesY; y++) {
                    infosToParallelize.add(new MyRDDInfos(x,y,zoomInfos.get(i)));
                }
            }
        }

        JavaRDD<MyRDDInfos> myRDD = context.parallelize(infosToParallelize);

        myRDD.mapToPair().sortByKey();
        myRDD.foreach();

        myRDD.map(RDDinfos -> AnyZoomCalculator.Hbase2dem3infos(RDDinfos.X, RDDinfos.Y, RDDinfos.ZoomInfos))
                .foreach(dem3 -> {
            ToolRunner.run(HBaseConfiguration.create(), new HBaseAdd(), dem3.toStrings());
        });
        //get.close();
        //add.close();
    }
    //Calculator.Hbase2dem3infos(189/2, 47/2, zoomInfos.get(1));
}

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
        /*JavaPairRDD<String, PortableDataStream> files = context.binaryFiles("hdfs://ripoux:9000/user/raw_data/dem3/");


        files.foreach(fileData -> {
            byte[] pixels = fileData._2.toArray();
            String filename = fileData._1.split("/")[fileData._1.split(("/")).length - 1];
            filename = filename.split("\\.")[0];
            Dem3Infos infos = MaxZoomCalculator.hgt2dem3infos(pixels, filename, zoomInfos.get(0).ZoomLevel);
            ToolRunner.run(HBaseConfiguration.create(), new HBaseAdd(), infos.toStrings());
        });*/


        HBaseGet get = new HBaseGet();
        HBaseAdd add = new HBaseAdd();

        int cpt = 0;
        ArrayList<MyRDDInfos> infosToParallelize = new ArrayList<MyRDDInfos>();
        for (int i = 1; i < zoomInfos.size(); i++) {
            for (int x = 0; x < zoomInfos.get(i).NbTilesX; x++) {
                for (int y = 0; y < zoomInfos.get(i).NbTilesY; y++) {
                    infosToParallelize.add(new MyRDDInfos(x,y,zoomInfos.get(i), get, add));
                    cpt++;
                }
            }
        }
        System.out.println(cpt);

        JavaRDD<MyRDDInfos> myRDD = context.parallelize(infosToParallelize).cache();

        myRDD.foreach(RDDinfos -> {
            Dem3Infos infos = AnyZoomCalculator.Hbase2dem3infos(RDDinfos.X, RDDinfos.Y, RDDinfos.ZoomInfos, RDDinfos.Get);
            ToolRunner.run(HBaseConfiguration.create(), RDDinfos.Add, infos.toStrings());
        });

        //get.close();
        //add.close();
    }
    //Calculator.Hbase2dem3infos(189/2, 47/2, zoomInfos.get(1));
}

package bigdata;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.util.ToolRunner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.input.PortableDataStream;
import org.apache.spark.rdd.JdbcRDD;
import org.apache.spark.rdd.RDD;
import org.apache.hadoop.hbase.TableName;

import org.apache.hadoop.conf.Configuration;

import scala.Tuple2;

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


        files.map(fileData -> {
            byte[] pixels = fileData._2.toArray();
            String filename = fileData._1.split("/")[fileData._1.split(("/")).length - 1];
            filename = filename.split("\\.")[0];
            return MaxZoomCalculator.hgt2dem3infos(pixels, filename, zoomInfos.get(0).ZoomLevel);
        }).foreach(dem3 -> {
            ToolRunner.run(HBaseConfiguration.create(), new HBaseAdd(), dem3.toStrings());

        });
*/
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
            myRDD.foreach(RDDinfos -> {
                ToolRunner.run(HBaseConfiguration.create(), new GenerateAnyZoomLevel(), RDDinfos.toStrings());
            });
        }

/*
        myRDD = context.parallelize(infosToParallelize);
        JavaPairRDD<Integer, MyRDDInfos> sortedRDD = myRDD.mapToPair(RDDinfos -> {
            int key = RDDinfos.ZoomInfos.ZoomLevel;
            return new Tuple2<Integer, MyRDDInfos>(key, RDDinfos);
        });
        sortedRDD.sortByKey();

        sortedRDD.foreach(tuple -> {
            System.out.println("X"+tuple._2.X + "Y"+tuple._2.Y + "Z" + tuple._1);
            ToolRunner.run(HBaseConfiguration.create(), new GenerateAnyZoomLevel(), tuple._2.toStrings());
        });
*/
        /*sortedRDD.foreach(dem3 -> {
            System.out.println(dem3._2.toStrings()[5]);
            ToolRunner.run(HBaseConfiguration.create(), new HBaseAdd(), dem3._2.toStrings());
        });*/

    }
    //Calculator.Hbase2dem3infos(189/2, 47/2, zoomInfos.get(1));
}

package bigdata;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.util.ToolRunner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.input.PortableDataStream;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

import static bigdata.Calculator.hgt2dem3infos;

public class Main {

	public static void main(String[] args) throws Exception {

		//ToolRunner.run(HBaseConfiguration.create(), new HBaseCreate(), args);

		SparkConf conf = new SparkConf().setAppName("Projet Spark");
		JavaSparkContext context = new JavaSparkContext(conf);
		JavaPairRDD<String, PortableDataStream> files= context.binaryFiles("hdfs://ripoux:9000/user/raw_data/dem3/");

		int zoomLevel = 1;
		int nb_tiles_lat, nb_tiles_long;
		while(zoomLevel <= 8){
			nb_tiles_lat = 180 / zoomLevel;
			nb_tiles_long = 360 / zoomLevel;
			int finalZoomLevel = zoomLevel;

			if(zoomLevel == 1){
				files.foreach(fileData -> {
					byte[] pixels = fileData._2.toArray();
					String filename = fileData._1.split("/")[fileData._1.split(("/")).length-1];
					filename = filename.split("\\.")[0];
					Dem3Infos infos = hgt2dem3infos(pixels, filename, finalZoomLevel);
					ToolRunner.run(HBaseConfiguration.create(), new HBaseAdd(), infos.toStrings());
				});
			} else{
				ArrayList<Tuple2> myTuples = new ArrayList<Tuple2>();
				for(int x = 0; x < nb_tiles_long; x++){
					for(int y = 0; y < nb_tiles_lat; y ++){
						Tuple2<Integer, Integer> tuple = new Tuple2<Integer, Integer>(x, y);
						myTuples.add(tuple);
					}
				}
				JavaRDD<Tuple2> tupleRDD = context.parallelize(myTuples);
				tupleRDD.foreach(tuple -> {
					Dem3Infos infos = Calculator.Hbase2dem3infos((int)tuple._1, (int)tuple._2, finalZoomLevel);
					ToolRunner.run(HBaseConfiguration.create(), new HBaseAdd(), infos.toStrings());
				});
			}
			zoomLevel++;
		}
    }
}

package bigdata;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.input.PortableDataStream;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {

		SparkConf conf = new SparkConf().setAppName("Projet Spark");
		JavaSparkContext context = new JavaSparkContext(conf);
		JavaPairRDD<String, PortableDataStream> files= context.binaryFiles("hdfs://young:9000/user/raw_data/dem3/N42E009.hgt");

		files.foreach(v1 -> {
		    byte[] pixels = v1._2.toArray();
		    Calculator.list2png(Calculator.hgt2list(pixels));
        });

		//Calculator.list2png(Calculator.hgt2list("N42E009.hgt"));

	}
	
}

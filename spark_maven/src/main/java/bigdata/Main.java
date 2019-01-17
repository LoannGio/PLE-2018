package bigdata;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {

		//parkConf conf = new SparkConf().setAppName("TP Spark");
		//JavaSparkContext context = new JavaSparkContext(conf);
		Calculator.hgt2file(args[0], args[1]);
		//System.out.println(Calculator.hgt2latlon(args[0], "toto.txt"));

	}
	
}

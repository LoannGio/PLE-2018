package bigdata;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {

		//parkConf conf = new SparkConf().setAppName("TP Spark");
		//JavaSparkContext context = new JavaSparkContext(conf);
		Calculator.file2png(Calculator.hgt2list(args[0]));
		//System.out.println(Calculator.hgt2latlon(args[0], "toto.txt"));

	}
	
}

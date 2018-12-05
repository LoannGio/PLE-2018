package bigdata;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class Main {

	public static void main(String[] args) {
		
		//parkConf conf = new SparkConf().setAppName("TP Spark");
		//JavaSparkContext context = new JavaSparkContext(conf);
		System.out.println(Calculator.hgt2latlon(args[0]));
	}
	
}

package bigdata;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.util.ToolRunner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.input.PortableDataStream;

import java.util.ArrayList;
import java.util.Arrays;

import static bigdata.Calculator.hgt2dem3infos;

public class Main {

	public static void main(String[] args) throws Exception {

		ToolRunner.run(HBaseConfiguration.create(), new HBaseCreate(), args);

		SparkConf conf = new SparkConf().setAppName("Projet Spark");
		JavaSparkContext context = new JavaSparkContext(conf);
		JavaPairRDD<String, PortableDataStream> files= context.binaryFiles("hdfs://ripoux:9000/user/raw_data/dem3/");

		files.foreach(v1 -> {
			byte[] pixels = v1._2.toArray();
		    String filename = v1._1.split("/")[v1._1.split(("/")).length-1];
		    filename = filename.split("\\.")[0];
		    Dem3Infos infos = hgt2dem3infos(pixels, filename);
            ToolRunner.run(HBaseConfiguration.create(), new HBaseAdd(), infos.toStrings());
        });

    }
}

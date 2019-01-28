package bigdata;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;


public class StreamingLambda {

    public static void main(String[] args) throws Exception {
        SparkConf conf = new SparkConf().setAppName("Projet Spark");
        JavaStreamingContext sc = new JavaStreamingContext(conf, new Duration(5000));

        //JavaDStream<String> hdfsDstream = sc.textFileStream("hdfs://ripoux:9000/user/lgiovannange/");
        JavaPairInputDStream<LongWritable, Text> hdfsDstream = sc.fileStream("hdfs://ripoux:9000/user/lgiovannange/", LongWritable.class, Text.class, TextInputFormat.class);

        hdfsDstream.foreachRDD(rdd -> {
            System.out.println("++++++++++++++++++");
            System.out.println("++++++++++++++++++");
            System.out.println("++++++++++++++++++");

        });

        sc.start();
        sc.awaitTermination();
    }
}

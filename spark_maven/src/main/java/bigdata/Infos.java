package bigdata;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;


import java.io.IOException;
import java.io.Serializable;

public class Infos implements Serializable {
    public static final byte[] TABLE_NAME = Bytes.toBytes("lgiovannange2");
    public static final byte[] FAMILY_DEM3 = Bytes.toBytes("dem3");
    public static final byte[] QUALIFIER_HEIGHTVALUES = Bytes.toBytes("heightvalues");
    public static final byte[] QUALIFIER_LATMIN = Bytes.toBytes("latmin");
    public static final byte[] QUALIFIER_LATMAX = Bytes.toBytes("latmax");
    public static final byte[] QUALIFIER_LONGMIN = Bytes.toBytes("longmin");
    public static final byte[] QUALIFIER_LONGMAX = Bytes.toBytes("longmax");
    public static final int DEFAULT_LENGTH = 1201;
    public static final String HDFS_DIRECTORY_YOUNG = "hdfs://young:9000/user/raw_data/dem3/";
    public static final String HDFS_DIRECTORY_RIPOUX = "hdfs://ripoux:9000/user/raw_data/dem3/";
    public static final String IP_YOUNG = "10.0.5.25";
    public static final String IP_RIPOUX = "10.0.213.28";

}
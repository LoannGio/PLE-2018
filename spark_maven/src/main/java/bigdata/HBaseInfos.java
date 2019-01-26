package bigdata;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;


import java.io.IOException;
import java.io.Serializable;

public class HBaseInfos implements Serializable {
    public static final byte[] TABLE_NAME = Bytes.toBytes("lgiovannange");
    public static final byte[] FAMILY_DEM3 = Bytes.toBytes("dem3");
    public static final byte[] QUALIFIER_HEIGHTVALUES = Bytes.toBytes("heightvalues");
    public static final byte[] QUALIFIER_LATMIN = Bytes.toBytes("latmin");
    public static final byte[] QUALIFIER_LATMAX = Bytes.toBytes("latmax");
    public static final byte[] QUALIFIER_LONGMIN = Bytes.toBytes("longmin");
    public static final byte[] QUALIFIER_LONGMAX = Bytes.toBytes("longmax");
    public static final int DEFAULT_LENGTH = 1201;

}

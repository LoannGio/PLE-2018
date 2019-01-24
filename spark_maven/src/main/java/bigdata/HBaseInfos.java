package bigdata;

import org.apache.hadoop.hbase.util.Bytes;

public class HBaseInfos {
    public static final byte[] TABLE_NAME = Bytes.toBytes("lgiovannange");
    public static final byte[] FAMILY_DEM3 = Bytes.toBytes("dem3");
    public static final byte[] QUALIFIER_HEIGHTVALUES = Bytes.toBytes("heightvalues");
    public static final byte[] QUALIFIER_LATMIN = Bytes.toBytes("latmin");
    public static final byte[] QUALIFIER_LATMAX = Bytes.toBytes("latmax");
    public static final byte[] QUALIFIER_LONGMIN = Bytes.toBytes("longmin");
    public static final byte[] QUALIFIER_LONGMAX = Bytes.toBytes("longmax");

}

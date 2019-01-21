package bigdata;

import org.apache.hadoop.hbase.util.Bytes;

public class HBaseInfos {
    public static final byte[] TABLE_NAME = Bytes.toBytes("lgiovannange");
    public static final byte[] FAMILY_INFOS = Bytes.toBytes("infos");
    public static final byte[] FAMILY_DATA = Bytes.toBytes("data");
}

package bigdata;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Tool;

import java.io.Serializable;

public class HBaseAdd extends Configured implements Tool, Serializable {

    @Override
    public int run(String[] infos) throws Exception {
        Connection connection = ConnectionFactory.createConnection(getConf());
        Table table = connection.getTable(TableName.valueOf(HBaseInfos.TABLE_NAME));
        Put put = new Put(Bytes.toBytes(infos[5]));
        put.addColumn(HBaseInfos.FAMILY_DEM3, Bytes.toBytes("latmin"), Bytes.toBytes(infos[0]));
        put.addColumn(HBaseInfos.FAMILY_DEM3, Bytes.toBytes("latmax"), Bytes.toBytes(infos[1]));
        put.addColumn(HBaseInfos.FAMILY_DEM3, Bytes.toBytes("longmin"), Bytes.toBytes(infos[2]));
        put.addColumn(HBaseInfos.FAMILY_DEM3, Bytes.toBytes("longmax"), Bytes.toBytes(infos[3]));
        put.addColumn(HBaseInfos.FAMILY_DEM3, Bytes.toBytes("heightvalues"), Bytes.toBytes(infos[4]));


        table.put(put);
        table.close();
        connection.close();
        return 0;
    }
}

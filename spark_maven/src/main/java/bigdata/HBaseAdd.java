package bigdata;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Tool;

import java.io.IOException;
import java.io.Serializable;

public class HBaseAdd extends Configured implements Tool, Serializable {

   /* private Connection connection;
    private Table table;

    public HBaseAdd(){
        super();
        try {
            connection = ConnectionFactory.createConnection(getConf());
            table = connection.getTable(TableName.valueOf(HBaseInfos.TABLE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public int run(String[] infos) throws Exception {
        Connection connection = ConnectionFactory.createConnection(getConf());
        Table table = connection.getTable(TableName.valueOf(HBaseInfos.TABLE_NAME));
        Put put = new Put(Bytes.toBytes(infos[5]));
        put.addColumn(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LATMIN, Bytes.toBytes(infos[0]));
        put.addColumn(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LATMAX, Bytes.toBytes(infos[1]));
        put.addColumn(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LONGMIN, Bytes.toBytes(infos[2]));
        put.addColumn(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LONGMAX, Bytes.toBytes(infos[3]));
        put.addColumn(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_HEIGHTVALUES, Bytes.toBytes(infos[4]));


        table.put(put);
        table.close();
        connection.close();
        return 0;
    }
/*
    public void close(){
        try {
            table.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}

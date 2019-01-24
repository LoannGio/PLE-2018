package bigdata;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Tool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class HBaseGet extends Configured implements Tool, Serializable {
    public Dem3Infos infos = new Dem3Infos();

    @Override
    public int run(String[] args) throws Exception {
        Connection connection = ConnectionFactory.createConnection(getConf());
        Table table = connection.getTable(TableName.valueOf(HBaseInfos.TABLE_NAME));
        byte[] rowkey = Bytes.toBytes(args[0]);
        Result res = table.get(new Get(rowkey));

        infos.RowKey = args[0];
        infos.LatMin = Integer.valueOf(new String(res.getValue(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LATMIN), "UTF-8"));
        infos.LatMax = Integer.valueOf(new String(res.getValue(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LATMAX), "UTF-8"));
        infos.LongMin = Integer.valueOf(new String(res.getValue(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LONGMIN), "UTF-8"));
        infos.LongMax = Integer.valueOf(new String(res.getValue(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LONGMAX), "UTF-8"));

        String tmp = new String(res.getValue(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_HEIGHTVALUES), "UTF-8");

        for(String s : tmp.split(", ")){
            infos.HeightValues.add(s);
        }
        return 0;
    }

    public Dem3Infos getDem3Agregate(int x, int y, int zoomLevel) throws Exception {
        String[] params = new String[1];
        params[0] = "X"+x+"Y"+y+"Z"+zoomLevel;;
        run(params);
        return infos;
    }
}

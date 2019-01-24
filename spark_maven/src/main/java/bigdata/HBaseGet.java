package bigdata;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

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
        if(res.isEmpty())
            return 0;

        infos.RowKey = args[0];
        infos.LatMin = Integer.valueOf(new String(res.getValue(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LATMIN), "UTF-8"));
        infos.LatMax = Integer.valueOf(new String(res.getValue(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LATMAX), "UTF-8"));
        infos.LongMin = Integer.valueOf(new String(res.getValue(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LONGMIN), "UTF-8"));
        infos.LongMax = Integer.valueOf(new String(res.getValue(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_LONGMAX), "UTF-8"));

        String tmp = Bytes.toString(res.getValue(HBaseInfos.FAMILY_DEM3, HBaseInfos.QUALIFIER_HEIGHTVALUES));

        for(String s : tmp.split(", ")){
            infos.HeightValues.add(s);
        }

        return 1;
    }

    public Dem3Infos getDem3FromHBase(int x, int y, int zoomLevel) throws Exception {
        String[] params = new String[1];
        params[0] = "X"+x+"Y"+y;//+"Z"+zoomLevel;
        int exitStatus =  ToolRunner.run(HBaseConfiguration.create(), this, params);


        if(exitStatus == 0){
            //No dem3 found, create water
            infos.LatMin = y;
            infos.LatMax = y +1;
            infos.LongMin = x;
            infos.LongMax = x +1;
            infos.RowKey = params[0];
            int lenght2dto1d = HBaseInfos.DEFAULT_LENGTH * HBaseInfos.DEFAULT_LENGTH;
            String hv = lenght2dto1d + "x" + 0;
            infos.HeightValues.add(hv);
        }
        /*System.out.println("LOONKING FOR : " + params[0]);
        for (int i = 0 ; i < infos.HeightValues.size(); i++) {
            if(i > 9480 && i < 9500)
                System.out.println(infos.HeightValues.get(i));
        }*/
        return infos;
    }
}

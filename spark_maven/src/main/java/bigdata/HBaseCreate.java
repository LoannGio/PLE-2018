package bigdata;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.io.Serializable;

public class HBaseCreate extends Configured implements Tool, Serializable {

    private static void createOrOverwrite(Admin admin, HTableDescriptor table) throws IOException {
        if (admin.tableExists(table.getTableName())) {
            admin.disableTable(table.getTableName());
            admin.deleteTable(table.getTableName());
        }
        admin.createTable(table);
    }

    private static void createTable(Connection connect) {
        try {
            final Admin admin = connect.getAdmin();
            HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(HBaseInfos.TABLE_NAME));
            HColumnDescriptor famDem3 = new HColumnDescriptor(HBaseInfos.FAMILY_DEM3);
            //famInfos.setInMemory(true);
            tableDescriptor.addFamily(famDem3);
            createOrOverwrite(admin, tableDescriptor);
            admin.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public int run(String[] args) throws IOException {
        Connection connection = ConnectionFactory.createConnection(getConf());
        createTable(connection);
        return 0;
    }
}

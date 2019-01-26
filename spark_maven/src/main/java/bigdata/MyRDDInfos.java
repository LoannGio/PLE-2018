package bigdata;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;

import java.io.Serializable;

public class MyRDDInfos implements Serializable {
    public ZoomInfos ZoomInfos;
    public int X;
    public int Y;

    public MyRDDInfos(){}

    public MyRDDInfos(int x, int y, ZoomInfos zi){
        X = x;
        Y = y;
        ZoomInfos = zi;
    }

    public String[] toStrings(){
        String[] res = new String[6];
        res[0] = ""+X;
        res[1] = ""+Y;
        res[2] = ""+ZoomInfos.ZoomLevel;
        res[3] = ""+ZoomInfos.RatioToPrevZoom;
        res[4] = ""+ZoomInfos.NbTilesX;
        res[5] = ""+ZoomInfos.NbTilesY;
        return res;
    }
}

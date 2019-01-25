package bigdata;

import java.io.Serializable;

public class MyRDDInfos implements Serializable {
    public ZoomInfos ZoomInfos;
    public int X;
    public int Y;
    public HBaseGet Get;

    public MyRDDInfos(){}

    public MyRDDInfos(int x, int y, ZoomInfos zi){
        X = x;
        Y = y;
        ZoomInfos = zi;
    }
}

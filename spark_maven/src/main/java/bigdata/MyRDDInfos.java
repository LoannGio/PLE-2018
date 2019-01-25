package bigdata;

import java.io.Serializable;

public class MyRDDInfos implements Serializable {
    public ZoomInfos ZoomInfos;
    public int X;
    public int Y;
    public HBaseGet Get;
    public HBaseAdd Add;

    public MyRDDInfos(){}

    public MyRDDInfos(int x, int y, ZoomInfos zi, HBaseGet get, HBaseAdd add){
        X = x;
        Y = y;
        ZoomInfos = zi;
        Get = get;
        Add = add;
    }
}

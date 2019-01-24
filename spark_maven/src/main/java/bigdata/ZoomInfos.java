package bigdata;

import java.io.Serializable;

public class ZoomInfos implements Serializable {
    public int ZoomLevel = -1;
    public int NbTilesX = -1;
    public int NbTilesY = -1;
    public int RatioToPrevZoom = -1;

    public ZoomInfos(int zoomLevel, int nbTilesX, int nbTilesY, int ratio) {
        ZoomLevel = zoomLevel;
        NbTilesX = nbTilesX;
        NbTilesY = nbTilesY;
        RatioToPrevZoom = ratio;
    }
}

package bigdata;

import java.util.ArrayList;

public class AnyZoomCalculator extends Calculator{
    public static Dem3Infos Hbase2dem3infos(int x, int y, ZoomInfos zoomInfo){
        Dem3Infos result = new Dem3Infos();
        int ratio = zoomInfo.RatioToPrevZoom;
        int length = HBaseInfos.DEFAULT_LENGTH;
        Dem3Infos[] imgToAggregate = new Dem3Infos[ratio * ratio];
        HBaseGet get = new HBaseGet();
        for(int i = 0 ; i < ratio ; i++) {
            for (int j = 0; j < ratio; j++) {
                try {
                    imgToAggregate[ratio * i + j] = get.getDem3FromHBase(ratio * x + i, ratio * y + j, zoomInfo.ZoomLevel - 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        result.HeightValues = aggregateHeightValues(imgToAggregate, ratio, length);
        int occur, value;
        ArrayList<Integer> ihv = new ArrayList<Integer>();
        for(String s : result.HeightValues){
            occur = Integer.valueOf(s.split("x")[0]);
            value = Integer.valueOf(s.split("x")[1]);
            for(int k = 0 ; k < occur ; k++) {
                ihv.add(value);
            }
        }

        result.LatMin = imgToAggregate[0].LatMin;
        result.LongMin = imgToAggregate[0].LongMin;
        result.LatMax = imgToAggregate[ratio*ratio-1].LatMax;
        result.LongMax = imgToAggregate[ratio*ratio-1].LongMax;
        result.RowKey = "X"+x+"Y"+y+"Z"+zoomInfo.ZoomLevel;

        return result;
    }

    private static ArrayList<String> aggregateHeightValues(Dem3Infos[] imgToAggregate, int ratio, int length){
        int [][][]imgTab = new int[imgToAggregate.length][length][length];
        for(int i = 0 ; i < imgToAggregate.length ; i++) {
            imgTab[i] = HVString2HVInt(imgToAggregate[i].HeightValues);
        }

        int [][]finalImg = new int[length][length];
        for(int i = 0 ; i < length ; i++){
            for(int j = 0 ; j < length ; j++){
                finalImg[i][j] = -1;
            }
        }

        for(int i = 0 ; i < length ; i++){
            for(int j = 0 ; j < length ; j++){
                finalImg[i][j] = getHVMean(i, j, imgTab, ratio, length);
            }
        }

        ArrayList<Integer> aggregatedImg = new ArrayList<Integer>();
        for(int i = 0 ; i < length ; i++) {
            for(int j = 0 ; j < length ; j++) {
                aggregatedImg.add(finalImg[i][j]);
            }
        }
        return HeightValues2ConcatenatedStringList(aggregatedImg);

    }

    private static int[][] HVString2HVInt(ArrayList<String> str_hv) {
        int i = 0;
        int j = 0;
        int occur=-1;
        int value = -1;

        int [][]hv = new int[HBaseInfos.DEFAULT_LENGTH][HBaseInfos.DEFAULT_LENGTH];
        for(int l = 0 ; l < str_hv.size() ; l++){
            String s = str_hv.get(l);
            occur = Integer.valueOf(s.split("x")[0]);
            value = Integer.valueOf(s.split("x")[1]);
            for(int k = 0 ; k < occur ; k++) {
                hv[i][j] = value;
                if(i < HBaseInfos.DEFAULT_LENGTH - 1) {
                    i++;
                }
                else {
                    i = 0;
                    j++;
                }
            }
        }
        return hv;
    }

    private static int getHVMean(int i, int j, int[][][] imgTab, int ratio, int length) {
        int x = ratio*i;
        int y = ratio*j;
        int limit = (length+1)/ratio;
        int indexImageX;
        int indexImageY;
        int indexImage;
        int sum = 0;
        int cpt_err = 0;
        for(int k = 0 ; k < ratio ; k++){
            for(int l = 0 ; l < ratio ; l++){
                indexImageX = (x+k)/(limit*ratio);
                indexImageY = (y+l)/(limit*ratio);
                if(((x + k) % length) - ((x + k - 1) % length) < 0) //BAND-AID FIX TEST
                    indexImageX++;
                if(((y + l) % length) - ((y + l - 1) % length) < 0) //BAND-AID FIX TEST
                    indexImageY++;
                indexImage = indexImageX + ratio*indexImageY;
                if(x+k < ratio*length && y+l < ratio*length && indexImage < ratio*ratio) {
                    sum += imgTab[indexImage][(y + l) % length][(x + k) % length];
                }
                else
                    cpt_err++;
            }
        }

        if(cpt_err == ratio*ratio)
            return 0;
        return sum/((ratio*ratio)-cpt_err);
    }
}

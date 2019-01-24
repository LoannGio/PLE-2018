package bigdata;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Calculator {
    private static final int MAX_HEIGHT = 8000;

    public static Dem3Infos hgt2dem3infos(byte[] buffer, String filename, int zoomLevel){
        Dem3Infos result;
        int latmin, latmax, longmin, longmax;
        ArrayList<Integer> heightValues = new ArrayList<Integer>();

        int length = HBaseInfos.DEFAULT_LENGTH;
        int[][] height = new int[length][length];

        int lat = Integer.valueOf(filename.substring(1, 3));
        int lng = Integer.valueOf(filename.substring(4,7));

        if(filename.toLowerCase().charAt(0) == 's')
            lat *= -1;
        if(filename.toLowerCase().charAt(3) == 'w')
            lng *= -1;

        int buffer_index = 0;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if(buffer_index < buffer.length - 1){
                    height[i][j] = (int) ((buffer[buffer_index] & 0xFF) << 8 | (buffer[buffer_index+1] & 0xFF));
                    buffer_index += 2;

                }
            }
        }
        heightValues = correct(height, length);
        heightValues = generateValues(heightValues, length);

        latmin = lat;
        latmax = lat +1;
        longmin = lng;
        longmax = lng + 1;
        ArrayList<String> str_heightValues = HeightValues2ConcatenatedStringList(heightValues);
        int trueLatMin = Math.abs(latmin - 89);
        int trueLongMin = longmin + 180;
        String rowkey = "X" + trueLongMin +  "Y" + trueLatMin + "Z" + zoomLevel;


        result = new Dem3Infos(latmin, latmax, longmin, longmax, str_heightValues, rowkey);
        return result;
    }

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
                    /*imgToAggregate[0] = get.getDem3FromHBase(2*x, 2*y, zoomLevel-1);
                    imgToAggregate[1] = get.getDem3FromHBase(2*x+1, 2*y, zoomLevel-1);
                    imgToAggregate[2] = get.getDem3FromHBase(2*x, 2*y+1, zoomLevel-1);
                    imgToAggregate[3] = get.getDem3FromHBase(2*x+1, 2*y+1, zoomLevel-1);*/
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
        list2png(ihv, ratio*length);

        result.LatMin = imgToAggregate[0].LatMin;
        result.LongMin = imgToAggregate[0].LongMin;
        result.LatMax = imgToAggregate[ratio*ratio-1].LatMax;
        result.LongMax = imgToAggregate[ratio*ratio-1].LongMax;
        result.RowKey = "X"+x+"Y"+y+"Z"+zoomInfo.ZoomLevel;

        return result;
    }

    /*private static ArrayList<String> aggregateHeightValues(Dem3Infos[] imgToAggregate, int ratio, int length) {
        int [][][]imgTab = new int[imgToAggregate.length][length][length];
        for(int i = 0 ; i < imgToAggregate.length ; i++) {
            imgTab[i] = HVString2HVInt(imgToAggregate[i].HeightValues);
        }

        int [][]finalImg = new int[ratio*length][ratio*length];

        for(int k = 0 ; k < ratio ; k++) {
            for(int l = 0 ; l < ratio ; l++) {
                for (int i = 0; i < length ; i++) {
                    for (int j = 0; j < length ; j++) {
                        finalImg[k * length + j][l * length + i] = imgTab[k+ratio*l][i][j];
                    }
                }
            }
        }



        ArrayList<Integer> aggregatedImg = new ArrayList<Integer>();
        for(int i = 0 ; i < ratio*length ; i++) {
            for(int j = 0 ; j < ratio*length ; j++) {
                aggregatedImg.add(finalImg[i][j]);
            }
        }
        return HeightValues2ConcatenatedStringList(aggregatedImg);

    }*/

    private static ArrayList<String> aggregateHeightValues(Dem3Infos[] imgToAggregate, int ratio, int length){
        int [][][]imgTab = new int[imgToAggregate.length][length][length];
        for(int i = 0 ; i < imgToAggregate.length ; i++) {
            imgTab[i] = HVString2HVInt(imgToAggregate[i].HeightValues);
        }

        int [][]finalImg = new int[length][length];

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
                indexImageX = (x+k)/limit;
                indexImageY = (y+l)/limit;
                indexImage = indexImageX + ratio*indexImageY;
                System.out.println(indexImageX + " " + indexImageY);
                System.out.println("index : " + indexImage + " - x : " + (x+k) + " - y : " + (y+l));
                if(x+k < ratio*length && y+l < ratio*length && indexImage < ratio*ratio)
                    sum += imgTab[indexImage][(x+k)%length][(y+l)%length];
                else
                    cpt_err++;
            }
        }

        return sum/((ratio*ratio)-cpt_err);
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


    private static ArrayList<Integer> generateValues(ArrayList<Integer> heightLists, int length){
        ArrayList<Integer> heightValues = new ArrayList<Integer>();

        double heightValue;
        int i;
        for(i = 0; i < heightLists.size(); i++){
            heightValue = (heightLists.get(i) * 255.0) / MAX_HEIGHT;
            if(heightValue > 255)
                heightValue = 255;
            else if (heightValue > 0 && heightValue < 1)
                heightValue = 1;
            heightValues.add((int)heightValue);
        }

        int length2d = length*length;
        if(i < length2d){
            int toFill = length2d - i;
            for(i = 0; i < toFill; i++){
                heightValues.add(0);
            }
        }

        return heightValues;
    }

    private static ArrayList<Integer> correct(int [][] height, int length) {
        ArrayList<Integer> heightslist = new ArrayList<Integer>();

        for (int i = 0; i <length; i++) {
            for (int j = 0; j < length; j++) {
                heightslist.add(height[i][j]);
            }
        }

        int nb_checkers = 150;
        for(int i = 0; i < heightslist.size(); i++){
            if(heightslist.get(i) > 9000){
                int sum = 0;
                int cpt = 0;
                for(int p = (i-nb_checkers >= 0)?(i-nb_checkers):0; p < ((i+nb_checkers < heightslist.size())?(i+nb_checkers):(heightslist.size())); p++){
                    if(heightslist.get(p) <= 9000){
                        sum += heightslist.get(p);
                        cpt++;
                    }
                }
                if(cpt != 0)
                    heightslist.set(i, sum/cpt); //ici
            }
        }
        return heightslist;
    }


    public static ArrayList<String> HeightValues2ConcatenatedStringList(ArrayList<Integer> heightValues){
        ArrayList<String> res = new ArrayList<String>();
        int cValue = -1;
        int cpt = 0;
        int tmpValue;
        String str_out;

        for(int i = 0; i < heightValues.size(); i++){
            tmpValue = heightValues.get(i);
            if(cValue == -1){
                cValue = tmpValue;
                cpt++;
            }else if(cValue == tmpValue){
                cpt++;
            }else if(cValue != tmpValue){
                str_out = cpt + "x" + cValue;
                res.add(str_out);
                cValue = tmpValue;
                cpt = 1;
            }
        }
        if(cpt > 0){
            str_out = cpt + "x" + cValue;
            res.add(str_out);
        }
        return res;
    }

    public static void list2png(ArrayList<Integer> heightValues, int length) {

        // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
        // into integer pixels
        BufferedImage bi = new BufferedImage(length, length, BufferedImage.TYPE_INT_ARGB);

        int currentImgI = 0;
        int currentImgJ = 0;
        int r = 255;
        int g = 255;
        int b = 255;
        int color;

        for(int heightValue : heightValues){
            if(heightValue == 0){
                g = 255;
                r = 200;
                b = 255;
            } else if((heightValue > 0) && (heightValue <= 64)) {
                r = heightValue * 3;
                g = 64 + heightValue*2;
                b = 0;
            } else if(heightValue > 64 && heightValue <= 128) {
                r = 192 - (heightValue - 64);
                g = 192 - (heightValue-64) * 2;
                b = 0;
            }else if(heightValue > 128 && heightValue <= 239) {
                r = heightValue;
                g = 63 + (int)((heightValue-128) * 1.5);
                b = 0;
            }else if(heightValue > 239) {
                r = 255;
                g = 255;
                b = heightValue;
            }
            color = (255<<24) | (r<<16) | (g<<8) | b;
            bi.setRGB(currentImgJ, currentImgI, color);
            if(currentImgJ < length-1){
                currentImgJ++;
            } else{
                currentImgI++;
                currentImgJ = 0;
            }
        }
        try{
            File f = new File("out5.png");
            ImageIO.write(bi, "png", f);
        }catch(IOException e){
            System.out.println(e);
        }
    }

}

package bigdata;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Dem3Infos implements Serializable, Cloneable {
    public ArrayList<String> HeightValues = new ArrayList<String>();
    public int LatMin, LatMax, LongMin, LongMax;
    public String RowKey;

    public Dem3Infos(int latmin, int latmax, int longmin, int longmax, ArrayList<String> hvs, String balboa){
        LatMin = latmin;
        LatMax = latmax;
        LongMin = longmin;
        LongMax = longmax;
        HeightValues = hvs;
        RowKey = balboa;

    }

    @Override
    public Dem3Infos clone(){
        Dem3Infos newElem = null;
        try {
            newElem = (Dem3Infos) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return newElem;
    }

    public Dem3Infos(){}

    public String[] toStrings(){
        String[] result = new String[6];
        result[0] = Integer.toString(LatMin);
        result[1] = Integer.toString(LatMax);
        result[2] = Integer.toString(LongMin);
        result[3] = Integer.toString(LongMax);
        String str_heightvalues = HeightValues.toString().substring(1, HeightValues.toString().length() -1);
        result[4] = str_heightvalues;
        result[5] = RowKey;

        return result;
    }

    public Put getPut(){
        Put put = new Put(Bytes.toBytes(RowKey));
        put.addColumn(Infos.FAMILY_DEM3, Infos.QUALIFIER_LATMIN, Bytes.toBytes(LatMin));
        put.addColumn(Infos.FAMILY_DEM3, Infos.QUALIFIER_LATMAX, Bytes.toBytes(LatMax));
        put.addColumn(Infos.FAMILY_DEM3, Infos.QUALIFIER_LONGMIN, Bytes.toBytes(LongMin));
        put.addColumn(Infos.FAMILY_DEM3, Infos.QUALIFIER_LONGMAX, Bytes.toBytes(LongMax));
        String str_heightvalues = HeightValues.toString().substring(1, HeightValues.toString().length() -1);
        put.addColumn(Infos.FAMILY_DEM3, Infos.QUALIFIER_HEIGHTVALUES, Bytes.toBytes(str_heightvalues));
        System.out.println(RowKey);
        return put;
    }

    private static final int MAX_HEIGHT = 8000;

    public void hgt2dem3infos(byte[] buffer, String filename, int zoomLevel){
        int latmin, latmax, longmin, longmax;
        ArrayList<Integer> heightValues = new ArrayList<Integer>();

        int length = Infos.DEFAULT_LENGTH;
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
        ArrayList<String> str_heightValues = Calculator.HeightValues2ConcatenatedStringList(heightValues);
        int trueLatMin = Math.abs(latmin - 89);
        int trueLongMin = longmin + 180;
        String rowkey = "X" + trueLongMin +  "Y" + trueLatMin + "Z" + zoomLevel;

        LatMin = latmin;
        LatMax = latmax;
        LongMin = longmin;
        LongMax = longmax;
        HeightValues = str_heightValues;
        RowKey = rowkey;
    }

    private  ArrayList<Integer> generateValues(ArrayList<Integer> heightLists, int length){
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

    private  ArrayList<Integer> correct(int [][] height, int length) {
        ArrayList<Integer> heightslist = new ArrayList<Integer>();

        for (int i = 0; i <length; i++) {
            for (int j = 0; j < length; j++) {
                heightslist.add(height[i][j]);
            }
        }

        int nb_checkers = 150;
        for(int i = 0; i < heightslist.size(); i++){
            if(heightslist.get(i) > 9000 || heightslist.get(i) < 0){
                int sum = 0;
                int cpt = 0;
                for(int p = (i-nb_checkers >= 0)?(i-nb_checkers):0; p < ((i+nb_checkers < heightslist.size())?(i+nb_checkers):(heightslist.size())); p++){
                    if(heightslist.get(p) <= 9000 && heightslist.get(p) >= 0){
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
}

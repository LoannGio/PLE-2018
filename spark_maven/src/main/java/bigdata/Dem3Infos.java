package bigdata;

import java.io.File;
import java.util.ArrayList;

public class Dem3Infos {
    public ArrayList<String> HeightValues;
    public int LatMin, LatMax, LongMin, LongMax;
    public String Filename, RowKey;

    public Dem3Infos(int latmin, int latmax, int longmin, int longmax, ArrayList<String> hvs, String filename, String balboa){
        LatMin = latmin;
        LatMax = latmax;
        LongMin = longmin;
        LongMax = longmax;
        HeightValues = hvs;
        Filename = filename;
        RowKey = balboa;

    }

    public Dem3Infos(){}

    public String[] toStrings(){
        String[] result = new String[7];
        result[0] = Integer.toString(LatMin);
        result[1] = Integer.toString(LatMax);
        result[2] = Integer.toString(LongMin);
        result[3] = Integer.toString(LongMax);
        String str_heightvalues = HeightValues.toString().substring(1, HeightValues.toString().length() -1);
        result[4] = str_heightvalues;
        result[5] = Filename;
        result[6] = RowKey;

        return result;
    }
}

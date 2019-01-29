package bigdata;

import java.util.ArrayList;

public class MaxZoomCalculator extends Calculator{
    private static final int MAX_HEIGHT = 8000;

    public static Dem3Infos hgt2dem3infos(byte[] buffer, String filename, int zoomLevel){
        Dem3Infos result;
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
        ArrayList<String> str_heightValues = HeightValues2ConcatenatedStringList(heightValues);
        int trueLatMin = Math.abs(latmin - 89);
        int trueLongMin = longmin + 180;
        String rowkey = "X" + trueLongMin +  "Y" + trueLatMin + "Z" + zoomLevel;


        result = new Dem3Infos(latmin, latmax, longmin, longmax, str_heightValues, rowkey);
        return result;
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

package bigdata;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.Serializable;
import java.util.ArrayList;

public class GenerateAnyZoomLevel extends Configured implements Tool, Serializable {
    public Dem3Infos Dem3;

    @Override
    public int run(String[] args) throws Exception {
        int ratio = Integer.valueOf(args[3]);
        int z = Integer.valueOf(args[2]);
        int x = Integer.valueOf(args[0]);
        int y = Integer.valueOf(args[1]);

        Dem3Infos result = new Dem3Infos();

        Dem3Infos[] imgToAggregate = new Dem3Infos[ratio * ratio];
        Dem3Infos tmp;
        int tmpx, tmpy;
        String tmp_rowkey;
        int previousZoomLevel = z -1;
        Connection connection = ConnectionFactory.createConnection(getConf());
        Table table = connection.getTable(TableName.valueOf(Infos.TABLE_NAME));
        for(int i = 0 ; i < ratio ; i++) {
            tmpx = ratio * x +i;
            for (int j = 0; j < ratio; j++) {
                try {
                    tmpy = ratio*y+j;
                    tmp_rowkey = "X"+tmpx+"Y"+tmpy+"Z"+previousZoomLevel;
                    byte[] rowkey = Bytes.toBytes(tmp_rowkey);
                    Result res = table.get(new Get(rowkey));
                    tmp = new Dem3Infos();
                    if(res.isEmpty()){
                        //No dem3 found, create water
                        tmp.LatMin = y;
                        tmp.LatMax = y +1;
                        tmp.LongMin = x;
                        tmp.LongMax = x +1;
                        tmp.RowKey = tmp_rowkey;
                        int lenght2dto1d = Infos.DEFAULT_LENGTH * Infos.DEFAULT_LENGTH;
                        String hv = lenght2dto1d + "x" + 0;
                        tmp.HeightValues.add(hv);
                    }else{
                        tmp.RowKey = tmp_rowkey;
                        tmp.LatMin = Integer.valueOf(new String(res.getValue(Infos.FAMILY_DEM3, Infos.QUALIFIER_LATMIN)));
                        tmp.LatMax = Integer.valueOf(new String(res.getValue(Infos.FAMILY_DEM3, Infos.QUALIFIER_LATMAX)));
                        tmp.LongMin = Integer.valueOf(new String(res.getValue(Infos.FAMILY_DEM3, Infos.QUALIFIER_LONGMIN)));
                        tmp.LongMax = Integer.valueOf(new String(res.getValue(Infos.FAMILY_DEM3, Infos.QUALIFIER_LONGMAX)));

                        String hv = Bytes.toString(res.getValue(Infos.FAMILY_DEM3, Infos.QUALIFIER_HEIGHTVALUES));

                        for(String s : hv.split(", ")){
                            tmp.HeightValues.add(s);
                        }
                    }
                    imgToAggregate[ratio * i + j] = tmp;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        table.close();
        connection.close();

        result.HeightValues = aggregateHeightValues(imgToAggregate, ratio, Infos.DEFAULT_LENGTH);
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
        result.RowKey = "X"+x+"Y"+y+"Z"+z;

        Dem3 = result;
        //return ToolRunner.run(HBaseConfiguration.create(), new HBaseAdd(), result.toStrings());
        return 1;
    }

    private ArrayList<String> aggregateHeightValues(Dem3Infos[] imgToAggregate, int ratio, int length){
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
        return Calculator.HeightValues2ConcatenatedStringList(aggregatedImg);

    }

    private int[][] HVString2HVInt(ArrayList<String> str_hv) {
        int i = 0;
        int j = 0;
        int occur=-1;
        int value = -1;

        int [][]hv = new int[Infos.DEFAULT_LENGTH][Infos.DEFAULT_LENGTH];
        for(int l = 0 ; l < str_hv.size() ; l++){
            String s = str_hv.get(l);
            occur = Integer.valueOf(s.split("x")[0]);
            value = Integer.valueOf(s.split("x")[1]);
            for(int k = 0 ; k < occur ; k++) {
                hv[i][j] = value;
                if(i < Infos.DEFAULT_LENGTH - 1) {
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

    private int getHVMean(int i, int j, int[][][] imgTab, int ratio, int length) {
        int x = ratio*i;
        int y = ratio*j;
        int limit;// = (length+1)/ratio;
        int indexImageX;
        int indexImageY;
        int indexImage;
        int sum = 0;
        //int cpt_err = 0;
        for(int k = 0 ; k < ratio ; k++){
            for(int l = 0 ; l < ratio ; l++){
                indexImageX = (x+k)/(length);
                indexImageY = (y+l)/(length);
                /*if(((x + k) % length) - ((x + k - 1) % length) < 0) //BAND-AID FIX TEST
                    indexImageX++;
                if(((y + l) % length) - ((y + l - 1) % length) < 0) //BAND-AID FIX TEST
                    indexImageY++;*/
                indexImage = indexImageX + ratio*indexImageY;
                sum += imgTab[indexImage][(y+l)%length][(x+k)%length];
                /*if(x+k < ratio*length && y+l < ratio*length && indexImage < ratio*ratio) {
                    sum += imgTab[indexImage][(y + l) % length][(x + k) % length];
                }
                else
                    cpt_err++;*/
            }
        }

        return sum/(ratio*ratio);
        /*if(cpt_err == ratio*ratio)
            return 0;
        return sum/((ratio*ratio)-cpt_err);*/
    }
}

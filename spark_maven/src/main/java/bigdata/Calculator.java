package bigdata;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

abstract public class Calculator implements Serializable{

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

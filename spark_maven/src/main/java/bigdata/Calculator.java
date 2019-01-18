package bigdata;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class Calculator {

	public static ArrayList<Integer> hgt2list(byte[] buffer){
		int MAX_HEIGHT = 8000;
		ArrayList<Integer> heightValues = new ArrayList<Integer>();

		int length = 1201;
		int[][] height = new int[length][length];


		double heightValue;
		int buffer_index = 0;

		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				if(buffer_index < buffer.length - 1){
					height[i][j] = (int) ((buffer[buffer_index] & 0xFF) << 8 | (buffer[buffer_index+1] & 0xFF));
					buffer_index += 2;
					heightValue = (height[i][j] * 255.0) / MAX_HEIGHT;
					if(heightValue > 255)
						heightValue = 255;
					else if (heightValue > 0 && heightValue < 1)
						heightValue = 1;
					heightValues.add((int)heightValue);
				}
			}
		}
		return heightValues;
	}


	public static void list2png(ArrayList<Integer> heightValues) {
		int length = 1201;

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
				g = 0;
				r = 0;
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
			File f = new File("out.png");
			ImageIO.write(bi, "png", f);
		}catch(IOException e){
			System.out.println(e);
		}
	}
}

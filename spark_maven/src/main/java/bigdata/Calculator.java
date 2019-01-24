package bigdata;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class Calculator {
	private static final int MAX_HEIGHT = 8000;
	private static final int DEFAULT_LENGTH = 1201;

	public static Dem3Infos hgt2dem3infos(byte[] buffer, String filename, int zoomLevel){
		Dem3Infos result;
		int latmin, latmax, longmin, longmax;
		ArrayList<Integer> heightValues = new ArrayList<Integer>();

		int length = DEFAULT_LENGTH * zoomLevel;
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

	public static Dem3Infos Hbase2dem3infos(int x, int y, int zoomLevel){
		Dem3Infos result;
		int length = DEFAULT_LENGTH * zoomLevel;
		Dem3Infos[] imgToAgregate = new Dem3Infos[4];
		HBaseGet get = new HBaseGet();
		/*
		0 : top left
		1 : top right
		2 : bot left
		3 : bot right
		* */
		try{
			imgToAgregate[0] = get.getDem3Agregate(2*x, 2*y, zoomLevel-1);
			imgToAgregate[1] = get.getDem3Agregate(2*x+1, 2*y, zoomLevel-1);
			imgToAgregate[2] = get.getDem3Agregate(2*x, 2*y+1, zoomLevel-1);
			imgToAgregate[3] = get.getDem3Agregate(2*x+1, 2*y+1, zoomLevel-1);
		}catch(Exception e){
			e.printStackTrace();
		}

		return null;
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

	public static void list2png(ArrayList<Integer> heightValues) {
		int length = DEFAULT_LENGTH; //ZOOM LEVEL

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
			File f = new File("out1.png");
			ImageIO.write(bi, "png", f);
		}catch(IOException e){
			System.out.println(e);
		}
	}

}

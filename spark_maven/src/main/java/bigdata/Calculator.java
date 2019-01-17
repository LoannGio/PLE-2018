package bigdata;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Calculator {

	public static String hgt2file(String filepath, String outputFile){
		ArrayList<Integer> bounds = new ArrayList<Integer>();
		bounds.add(0);
		bounds.add(8000);
		bounds.add(2000);
		bounds.add(1000);
		bounds.add(4000);
		HeightLevelFinder hlf = new HeightLevelFinder(bounds);

		int length = 1201;
		int[][] height = new int[length][length];

		BufferedReader br = null;
		FileReader fr = null;
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(outputFile);
			fr = new FileReader(filepath);
			br = new BufferedReader(fr);
			String filename = filepath;
			double lat = Double.valueOf(filename.substring(1, 3));
			double lng = Double.valueOf(filename.substring(4, 7));
			char[] buffer = new char[2];

			if (filename.toLowerCase().charAt(0) == 's')
				lat *= -1;
			if (filename.toLowerCase().charAt(3) == 'w')
				lng *= 1;

			int cLevel = -1;	//current browsing level
			int cOccur = 0;		// current browsing level occur
			int tmpLevel = -1;

			for (int i = 0; i < length; i++) {
				for (int j = 0; j < length; j++) {
					if (br.read(buffer, 0, buffer.length) != -1) {
						height[i][j] = (buffer[0] << 8) | buffer[1];
						//String tmp = String.format("%.10f", lat + i * 1.0 / length) + "," + String.format("%.10f", lng + j * 0.001 / length) + "," + height[i][j] + "\n";
						//out.write(tmp.getBytes());
						tmpLevel = hlf.whichLevelIs(height[i][j]);
						if(cLevel == -1){
							cLevel = tmpLevel;
							cOccur++;
							continue;
						}

						if(cLevel == tmpLevel) {
							cOccur++;
						} else {
							String tmp = cOccur + "x" + cLevel + ",";
							out.write(tmp.getBytes());
							cLevel = tmpLevel;
							cOccur = 1;
						}

						if(i == length-1 && j == length-1){
							String tmp = cOccur + "x" + cLevel;
							out.write(tmp.getBytes());
						}
					} else {
						return "Error reading file : " + filename;
					}
				}
			}
		} catch (IOException e) {
			return "Error opening file : " + filepath;
		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();
				if (out != null)
					out.close();

			} catch (IOException ex) {
			}
		}
		return "Done";
	}


	public static void latlon2png(ArrayList<String> latlons) {
		int length = 1201;

		// TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
		// into integer pixels
		BufferedImage bi = new BufferedImage(length, length, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig2 = bi.createGraphics();

		for(int i = 0; i < latlons.size(); i++){
			int altitude = Integer.parseInt(latlons.get(i).split(",")[2]);
		}
	}
}

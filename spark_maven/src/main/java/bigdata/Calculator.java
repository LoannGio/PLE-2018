package bigdata;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class Calculator {

	public static ArrayList<String> hgt2list(String filepath){
		ArrayList<String> res = new ArrayList<String>();
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

		try {
			fr = new FileReader(filepath);
			br = new BufferedReader(fr);
			char[] buffer = new char[2];

			int cLevel = -1;	//current browsing level
			int cOccur = 0;		// current browsing level occur
			int tmpLevel = -1;

			for (int i = 0; i < length; i++) {
				for (int j = 0; j < length; j++) {
					if (br.read(buffer, 0, buffer.length) != -1){
						height[i][j] = (int) ((buffer[0] & 0xFF) << 8 | (buffer[1] & 0xFF));
						System.out.println(height[i][j]);
						tmpLevel = hlf.whichLevelIs(height[i][j]);
						if(cLevel == -1){
							cLevel = tmpLevel;
							cOccur++;
							continue;
						}

						if(cLevel == tmpLevel) {
							cOccur++;
						} else {
							String tmp = cOccur + "x" + cLevel;
							res.add(tmp);
							cLevel = tmpLevel;
							cOccur = 1;
						}

						if(i == length-1 && j == length-1){
							String tmp = cOccur + "x" + cLevel;
							res.add(tmp);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {
			}
		}
		return res;
	}


	public static void file2png(ArrayList<String> heights) {
		int length = 1201;

		// TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
		// into integer pixels
		BufferedImage bi = new BufferedImage(length, length, BufferedImage.TYPE_INT_ARGB);

		int [] color = new int[6];
		color[0] = (255<<24) | (0<<16) | (102<<8) | 205;
		color[1] = (255<<24) | (0<<16) | (255<<8) | 204;
		color[2] = (255<<24) | (0<<16) | (255<<8) | 0;
		color[3] = (255<<24) | (255<<16) | (255<<8) | 0;
		color[4] = (255<<24) | (204<<16) | (153<<8) | 0;
		color[5] = (255<<24) | (255<<16) | (255<<8) | 255;

		int currentImgI = 0;
		int currentImgJ = 0;
		int occur = -1;
		int level = -1;

		for(String s : heights){
			occur = Integer.valueOf(s.split("x")[0]);
			level = Integer.valueOf(s.split("x")[1]);
			for(int o = 0; o < occur; o++){
				bi.setRGB(currentImgJ, currentImgI, color[level]);
				if(currentImgJ < length-1){
					currentImgJ++;
				} else{
					currentImgI++;
					currentImgJ = 0;
				}
			}
		}
		try{
			File f = new File("out.png");
			ImageIO.write(bi, "png", f);
		}catch(IOException e){
			System.out.println(e);
		}
		System.out.println("Done");
	}
}

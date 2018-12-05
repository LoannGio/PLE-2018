package bigdata;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class Calculator {
	
	public static String hgt2latlon(String filepath, String outputFile){
		String res = "";
		int length = 1201;	
		int[][] height = new int[length][length];
		
		BufferedReader br = null;
		FileReader fr = null;
		FileOutputStream out = null;
		
		try{
			out = new FileOutputStream(outputFile);
			fr = new FileReader(filepath);
			br = new BufferedReader(fr);
			String filename = filepath.split("/")[1];
			double lat = Double.valueOf(filename.substring(1, 3));
			double lng = Double.valueOf(filename.substring(4,7));
			char[] buffer = new char[2];
			
			if(filename.toLowerCase().charAt(0) == 's')
				lat *= -1;
			if(filename.toLowerCase().charAt(3) == 'w')
				lng *= 1;
			
			for(int i = 0; i < length; i++){
				for(int j = 0; j < length; j++){
					if(br.read(buffer, 0, buffer.length) != -1){	
						height[i][j] = (buffer[0] << 8) | buffer[1];
						//System.out.println();
						String tmp = String.format("%.10f", lat + i * 1.0 / length) + "," + String.format("%.10f", lng + j *0.001 / length) + "," +height[i][j] + "\n";
						out.write(tmp.getBytes());
						
					}else{
						return "Error reading file : " + filename;
					}
				}
			}
		}catch(IOException e){
			return "Error opening file : " + filepath;	
		}finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();
				if(out != null)
					out.close();

			} catch (IOException ex) {}
		}		
		return res;
	}

}

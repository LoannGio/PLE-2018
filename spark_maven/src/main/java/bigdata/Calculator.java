package bigdata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Calculator {
	
	public static String hgt2latlon(String filename){
		String res = "";
		
		int length = 1201;	
		int[][] height = new int[length][length];
		
		BufferedReader br = null;
		FileReader fr = null;
				
		try{
			fr = new FileReader(filename);
			br = new BufferedReader(fr);
			
			double lat = Double.valueOf(filename.substring(1, 2));
			double lng = Double.valueOf(filename.substring(4,5));
			char[] buffer = new char[2];
			
			if(filename.toLowerCase().charAt(0) == 's')
				lat *= -1;
			if(filename.toLowerCase().charAt(3) == 'w')
				lng *= 1;
			
			for(int i = 0; i < length; i++){
				for(int j = 0; j < length; j++){
					if(br.read(buffer, 0, buffer.length) != -1){
						height[i][j] = (buffer[0] << 8) | buffer[1];
						double tmpLat = lat + i * 1.0 / length;
						double tmpLng = lng + j *0.001 / length;
						res += String.format("%.10f", tmpLat) + "," + String.format("%.10f", tmpLng) + "\n";
					}else{
						return "Error reading file : " + filename;
					}
				}
			}
		}catch(IOException e){
			return "Error opening file : " + filename;	
		}finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {}
		}		
		return res;
	}

}



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class ReadWriteCsv {

	private static String[] values;
	
	public static String[] getValues(){
		values = null;
		readFile();
		return values;
	}
	
	public static void saveValues(String value){
		writeFile(value);
	}
	
	public static void readFile() {

		BufferedReader br = null;

		try {

			br = new BufferedReader(new FileReader("depositedUniqueness.txt"));
			String line = null;

			while ((line = br.readLine()) != null) {

				String[] values = line.split(",");
			}
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			try {
				if (br != null)
					br.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void writeFile(String uniquenessString){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("depositedUniqueness.txt", true));
			out.append(uniquenessString + ",");
			out.close();
		} catch (IOException e) {
		}
	}
}
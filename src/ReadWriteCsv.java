

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class ReadWriteCsv {

	public void readFile() {

		BufferedReader br = null;

		try {

			br = new BufferedReader(new FileReader("depositedUniqueness.txt"));
			String line = null;

			while ((line = br.readLine()) != null) {

				String[] values = line.split(",");

				//Do necessary work with the values, here we just print them out
				for (String str : values) {
					System.out.println(str);
				}
				System.out.println();
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

	public void writeFile(String uniquenessString){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("depositedUniqueness.txt", true));
			out.append(uniquenessString + ",");
			out.close();
		} catch (IOException e) {
		}
	}

//	public static void main(String[] args) {
//		ReadWriteCsv test = new ReadWriteCsv();
//		test.readFile();
//		test.writeFile("1234");
//	}
}
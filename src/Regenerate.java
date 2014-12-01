import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class Regenerate {

	static String[] files;
	static Vector<String> exist;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File dic = new File("field2");
		if(dic.isDirectory())
			System.out.println("hahahaha");
		files = dic.list();
		
		exist = new Vector<String>();
		FileWriter fw = null;
		try {
			fw = new FileWriter("map_list.csv");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FileReader fr;
		try {
			fr = new FileReader("Apache_MapFile.csv");

			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String line = br.readLine();
				fw.write(line+"\n");
				for (String x : line.split(",")) {
					if (x.length() > 0)
						exist.add(x);
				}
			}
			fr.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String s : files) {
			String t = s.replace(".txt", "");
			if (!isExist(t))
				try {
					fw.write(t+",\n");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		try {
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static boolean isExist(String x) {
		if (exist.contains(x))
			return true;
		else
			return false;

	}

}

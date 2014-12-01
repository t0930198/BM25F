import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import tool.Evaluate;
import tool.Sentence;
import tool.TopN;

public class BM25F implements Runnable {

	Vector<Vector<String>> fullCluster;
	Vector<String> history;
	Vector<String> testing;

	private int[] hit;

	String cluster_file;
	String history_file;
	String test_file;

	public static double w1 = 1.163;
	public static double w2 = 0.013;
	public static double w3 = 2.285;
	public static double w4 = 0.032;
	public static double w5 = 0.772;
	public static double w6 = 0.381;
	public static double w7 = 2.427;

	public static double wf1 = 2.999;
	public static double wf2 = 0.994;
	public static double bf1 = 0.504;
	public static double bf2 = 1.0;
	public static double k1 = 2.0;
	public static double k3 = 0.001;
	public static int BM25Feature3;
	public static int BM25Feature4;
	public static int BM25Feature5;
	public static float BM25Feature6;
	public static float BM25Feature7;

	public static void main(String[] args) throws IOException,
			InterruptedException {

		Thread main = new Thread(new BM25F(), "Apache");
		main.start();
		main.join();
	}

	public BM25F() {

		this.fullCluster = new Vector<Vector<String>>();
		this.history = new Vector<String>();
		this.testing = new Vector<String>();

		hit = new int[10];
		for(int i=0;i<10;i++)
			hit[i]=0;
		
		cluster_file = "map_list.csv";
		history_file = "apache_history.txt";
		test_file = "test_file.csv";
		BufferedReader fileReader;
		try {
			fileReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(cluster_file), "utf8"));
			Vector<String> v;
			while (fileReader.ready()) {
				v = new Vector<String>();
				for (String x : fileReader.readLine().split(",")) {
					if (x.length() > 0) {
						v.add(x);
						history.add(x);
					}

				}
				fullCluster.add(v);
			}
			fileReader.close();
			fileReader = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * try { fileReader = new BufferedReader(new InputStreamReader( new
		 * FileInputStream(history_file), "utf8")); while (fileReader.ready()) {
		 * for (String x : fileReader.readLine().split(",")) { if (x.length() >
		 * 0) history.add(x); } } } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		try {
			fileReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(test_file), "utf8"));
			while (fileReader.ready()) {
				testing.add(fileReader.readLine().split(",")[0]);

			}
			fileReader.close();
			fileReader = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void run() {

		File logfile = new File("log_Vulnerabilities.txt");
		logfile.delete();
		try {
			logfile.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (String x : testing) {

			Sentence test = new Sentence();
			test.fileName = x;

			try {
				BufferedReader fieldReader = new BufferedReader(
						new InputStreamReader(new FileInputStream("field1/"
								+ test.fileName + ".txt"), "utf8"));

				test.setField1(fieldReader.readLine());
				fieldReader.close();

				fieldReader = new BufferedReader(
						new InputStreamReader(new FileInputStream("field2/"
								+ test.fileName + ".txt"), "utf8"));
				test.setField2(fieldReader.readLine());
				fieldReader.close();
				fieldReader = null;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ArrayList<Sentence> training = new ArrayList<Sentence>();

			for (int j = 0; j < history.size(); j++) {
				Sentence trainingData = new Sentence();
				trainingData.fileName = history.get(j);
				if (Integer.parseInt(trainingData.fileName) > Integer
						.parseInt(test.fileName)||Integer.parseInt(trainingData.fileName)==Integer.parseInt(test.fileName))
					continue;
				try {
					BufferedReader fieldReader = new BufferedReader(
							new InputStreamReader(new FileInputStream("field1/"
									+ trainingData.fileName + ".txt"), "utf8"));
					trainingData.setField1(fieldReader.readLine());
					fieldReader.close();
					fieldReader = new BufferedReader(new InputStreamReader(
							new FileInputStream("field2/"
									+ trainingData.fileName + ".txt"), "utf8"));
					trainingData.setField2(fieldReader.readLine());
					fieldReader.close();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				training.add(trainingData);

			}
			BM25Fext(test, training);
		}
		for(int z=0;z<hit.length;z++){
		Evaluate ev = new Evaluate(hit[z], testing.size() - hit[z], testing.size()
				- hit[z], 0);
		System.out.println("Top "+(z+1)+" recall rate is " + ev.getRecall() * 100
				+ "%");
		}

	}

	public void BM25Fext(Sentence test, ArrayList<Sentence> training) {

		// single word
		wf1 = 2.98;
		wf2 = 0.287;
		bf1 = 0.703;
		bf2 = 1.0;
		k1 = 2.0;
		k3 = 0.382;
		double avgLengthF1 = 0;
		double avgLengthF2 = 0;
		for (int i = 0; i < training.size(); i++) {
			Sentence nowSentence = training.get(i);
			avgLengthF1 += nowSentence.field1.size();
			avgLengthF2 += nowSentence.field2.size();
		}

		avgLengthF1 /= training.size();
		avgLengthF2 /= training.size();

		Map<String, Double> IDFMap = IDF(test, training);
		Map<String, Double> TFQMap = TFQ(test, IDFMap);

		Map<String, Double> BM25FextMapForSingleWord = new HashMap<String, Double>();
		for (int i = 0; i < training.size(); i++) {
			Sentence nowTraining = training.get(i);
			double result = 0;
			for (String key : IDFMap.keySet()) {
				double TFd = TFD(key, nowTraining, avgLengthF1, avgLengthF2);
				result += IDFMap.get(key)
						* (((k3 + 1) * TFQMap.get(key)) / (k3 + TFQMap.get(key)))
						* (TFd / (k1 + TFd));
			}
			BM25FextMapForSingleWord.put(nowTraining.fileName, result);
		}

		// double word
		wf1 = 2.999;
		wf2 = 0.994;
		bf1 = 0.504;
		bf2 = 1.0;
		k1 = 2.0;
		k3 = 0.001;
		test.changeToDoubleWord();
		for (int i = 0; i < training.size(); i++) {
			training.get(i).changeToDoubleWord();
		}
		avgLengthF1 = 0;
		avgLengthF2 = 0;
		for (int i = 0; i < training.size(); i++) {
			Sentence nowSentence = training.get(i);
			avgLengthF1 += nowSentence.field1.size();
			avgLengthF2 += nowSentence.field2.size();
		}

		avgLengthF1 /= training.size();
		avgLengthF2 /= training.size();

		IDFMap = IDF(test, training);
		TFQMap = TFQ(test, IDFMap);

		Map<String, Double> BM25FextMapForDoubleWord = new HashMap<String, Double>();
		for (int i = 0; i < training.size(); i++) {
			Sentence nowTraining = training.get(i);
			double result = 0;
			for (String key : IDFMap.keySet()) {
				double TFd = TFD(key, nowTraining, avgLengthF1, avgLengthF2);
				result += IDFMap.get(key)
						* (((k3 + 1) * TFQMap.get(key)) / (k3 + TFQMap.get(key)))
						* (TFd / (k1 + TFd));
			}
			BM25FextMapForDoubleWord.put(nowTraining.fileName, result);
		}
		double resultValue = -1;
		TopN topN = new TopN(10, test.fileName);

		try {
			FileWriter log = new FileWriter("log_Vulnerabilities.txt", true);// change
			log.write(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>+\n");
			log.write("test fileName : " + test.fileName
					+ "\n------------------------------\n");
			String resultFileName = "";
			for (String key : BM25FextMapForDoubleWord.keySet()) {
				// System.out.println("train: "+key+" test: "+test.fileName);
				// --------------------------------------------------------
				int i = 0, j = 0;
				String[] strs = new String[100];
				String[] strs1 = new String[100];
				String[] ver = new String[1000];
				Scanner sc = null;
				Scanner sc1 = null;
				Scanner ve = null;

				try {
					// System.out.println(key + " " + test.fileName);
					sc = new Scanner(new File("field3/" + key + ".txt"));
					sc1 = new Scanner(new File("field3/" + test.fileName
							+ ".txt"));

					while (sc.hasNext()) {
						strs[i] = sc.nextLine();
						strs1[i] = sc1.nextLine();
						i++;

					}
					sc = null;
					sc1 = null;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.exit(0);
				}
				// System.out.println(strs[2]+" , "+key);
				// System.out.println(strs1[2]+" , "+test.fileName );
				// System.out.println(strs[3]+" , "+key);
				// System.out.println(strs1[3]+" , "+test.fileName );
				// sc.close();
				// ----------------------------------------------------

				try {
					ve = new Scanner(new File("eclipse_version.txt"));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.exit(0);
				}
				while (ve.hasNext()) {
					ver[j] = ve.next();
					// UW[ j++ ]O
					j++;
				} // System.out.println(ver[14]);
				ve.close();

				// --------------------------------------------------------
				int flag3, flag4, flag7, flag8;
				int flag5 = 1;
				int flag7d = 0, flag7q = 0;
				// System.out.println("  compareTo() ");
				flag3 = strs[0].compareTo(strs1[0]);
				// System.out.println(flag);
				if (flag3 == 0) {
					BM25Feature3 = 1;
				} else {
					BM25Feature3 = 0;
				}

				flag4 = strs[1].compareTo(strs1[1]);
				if (flag4 == 0) {
					BM25Feature4 = 1;
				} else {
					BM25Feature4 = 0;
				}

				BM25Feature5 = flag5;

				float flag6d = Integer.parseInt(strs[3]);
				float flag6q = Integer.parseInt(strs1[3]);
				float num = 1 + (Math.abs(flag6d - flag6q));

				BM25Feature6 = 1 / num;

				for (int k = 0; k < 645; k++) {
					flag7 = ver[k].compareTo(strs[2]);
					flag8 = ver[k].compareTo(strs1[2]);
					if (flag7 == 0) {
						flag7q = k;
					}
					if (flag8 == 0) {
						flag7d = k;
					} //
					//System.out.println(ver[37]);
				}

				// System.out.println(strs[2]);
				// System.out.println(strs1[2]);
				// System.out.println(flag7d + "   " + flag7q);

				// System.out.println(BM25Feature6);
				// System.out.println(strs[2]);
				// System.out.println(strs1[2]);
				// float flag7d = Float.parseFloat(strs[2]);
				// float flag7q = Float.parseFloat(strs1[2]);
				float num1 = 1 + (Math.abs(flag7d - flag7q));
				BM25Feature7 = 0;

				// BM25Feature7=1;

				if (w1 * BM25FextMapForSingleWord.get(key) + w2
						* BM25FextMapForDoubleWord.get(key) + w3 * BM25Feature3
						+ w4 * BM25Feature4 + w5 * BM25Feature5 + w6
						* BM25Feature6 + w7 * BM25Feature7 > resultValue) {
					resultValue = w1 * BM25FextMapForSingleWord.get(key) + w2
							* BM25FextMapForDoubleWord.get(key) + w3
							* BM25Feature3 + w4 * BM25Feature4 + w5
							* BM25Feature5 + w6 * BM25Feature6 + w7
							* BM25Feature7;
					resultFileName = key;
					// System.out.println(BM25Feature3);
				}
				topN.add(Integer.parseInt(key),
						w1 * BM25FextMapForSingleWord.get(key) + w2
								* BM25FextMapForDoubleWord.get(key) + w3
								* BM25Feature3 + w4 * BM25Feature4 + w5
								* BM25Feature5 + w6 * BM25Feature6 + w7
								* BM25Feature7);

				log.write("filename : " + key + "  single : "
						+ BM25FextMapForSingleWord.get(key) + " double : "
						+ BM25FextMapForDoubleWord.get(key) + "  Feature3: "
						+ BM25Feature3 + "  Feature4: " + BM25Feature4
						+ "  Feature5: " + BM25Feature5 + "  Feature6: "
						+ BM25Feature6 + "  Feature7: " + BM25Feature7 + "\n");
			}
			log.write("Highest filename : " + resultFileName + " value : "
					+ resultValue + "\n");
			log.write("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" + "\n");
			log.flush();
			log.close();

			FileWriter fw = new FileWriter("result_Vulnerabilities.txt", true);// change
			fw.write(test.fileName + "," + resultFileName + "," + resultValue
					+ "\n");// change by
			fw.flush();
			fw.close();
			// topN.print();
			for(int z=1;z<11;z++)
				if (topN.match(fullCluster, z) > 0) {
					hit[z-1] += 1;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Map<String, Double> IDF(Sentence test,
			ArrayList<Sentence> training) {

		Map<String, Double> IDFMap = new HashMap<String, Double>();
		for (int i = 0; i < test.field1.size() + test.field2.size(); i++) {
			String target;
			if (i < test.field1.size()) {
				target = test.field1.get(i);
			} else {
				target = test.field2.get(i - test.field1.size());
			}
			double idf = CalculatIDF(target, training);
			if (idf > 0) {
				IDFMap.put(target, idf);
			}
		}
		return IDFMap;
	}

	public static Map<String, Double> TFQ(Sentence test,
			Map<String, Double> IDFMap) {

		Map<String, Double> TFQMap = new HashMap<String, Double>();
		for (String key : IDFMap.keySet()) {
			int occurrenceInF1 = 0;
			int occurrenceInF2 = 0;
			// occurrence in field1
			for (int i = 0; i < test.field1.size(); i++) {
				String target = test.field1.get(i);
				if (key.equals(target)) {
					occurrenceInF1++;
				}
			}
			// occurrence in field2
			for (int i = 0; i < test.field2.size(); i++) {
				String target = test.field2.get(i);
				if (key.equals(target)) {
					occurrenceInF2++;
				}
			}

			TFQMap.put(key, (double) (wf1 * occurrenceInF1)
					+ (double) (wf2 * occurrenceInF2));
		}

		return TFQMap;
	}

	public static double TFD(String test, Sentence training,
			double avgLengthF1, double avgLengthF2) {

		int occurrencesF1 = 0;
		for (int j = 0; j < training.field1.size(); j++) {
			if (test.equals(training.field1.get(j))) {
				occurrencesF1++;
			}
		}

		int occurrencesF2 = 0;
		for (int j = 0; j < training.field2.size(); j++) {
			if (test.equals(training.field2.get(j))) {
				occurrencesF2++;
			}
		}

		return (wf1 * occurrencesF1 / (1 - bf1 + (bf1 * training.field1.size() / avgLengthF1)))
				+ (wf2 * occurrencesF2 / (1 - bf2 + (bf2
						* training.field2.size() / avgLengthF2)));

	}

	public static double CalculatIDF(String test, ArrayList<Sentence> group) {
		int occurrences = 0;
		for (int i = 0; i < group.size(); i++) {
			Sentence training = group.get(i);
			for (int j = 0; j < training.field1.size() + training.field2.size(); j++) {
				String fromTraining = "";
				if (j < training.field1.size()) {
					fromTraining = training.field1.get(j);
				} else {
					fromTraining = training.field2.get(j
							- training.field1.size());
				}
				if (test.equals(fromTraining)) {
					occurrences++;
					break;
				}
			}
		}
		if (occurrences == 0) {
			return 0;
		} else {
			return Math.log((double) group.size() / occurrences);
		}
	}
}

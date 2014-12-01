package tool;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class TopN {

	private int id[];
	private double score[];
	private int n;
	private String target;

	public TopN(int n, String target) {
		this.n = n;
		this.target = target;
		id = new int[n + 1];
		score = new double[n + 1];
		for (int i = 0; i < id.length; i++) {
			id[i] = 0;
			score[i] = 0;
		}
	}

	public void print() {
		System.out.println("Top " + n + " score of " + target + ":");
		for (int i = 0; i < id.length; i++)
			System.out.println(id[i] + ": " + score[i]);
	}

	public void add(int id, double score) {
		this.id[n] = id;
		this.score[n] = score;
		sort();
	}

	private void sort() {
		for (int i = 0; i < id.length; i++)
			for (int j = 0; j < id.length - 1; j++) {
				if (score[j] < score[j + 1])
					swap(j, j + 1);
			}
	}

	private void swap(int a, int b) {
		int x = 0;
		x = id[a];
		id[a] = id[b];
		id[b] = x;

		double y = 0;
		y = score[a];
		score[a] = score[b];
		score[b] = y;
	}

	public int match(Vector<Vector<String>> cluster,int t) {
			
		for (Vector<String> x : cluster) {
			for (int i =0;i<t;i++) {
				if (x.contains(Integer.toString(id[i])) && x.contains(target)){
					return cluster.indexOf(x);
				}
			}
		}
		return -1;
	}
}

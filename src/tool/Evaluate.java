package tool;

public class Evaluate {

	private int tp, fp;
	private int tn, fn;
	
	
	public Evaluate(){
		
	}
	public Evaluate(int tp, int fp, int tn, int fn){
		this.tp=tp;
		this.fp=fp;
		this.tn=tn;
		this.fn=fn;
	}
	public double getRecall(){
		return ((double)tp)/(double)(tp+tn);
	}
	public double getPrecision(){
		return ((double)tp/(double)(tp+fp));
	}
	
}

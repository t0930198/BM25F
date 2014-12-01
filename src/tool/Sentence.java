package tool;
import java.util.ArrayList;


public class Sentence {
	
	
	public String fileName;
	public ArrayList<String> field1 = new ArrayList<String>();
	public ArrayList<String> field2 = new ArrayList<String>();
	
	public void setField1(String string) {
		if(string != null) {
		String[] spiltedString = string.split(" ");
		for(int i = 0 ; i< spiltedString.length ; i++){
			field1.add(spiltedString[i]);
		}
	}
	}
	
	public void setField2(String string) {
		if(string != null) {
			String[] spiltedString = string.split(" ");
			for(int i = 0 ; i< spiltedString.length ; i++){
				field2.add(spiltedString[i]);
			}
		}
	}
	
	public void changeToDoubleWord(){
		
		ArrayList<String> f1 = new ArrayList<String>();
		ArrayList<String> f2 = new ArrayList<String>();
		
		for(int i = 1 ; i < field1.size() ; i++) {
			f1.add(field1.get(i-1) + " " + field1.get(i));
		}
		
		for(int i = 1 ; i < field2.size() ; i++) {
			f2.add(field2.get(i-1) + " " + field2.get(i));
		}
		
		field1 = f1;
		field2 = f2;
	}
}

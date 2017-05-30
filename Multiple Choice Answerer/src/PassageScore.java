
/*
 * class serves the purpose of holding a passage's score and the
 * index of which answer that score is for
 */
public class PassageScore {
	public double score;
	public int ansIndex;
	private static char[] letters = {'a', 'b', 'c', 'd', 'e'};
	
	public PassageScore(double s, int a){
		score = s;
		ansIndex = a;
	}
	public PassageScore(int a, double s){
		score = s;
		ansIndex = a;
	}
	public PassageScore(double s){
		score = s;
		ansIndex = -1;
	}
	public PassageScore(int a){
		score = -1;
		ansIndex = a;
	}
	
	public String toString(){
		return "Ans: " + letters[ansIndex] + " Score: " + score + "\n";
	}
}

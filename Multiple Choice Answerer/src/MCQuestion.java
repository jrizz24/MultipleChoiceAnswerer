import java.util.ArrayList;

public class MCQuestion {
	private static char[] letters = {'A', 'B', 'C', 'D', 'E'};
	
	private String question;
	private ArrayList<String> choices;
	private int answer;
	
	private int choiceNum;
	
	public MCQuestion(String[] q){
		choices = new ArrayList<String>();
		question = q[0];
		for(int i = 1; i < q.length; i++){
			choices.add(q[i]);
		}
		choiceNum = q.length - 1;
	}
	public MCQuestion(String[] q, int a){
		choices = new ArrayList<String>();
		question = q[0];
		for(int i = 1; i < q.length; i++){
			choices.add(q[i]);
		}
		choiceNum = q.length - 1;
		answer = a;
	}

	public String getQuestion(){return question;}
	public String getA(){return choices.get(0);}
	public String getB(){return choices.get(1);}
	public String getC(){return choices.get(2);}
	public String getD(){return choices.get(3);}
	public String getE(){return choices.get(4);}
	public ArrayList<String> getChoices(){return choices;}
	public int getChoiceNum(){return choiceNum;}
	
	public String toString(){
		String result = "";
		result += question + "\n";
		for(int i = 0; i < choices.size(); i++){
			result += letters[i] + ") " + choices.get(i) + "\n";
		}
		
		return result;
	}
	public int getAnswer() {
		return answer;
	}
}

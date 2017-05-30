import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MCDataSetTest implements Runnable {
	static MCAnswerer answerer = new MCAnswerer();;
	static Integer correct = 0;
	static Integer total = 0;
	MCQuestion ques;
	static ArrayList<Thread> threads = new ArrayList<Thread>();

	public MCDataSetTest(MCQuestion q) {
		ques = q;
	}

	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		PrintStream incorrect = new PrintStream("res/incorrect.txt");
		Reader mcJSON = new FileReader("res/MCdataset.json");
		JSONTokener tokener = new JSONTokener(mcJSON);
		JSONArray mcJSONArr = new JSONArray(tokener);
		Random r = new Random();
		for (int i = 0; i < 400; i++) {
			int index = r.nextInt(mcJSONArr.length());
			JSONObject jsonQ = (JSONObject) mcJSONArr.remove(index);
			String[] questions = new String[5];
			questions[0] = (String) jsonQ.get("question");
			questions[1] = (String) jsonQ.get("A");
			questions[2] = (String) jsonQ.get("B");
			questions[3] = (String) jsonQ.get("C");
			questions[4] = (String) jsonQ.get("D");
			int answer = 0;
			String ans = (String) jsonQ.get("answer");
			if (ans.equals("B")) {
				answer = 1;
			} else if (ans.equals("C")) {
				answer = 2;
			} else if (ans.equals("D")) {
				answer = 3;
			}
			MCQuestion question = new MCQuestion(questions, answer);
			int answerFound = -1;
			total++;
			System.out.println();
			System.out.println(question.toString());
			System.out.println("Answer: " + answerer.letters[question.getAnswer()]);
			answerFound = answerer.run(question.toString());
			if (answerFound == question.getAnswer()) {
				System.out.println("CORRECT!!!!");
				correct++;
			}
			else{
				System.out.println("NOT CORRECT:(");
				incorrect.println(question.toString());
				incorrect.println("Answer: " + answerer.letters[question.getAnswer()]);
				incorrect.println();
			}
			System.out.println("Running Accuracy: %" + (correct / (total * 1.0)) * 100 + " (" + correct + " of " + total  +")" );
			int time = r.nextInt(600000) + 120000;
			Thread.sleep(time);
			
		}
		double percentCorrect = (correct / (total * 1.0)) * 100;
		System.out.println("Accuracy: %" + percentCorrect);
		Toolkit.getDefaultToolkit().beep();

	}

	@Override
	public void run() {
		

		
	}

}

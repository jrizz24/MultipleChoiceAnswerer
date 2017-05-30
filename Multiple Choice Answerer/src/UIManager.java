import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JButton;

public class UIManager extends JFrame {

	private JPanel contentPane;
	private JTextArea textArea;
	private JButton btnFindAnswer;
	
	private MCAnswerer mc;

	/**
	 * Create the frame.
	 */
	public UIManager() {
		mc = new MCAnswerer();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 700, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textArea = new JTextArea();
		textArea.setTabSize(4);
		textArea.setWrapStyleWord(true);
		contentPane.add(textArea);
		textArea.setBounds(15, 10, 661, 320);
		
		btnFindAnswer = new JButton("Find Answer");
		btnFindAnswer.setBounds(555, 342, 121, 29);
		contentPane.add(btnFindAnswer);
		btnFindAnswer.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				String mcinput = textArea.getText();
				mc.run(mcinput);
			  } 
			} );	
	}
	
	void showMCInput(MCQuestion question){
		char[] letters = {'a', 'b', 'c', 'd', 'e'};
		textArea.setText("Your Inputed Multiple Choice Question: \n\n");
		textArea.append(question.getQuestion() + "\n");
		for(int i = 0; i < question.getChoiceNum(); i++){
			textArea.append(letters[i] + ") " + question.getChoices().get(i) + "\n");
		}
	}
	
	public void appendAnswer(char ans){
		textArea.append("\nAnwer:    " + ans);
	}

}

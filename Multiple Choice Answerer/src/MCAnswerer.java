import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import org.jsoup.nodes.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MCAnswerer {
	public static char[] letters = { 'A', 'B', 'C', 'D', 'E' };

	private static UIManager ui;
	private MCQuestion question;
	private static LanguageProcessing nlp;

	ArrayList<String> keywordLemmas;
	ArrayList<ArrayList<String>> answerLemmasList;
	ArrayList<String> answerLemmas;
	HashMap<String, Double> keywordTfidf;

	public MCAnswerer() {
		nlp = new LanguageProcessing();
		ui = null;
	}

	public static void main(String[] args) throws FileNotFoundException {
		nlp = new LanguageProcessing();
		ui = new UIManager();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// open window
					ui.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	// function called when UI button clicked
	public int run(String mcinput) {
		parse(mcinput);
		if (ui != null)
			ui.showMCInput(question);

		// get keyword lemmas from question
		keywordLemmas = nlp.getLemmas(question.getQuestion());
		keywordLemmas = removeStopWords(keywordLemmas.toArray(new String[0]));
		System.out.println(keywordLemmas.toString());
		// query formulated by removing stopwords
		String keywordQuery = removeStopWords(question.getQuestion());
		System.out.println(keywordQuery);
		
		// from answers
		answerLemmasList = new ArrayList<ArrayList<String>>();
		answerLemmas = new ArrayList<String>();
		for (String ans : question.getChoices()) {
			ans = ans.substring(0, 1).toUpperCase() + ans.substring(1);
			ArrayList<String> lemmas = nlp.getLemmas(ans);
			answerLemmasList.add(lemmas);
		}
		for(ArrayList<String> ans : answerLemmasList){
			ans = removeStopWords(ans.toArray(new String[0]));
			answerLemmas.addAll(ans);
		}
		// search google using query

		//ArrayList<Document> docs = Qrobe.search(keywordQuery);
		ArrayList<Document> docs = GoogleSearch.search(keywordQuery);

		// get all the text from the websites
		// and remove stop words
		ArrayList<String> text = new ArrayList<String>();
		for (Document doc : docs) {
			if (doc.body() != null) {
				text.add(doc.body().text());
			}
		}

		// get list of annotations
		ArrayList<Annotation> annotations = nlp.getAnnotations(text);

		// get important passages from the website's text
		ArrayList<ArrayList<String>> passages = getPassages(annotations);
		removeStopWords(passages);
		// System.out.println(passages.toString());
		System.out.println(passages.size());

		// give each keyword a tfidf score based on the all the passages
		// containing
		// at least one ans or ques keyword
		keywordTfidf = keywordTfidf(passages);

		// filter those passages even more
		passages = filterPassages(passages);
		// System.out.println(passages.toString());
		System.out.println(passages.size());

		// score all the passages
		ArrayList<PassageScore> passageScores = scorePassages(passages);

		double[] ansScores = getAnsScores(passageScores);
		System.out.println();
		System.out.println("Answer Scores: ");
		double max = -1;
		int index = -1;
		for (int i = 0; i < ansScores.length; i++) {
			System.out.println(letters[i] + ": %" + ansScores[i] * 100);
			if (ansScores[i] > max) {
				max = ansScores[i];
				index = i;
			}
		}
		if (ui != null && max != -1) {
			ui.appendAnswer(letters[index]);
		}

		return index;
	}

	public double[] getAnsScores(ArrayList<PassageScore> psgScores) {
		double[] ansScores = new double[question.getChoiceNum()];
		for (int i = 0; i < ansScores.length; i++) {
			ansScores[i] = 0.0;
		}
		double total = 0.0;
		for (PassageScore ps : psgScores) {
			total += ps.score;
			ansScores[ps.ansIndex] += ps.score;
		}
		for (int i = 0; i < ansScores.length; i++) {
			ansScores[i] = ansScores[i] / total * 1.0;
		}

		return ansScores;
	}

	/*
	 * takes the passages as a paramater and returns a map of tfidf scores for
	 * each keyword in the question. it uses the term frequency in the question
	 * and the document frequency of all the passages
	 */
	public HashMap<String, Double> keywordTfidf(ArrayList<ArrayList<String>> passages) {
		HashMap<String, Double> scores = new HashMap<String, Double>();
		HashMap<String, Double> docFreq = new HashMap<String, Double>();
		// set scores hashmap to hold tf values
		// init docFreq map with 0s
		for (String kw : keywordLemmas) {
			if (scores.containsKey(kw)) {
				scores.put(kw, scores.get(kw) + 1);
			} else {
				scores.put(kw, 1.0);
			}
			docFreq.put(kw, 0.0);
		}
		for (Map.Entry<String, Double> term : scores.entrySet()) {
			term.setValue(term.getValue() / keywordLemmas.size() * 1.0);
		}

		// populate docFreq map values with term document frequencies
		for (ArrayList<String> passage : passages) {
			// a set to hold the keywords that this passage contains to make
			// sure no duplicates
			Set<String> keyw = new TreeSet<String>();
			for (String word : passage) {
				if (docFreq.containsKey(word)) {
					keyw.add(word);
				}
				// if the size of the set is the size of the map we can break
				// loop - psg contains all
				if (docFreq.size() == keyw.size()) {
					break;
				}
			}
			// add one to each word found
			for (String kw : keyw) {
				docFreq.put(kw, docFreq.get(kw) + 1);
			}
			// done, check next passage
		}

		// finalize by calculating tf-idf for each term
		// start with adjusting the docFreq values according to idf formula
		for (Map.Entry<String, Double> term : docFreq.entrySet()) {
			if (term.getValue() != 0.0) {
				double d = passages.size() / term.getValue();
			}
			// catch divide by zero error
			else {
				// if the term isnt in any passage, set it to zero - its
				// worthless
				term.setValue(0.0);
			}
		}
		// multiply corresponding map values together for result
		for (Map.Entry<String, Double> term : scores.entrySet()) {
			term.setValue(term.getValue() * docFreq.get(term.getKey()));
		}

		return scores;
	}

	public ArrayList<PassageScore> scorePassages(ArrayList<ArrayList<String>> passages) {
		// the best possible score a passage can get regarding question keywords
		// i.e. the sum of the tfidf scores for the question keywords
		double bestQuestionKWScore = 0.0;
		for (Map.Entry<String, Double> term : keywordTfidf.entrySet()) {
			bestQuestionKWScore += term.getValue();
		}
		ArrayList<PassageScore> scores = new ArrayList<PassageScore>();
		for (int i = 0; i < passages.size(); i++) {
			// keeping track of word occurances in set to avoid duplication
			Set<String> keyws = new TreeSet<String>();
			for (String word : passages.get(i)) {
				if (keywordLemmas.contains(word)) {
					keyws.add(word);
				}
			}
			// the score for how well a passage matches the question is
			// calculated
			// as the sum of the tfidf weights(with respect to the question) of
			// the
			// existing keywords in the passage, divided by the the sum of the
			// tfidf socres
			double queKWScore = 0.0;
			for (String word : keyws) {
				queKWScore += 1;// * keywordTfidf.get(word);
			}
			// if the passage contains all the words, the score will be 1
			queKWScore = queKWScore / keywordTfidf.size();

			// loop through answers and find the answer with the max score
			// whichever is the max will be the answer this passage represents
			double max = -1.0;
			int index = -1;
			for (int a = 0; a < answerLemmasList.size(); a++) {
				ArrayList<String> ansKeywords = answerLemmasList.get(a);
				keyws.clear();
				for (String word : passages.get(i)) {
					if (ansKeywords.contains(word)) {
						keyws.add(word);
					}
				}
				double ansKWScore = 0.0;
				for (String word : keyws) {
					ansKWScore++;
				}
				ansKWScore = ansKWScore / answerLemmasList.size() * 1.0;
				if (ansKWScore > max) {
					max = ansKWScore;
					index = a;
				}
			}
			scores.add(new PassageScore(queKWScore * max, index));
		}

		return scores;
	}

	/////////////////////
	/*
	 * filters passages to be even more relevant removing ones that do not
	 * contain a keyword from the answer and the question
	 */
	public ArrayList<ArrayList<String>> filterPassages(ArrayList<ArrayList<String>> passages) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < passages.size(); i++) {
			boolean ans = false;
			boolean ques = false;
			for (String word : passages.get(i)) {
				if (answerLemmas.contains(word)) {
					ans = true;
				}
				if (keywordLemmas.contains(word)) {
					ques = true;
				}
				if (ans && ques) {
					result.add(passages.get(i));
					break;
				}
			}
		}
		return result;
	}

	/*
	 * finds relevant passages in the returned documents by if checking each
	 * sentence contains a keyword from either the question or the answer. if
	 * this is true for consecutive sentences, they are put into the same
	 * passage.
	 */
	public ArrayList<ArrayList<String>> getPassages(Annotation a) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		List<CoreMap> sentences = a.get(CoreAnnotations.SentencesAnnotation.class);

		// get the lemma for each sentence
		for (int i = 0; i < sentences.size(); i++) {
			ArrayList<String> passage = new ArrayList<String>();
			String entityMatch = "O";
			boolean lastOne = false;
			for (CoreLabel token : sentences.get(i).get(CoreAnnotations.TokensAnnotation.class)) {
				String entitytag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				if (entitytag.equals("LOCATION") || entitytag.equals("ORGANIZATION") || entitytag.equals("PERSON")) {
					if (entitytag.equals(entityMatch)) {
						passage.set(passage.size()-1, passage.get(passage.size()-1) + " " + token.get(CoreAnnotations.TextAnnotation.class));
					} else {
						passage.add(token.getString(CoreAnnotations.TextAnnotation.class));
						entityMatch = entitytag;
					}
				} else {
					passage.add(token.get(CoreAnnotations.LemmaAnnotation.class));
					entityMatch = "O";
				}
			}

			// check if the passage contains a keyword
			for (String word : passage) {
				if (keywordLemmas.contains(word) || answerLemmas.contains(word)) {
					/*
					 * if the last sentence is in result, add it to that passage
					 * it could be about the same topic. We dont want a passage
					 * giving only half an answer i.e. a question keyword in one
					 * one sentence and a an answer key word in the following
					 * sentence
					 */
					if (lastOne) {
						result.get(result.size() - 1).addAll(passage);
					}
					// else add new passage
					else {
						result.add(passage);
					}
					lastOne = true;
					break;
				} else {
					lastOne = false;
				}
			}
		}
		return result;
	}

	public ArrayList<ArrayList<String>> getPassages(ArrayList<Annotation> annotations) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		for (Annotation a : annotations) {
			result.addAll(getPassages(a));
		}

		return result;
	}

	public String removeStopWords(String question) {
		// get stopwords from text file
		Set<String> stopwords = new TreeSet<String>();
		Scanner s = null;
		try {
			s = new Scanner(new File("res/stopword.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (s.hasNext()) {
			stopwords.add(s.next());
		}
		s.close();

		String[] temp = question.replaceAll("[^a-zA-Z0-9' ]", "").replace("'", "\"").toLowerCase().split(" ");
		String result = "";
		for (int i = 0; i < temp.length; i++) {
			if (!stopwords.contains(temp[i]))
				result = result + temp[i] + " ";
		}
		return result.trim();
	}

	public ArrayList<String> removeStopWords(String[] question) {
		ArrayList<String> result = new ArrayList<String>();
		Set<String> stopwords = new TreeSet<String>();
		Scanner s = null;
		try {
			s = new Scanner(new File("res/stopword.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (s.hasNext()) {
			stopwords.add(s.next());
		}
		s.close();
		for (int i = 0; i < question.length; i++) {
			if (!stopwords.contains(question[i]))
				result.add(question[i]);
		}

		return result;
	}

	public void removeStopWords(ArrayList<ArrayList<String>> passages) {
		// get stopwords from text file
		Set<String> stopwords = new TreeSet<String>();
		Scanner s = null;
		try {
			s = new Scanner(new File("res/stopword.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (s.hasNext()) {
			stopwords.add(s.next());
		}
		s.close();
		for (int i = 0; i < passages.size(); i++) {
			ArrayList<String> stopLess = new ArrayList<String>();
			for (String word : passages.get(i)) {
				if (!stopwords.contains(word))
					stopLess.add(word);
			}
			passages.set(i, stopLess);
		}
	}

	public void parse(String input) {
		String[] parsedQuestion = input.split("\\r?\\n");
		for (int i = 1; i < parsedQuestion.length; i++) {
			parsedQuestion[i] = parsedQuestion[i].trim();
			parsedQuestion[i] = parsedQuestion[i].substring(2);
			parsedQuestion[i] = parsedQuestion[i].trim();
		}
		question = new MCQuestion(parsedQuestion);

	}

}

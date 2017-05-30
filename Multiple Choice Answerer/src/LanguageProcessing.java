import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

public class LanguageProcessing {

	private StanfordCoreNLP pipeline;

	public LanguageProcessing() {
		Properties props = new Properties();
		RedwoodConfiguration.empty().capture(System.err).apply();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		RedwoodConfiguration.current().clear().apply();
		pipeline = new StanfordCoreNLP(props);
	}

	public ArrayList<Annotation> getAnnotations(ArrayList<String> docs) {
		ArrayList<Annotation> result = new ArrayList<Annotation>();
		for (String doc : docs) {
			Annotation a = new Annotation(doc);
			pipeline.annotate(a);
			result.add(a);
		}

		return result;
	}

	public ArrayList<String> getLemmas(String str) {
		ArrayList<String> result = new ArrayList<String>();
		Annotation a = new Annotation(str);
		pipeline.annotate(a);
		List<CoreLabel> tokens = a.get(CoreAnnotations.TokensAnnotation.class);
		String entityMatch = "O";
		for (CoreLabel token : tokens) {
			String entitytag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
			if (entitytag.equals("LOCATION") || entitytag.equals("ORGANIZATION") || entitytag.equals("PERSON")) {
				if (entitytag.equals(entityMatch)) {
					result.set(result.size()-1, result.get(result.size()-1) + " " + token.get(CoreAnnotations.TextAnnotation.class));
				} else {
					result.add(token.getString(CoreAnnotations.TextAnnotation.class));
					entityMatch = entitytag;
				}
			} else {
				result.add(token.get(CoreAnnotations.LemmaAnnotation.class));
				entityMatch = "O";
			}
		}
		return result;
	}

	public ArrayList<ArrayList<String>> getLemmas(ArrayList<String> strings) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		for (String st : strings) {
			result.add(getLemmas(st));
		}
		return result;
	}

}

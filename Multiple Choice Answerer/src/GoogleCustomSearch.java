import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.*;
import org.jsoup.*;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.Customsearch.Cse.List;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

public class GoogleCustomSearch {
	private static String PROXYURL = "http://gimmeproxy.com/api/getProxy?minSpeed=200&website=google&get=true";
	private static String KEY = "AIzaSyCb1oeVhjuBHkOhGencERYGGcF1X76Zk9M";
	private static String googleURL = "https://www.google.com/search?q=";
	private static String googleBASE = "http://www.google.ca";
	private static String charset = "UTF-8";

	public static ArrayList<Document> search(String query) {
		Random r = new Random();
		int ua = r.nextInt(UserAgents.AGENTS.length);
		String agent = UserAgents.AGENTS[ua];
		System.out.println(agent);
		Elements links = null;
		Document doc = null;
		Map<String, String> cookies = null;
		try {
			Response res = Jsoup.connect(googleURL + query + "&nfpr=1").userAgent(agent)
					.timeout(12000).header("Accept-Language", "en").referrer("http://www.google.com")
					.ignoreContentType(true).execute();
			cookies = res.cookies();
			doc = Jsoup.connect(googleURL + query + "&nfpr=1").cookies(cookies).userAgent(agent)
					.timeout(12000).ignoreContentType(true).referrer("http://www.google.com").get();
			// check if google redirected us for spelling suggestions
			links = doc.select("h3.r > a");

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<Document> result = new ArrayList<Document>();

		for (Element link : links) {
			String title = link.text();
			// System.out.println(title);
			String url = link.select("a[href]").attr("href"); // Google returns
																// URLs in
																// format
			// System.out.println(url); //
			// "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".

			if (!url.startsWith("http")) {
				continue; // Ads/news/etc.
			}

			try {
				ua = r.nextInt(UserAgents.AGENTS.length);
				agent = UserAgents.AGENTS[ua];
				result.add(Jsoup.connect(url).userAgent(agent).get());
			} catch (IOException e) {
				// fail quietly
			}
		}

		return result;
	}

}

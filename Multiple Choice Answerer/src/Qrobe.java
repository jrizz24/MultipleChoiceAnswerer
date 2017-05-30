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
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.Timezone;
import com.machinepublishers.jbrowserdriver.UserAgent;

public class Qrobe {
	private static String PROXYURL = "http://gimmeproxy.com/api/getProxy?minSpeed=200&website=google&get=true";
	private static String KEY = "AIzaSyCb1oeVhjuBHkOhGencERYGGcF1X76Zk9M";
	private static String searchURL = "http://qrobe.it/search/?q=";
	private static String googleBASE = "http://www.google.ca";
	private static String charset = "UTF-8";

	public static ArrayList<Document> search(String query) {
		Random r = new Random();
		int ua = r.nextInt(UserAgents.AGENTS.length);
		String agent = UserAgents.AGENTS[ua];
		//System.out.println(agent);
		Elements links = null;
		
		JBrowserDriver driver = new JBrowserDriver(Settings.builder().
			      timezone(Timezone.AMERICA_NEWYORK).build());
		 driver.get(searchURL + query);
		 System.out.println(driver.getStatusCode());
		 Document doc = Jsoup.parse(driver.getPageSource());

		//System.out.println(doc.toString());
		links = doc.select("a#kNLink");


		ArrayList<Document> result = new ArrayList<Document>();
		for (int i = 0; i < 7; i++) {
			String title = links.get(i).text();
			// System.out.println(title);
			String url = links.get(i).attr("href"); // Google returns
																// URLs in
																// format
			//System.out.println(url); //
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
		driver.quit();

		return result;
	}

}

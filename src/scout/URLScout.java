package scout;

import org.jsoup.Connection;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.io.IOException;

public class URLScout implements Runnable{
	
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 "
			+ "(KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
	private UrlValidator urlValidator;
	private final URLScoutController controller;
	private final String url;
	
	public URLScout(final URLScoutController controller, final String url, UrlValidator urlValidator)
	{
		this.controller = controller;
		this.url = url;
		this.urlValidator = urlValidator;
	}
	
	public Document getHTML(String url)
	{
		try 
		{
			Connection connection = Jsoup.connect(url).userAgent(USER_AGENT); 
			Document html = connection.get();
			
			if (!connection.response().contentType().contains("text/html"))
			{
				return null; 
			}
			
			return html; 
		}
		catch(IOException ioe)
		{
			System.out.println("Unsuccessful HTTP request");
			return null; 
		}
	}
	
	private ArrayList<String> extractURLsFromDocument(Document html)
	{
		
		ArrayList<String> urls = new ArrayList<String>(); 
		
		Elements urlElements = html.select("a[href]"); 
		
		for (Element urlElement : urlElements)
		{
			String url = urlElement.absUrl("href");
			
			if (this.urlValidator.isValid(url)) {
				urls.add(url);
			}
		}
		
		return urls; 
	}
	
	public void run()
	{	
		Document html = this.getHTML(this.url);
		
		if (html != null)
		{	
			this.controller.processHTML(this.url, html);
			ArrayList<String> URLsOnPage = this.extractURLsFromDocument(html);
			ArrayList<String> cleanedURLs = processtext.URLCleaner.cleanURLs(URLsOnPage);
			this.controller.generateURLScouts(cleanedURLs);
		}
	}
}

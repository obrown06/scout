package scout;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedList;
import java.io.IOException;

import scout.ScoutControlCenter;
import scout.URLCleaner;

public class URLScout implements Runnable{
	
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 "
			+ "(KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
	private boolean successfulGET;
	public Document html;
	private final ScoutControlCenter controlCenter;
	private final String url;
	
	public URLScout(final ScoutControlCenter controlCenter, final String url)
	{
		this.controlCenter = controlCenter;
		this.url = url;
	}
	
	public boolean getHTML(String url)
	{
		try 
		{
			Connection connection = Jsoup.connect(url).userAgent(USER_AGENT); 
			Document htmlDocument = connection.get(); 
			this.html = htmlDocument; 
			
			if (!connection.response().contentType().contains("text/html"))
			{
				return false; 
			}
			
			return true; 
		}
		catch(IOException ioe)
		{
			System.out.println("Unsuccessful HTTP request");
			return false; 
		}
	}
	
	private ArrayList<String> findURLsOnPage()
	{
		if (this.html == null)
		{
			System.out.println("Called generateScouts(); on null doc");
			return null; 
		}
		
		ArrayList<String> urls = new ArrayList<String>(); 
		
		Elements urlElements = this.html.select("a[href]"); 
		
		for (Element urlElement : urlElements)
		{
			String url = urlElement.absUrl("href");
			urls.add(url);
		}
		
		return urls; 
	}
	
	public void run()
	{	
		if (this.getHTML(this.url))
		{
			this.controlCenter.computeSimilarityScoreAndProcessURL(this.url, this.html.toString());
			ArrayList<String> URLsOnPage = this.findURLsOnPage();
			ArrayList<String> cleanedURLs = scout.URLCleaner.cleanURLs(URLsOnPage);
			this.controlCenter.generateURLScouts(cleanedURLs);
		}
	}
}

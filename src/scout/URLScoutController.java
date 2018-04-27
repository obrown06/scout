package scout;

import org.jsoup.nodes.Document;

import org.apache.commons.validator.routines.UrlValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import scout.URLScout;

public class URLScoutController implements Runnable {
	private final int maxNURLsToCrawl = 10000;
	private final int NTHREADS = 20;
	public boolean isActive = true;
	
	private HashSet<String> queuedURLs = new HashSet<String>();
	private final ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
	
	private String baseURL;
	private String baseURLText;
	private ScoutServer server; 
	private ScoutingResultsRecord record = new ScoutingResultsRecord();
	
	private UrlValidator urlValidator = new UrlValidator();
	private NLPHelper nlpHelper = new NLPHelper(); 
	
	public URLScoutController(ScoutServer server, String baseURL) {
		this.baseURL = scout.URLCleaner.cleanURL(baseURL);
		this.queuedURLs.add(this.baseURL);
		this.server = server; 
	}
	
	public void run() {
		URLScout scout = new URLScout(this, this.baseURL, urlValidator);
		
		if (this.setBaseURLText(scout)) {
			this.executor.execute(scout);
		} else {
			this.shutdown();
		}
	}
	
	private boolean setBaseURLText(URLScout scout) {
		Document html = scout.getHTML(this.baseURL);
		
		if (html == null) {
			return false;
		}
		
		this.baseURLText = html.toString();
		return true; 
	}
	
	public void processHTML(String URL, Document html) {
		if (!this.isActive) {
			return; 
		}

		String htmlText = html.toString();
		double similarityScore = this.nlpHelper.cosineSimilarity(baseURLText, htmlText);
	
		
		synchronized(this) {
			this.record.update(URL, similarityScore);
			this.server.sendMessage(this, this.record);
			
			if (this.record.nURLsVisited() >= this.maxNURLsToCrawl) {
				this.shutdown();
			}
		}
		
	}
	
	public synchronized void generateURLScouts(ArrayList<String> URLs) {
		if (!this.isActive) {
			return; 
		}
		
		for (String URL : URLs) {
			if (this.queuedURLs.add(URL)) {
				this.executor.execute(new URLScout(this, URL, this.urlValidator));
			}
			
		}
	}
	
	public synchronized void shutdown() {
		if (!this.isActive) {
			return; 
		}
		
		this.isActive = false; 
		this.executor.shutdownNow();
		Thread.currentThread().interrupt();
	}
	
	public static void main(String [] args) {
		String URL = "https://en.wikipedia.org/wiki/Lord_Voldemort";
		ScoutServer server = new ScoutServer(); 
		
		URLScoutController center = new URLScoutController(server, URL);
		center.run();
	}
	
}

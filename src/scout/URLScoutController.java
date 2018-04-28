package scout;

import org.jsoup.nodes.Document;

import processtext.NLPHelper;

import org.apache.commons.validator.routines.UrlValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import scout.URLScout;
import socket.ScoutServer;

public class URLScoutController implements Runnable {
	private final int maxNURLsToCrawl = 3000;
	private final int NTHREADS = 20;
	public boolean isActive = true;
	
	private HashSet<String> visitedURLs = new HashSet<String>();
	private HashSet<String> URLFutures = new HashSet<String>();
	private final ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
	
	private String baseURL;
	private String baseURLText;
	private ScoutServer server; 
	private ScoutingResultsRecord record = new ScoutingResultsRecord();
	
	private UrlValidator urlValidator = new UrlValidator();
	private NLPHelper nlpHelper = new NLPHelper(); 
	
	public URLScoutController(ScoutServer server, String baseURL) {
		this.baseURL = processtext.URLCleaner.cleanURL(baseURL);
		this.visitedURLs.add(this.baseURL);
		this.URLFutures.add(this.baseURL);
		this.server = server; 
	}
	
	public void run() {
		URLScout scout = new URLScout(this, this.baseURL, urlValidator);
		
		if (this.setBaseURLText(scout)) {
			this.executor.execute(scout);
		} else {
			this.server.sendMessage(this, "FAILURE");
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
			
			if (this.record.nURLsVisited() >= this.maxNURLsToCrawl) {
				this.shutdown();
			}
		}
		
		this.server.sendScoutingResultsRecordMessage(this, this.record);
		
	}
	
	public void generateURLScouts(ArrayList<String> URLs) {
		if (!this.isActive) {
			return; 
		}
		
		for (String URL : URLs) {
			if (this.addURLToVisited(URL)) {
				this.URLFutures.add(URL);
				this.executor.execute(new URLScout(this, URL, this.urlValidator));
			}
			
		}
	}
	
	private synchronized boolean addURLToVisited(String URL) {
		return this.visitedURLs.add(URL);
	}
	
	public void removeURLFromFutures(String URL) {
		this.URLFutures.remove(URL);
		
		if (URLFutures.size() == 0) {
			this.shutdown(); 
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

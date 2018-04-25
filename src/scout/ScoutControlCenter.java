package scout;

import org.jsoup.nodes.Document;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.websocket.Session;
import java.io.IOException;

import scout.URLScout;

public class ScoutControlCenter implements Runnable {
	private final int maxNURLsToCrawl = 500000;
	private final int NTHREADS = 10;
	private final int nTopURLsToRecord = 10; 
	
	private HashSet<String> visitedURLs = new HashSet<String>();
	private int nURLsVisited = 0; 
	private final ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
	
	private Session session;
	private String baseURL;
	private String baseText; 
	
	private ArrayList<String> topURLs = new ArrayList<String>();
	private ArrayList<Double> topSimilarityScores = new ArrayList<Double>();
	
	public ScoutControlCenter(String baseURL, Session session) {
		this.baseURL = baseURL;
		this.session = session;
	}
	
	public void run() {
		URLScout scout = new URLScout(this, baseURL);
		this.setBaseText(scout);
		
		this.executor.execute(scout);
	}
	
	private void setBaseText(URLScout scout) {
		if (!scout.getHTML(this.baseURL)) {
			this.shutdown();
		}
		
		this.baseText = scout.html.toString();
	}
	
	public void computeSimilarityScoreAndProcessURL(String URL, String htmlString) {
		double similarityScore = scout.NLPHelper.computeSimilarityScore(this.baseText, htmlString);
		this.processURL(URL, similarityScore);
	}
	
	private synchronized void processURL(String URL, double similarityScore) {
		if (nURLsVisited >= maxNURLsToCrawl) {
			this.shutdown();
		}
		
		nURLsVisited++; 
		
		int insertionIndex = this.computeInsertionIndex(topSimilarityScores, similarityScore); 
		
		if (insertionIndex > nTopURLsToRecord) {
			this.insertTopURL(URL, similarityScore, insertionIndex);
		}
		
	}
	
	private int computeInsertionIndex(ArrayList<Double> sortedList, double doubleToInsert) {
		int index = Collections.binarySearch(sortedList, doubleToInsert);
		
		index = Math.abs(index);
		
		if (index == sortedList.size() + 1) {
			index = 0; 
		}
		
		return index; 
	}
	
	private void insertTopURL(String URL, Double similarityScore, int insertionIndex) {
		this.topURLs.add(insertionIndex, URL);
		this.topSimilarityScores.add(insertionIndex, similarityScore);
		
		if (topURLs.size() > nTopURLsToRecord) {
			topURLs.remove(topURLs.size() - 1);
			topSimilarityScores.remove(topSimilarityScores.size() - 1);
		}
	}
	
	public synchronized void generateURLScouts(ArrayList<String> URLs) {
		
		for (String URL : URLs) {
			if (visitedURLs.add(URL)) {
				URLScout scout = new URLScout(this, URL);
				this.executor.execute(scout);
			}
		}
	}
	
	public synchronized void shutdown() {
		closeSession();
		shutdownThreads();
	}
	
	private void closeSession() {
		try {
	        this.session.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	private void shutdownThreads() {
		this.executor.shutdownNow();
		Thread.currentThread().interrupt();
	}
	
}

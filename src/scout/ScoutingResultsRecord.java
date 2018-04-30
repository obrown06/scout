package scout;

import java.util.ArrayList;
import java.util.Collections;


public class ScoutingResultsRecord {
	private ArrayList<String> urls = new ArrayList<String>();
	private ArrayList<Double> scores = new ArrayList<Double>();
	private final int MAX_SIZE = 10; 
	private int nURLsVisited = 0; 
	
	public void update(String URL, double similarityScore) {	
		this.insert(URL, similarityScore);
		this.nURLsVisited++; 
	}
	
	public int nURLsVisited() {
		return this.nURLsVisited; 
	}
	
	private void insert(String URL, double similarityScore) {
		int insertionIndex = this.computeInsertionIndex(similarityScore);
		this.urls.add(insertionIndex, URL);
		this.scores.add(insertionIndex, similarityScore);
		
		
		if (this.urls.size() > this.MAX_SIZE) {
			this.urls.remove(this.urls.size() - 1);
			this.scores.remove(this.scores.size() - 1);
		}
	}
	
	private int computeInsertionIndex(double scoreToInsert) {
		int index = Collections.binarySearch(this.scores, scoreToInsert);
		
		if (index < 0) {
			index = Math.abs(index + 1);
		}
		
		return index; 
	}
}

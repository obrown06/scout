package scout;

import java.util.ArrayList;
import java.util.Collections;


public class ScoutingResultsRecord {
	private ArrayList<String> mostSimilarURLs = new ArrayList<String>();
	private ArrayList<Double> mostSimilarURLsScores = new ArrayList<Double>();
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
		System.out.println("TRYING TO INSERT: " + URL + " WITH SIMILARITY SCORE: " + Double.toString(similarityScore));
		System.out.println("mostSimilarURLs Size: " + mostSimilarURLs.size());
		int insertionIndex = this.computeInsertionIndex(similarityScore);
		System.out.println("Insertion index: " + insertionIndex);
		this.mostSimilarURLs.add(insertionIndex, URL);
		this.mostSimilarURLsScores.add(insertionIndex, similarityScore);
		
		
		if (this.mostSimilarURLs.size() > this.MAX_SIZE) {
			this.mostSimilarURLs.remove(this.mostSimilarURLs.size() - 1);
			this.mostSimilarURLsScores.remove(this.mostSimilarURLsScores.size() - 1);
		}
	}
	
	private int computeInsertionIndex(double scoreToInsert) {
		int index = Collections.binarySearch(this.mostSimilarURLsScores, scoreToInsert);
		
		if (index < 0) {
			index = Math.abs(index + 1);
		}
		
		return index; 
	}
}

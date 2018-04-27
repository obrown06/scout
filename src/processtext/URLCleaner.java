package processtext;
import java.util.ArrayList;

public class URLCleaner {
	
	public static ArrayList<String> cleanURLs(ArrayList<String> URLs) {
		for (int i = 0; i < URLs.size(); i++) {
			String URL = URLs.get(i);
			URLs.set(i, cleanURL(URL));
		}
		
		return URLs;
	}
	
	private static String removeQueryStrings(String URL) {
		int end; 
		
		if (URL.indexOf("?") > 0) {
	        end = URL.indexOf("?");
	    } else if (URL.indexOf("#") > 0) {
	        end = URL.indexOf("#");
	    } else {
	        end = URL.length();
	    }
		
		return URL.substring(0, end);
	}
	
	private static String removeEndSlash(String URL) {
		if (URL.charAt(URL.length() - 1) == '/') {
	    	URL = URL.substring(0, URL.length() - 1);
	    }
		
		return URL;
	}
	
	private static String standardizeHTTPPrefix(String URL) {
		StringBuilder sb = new StringBuilder(URL);
		
		if (!sb.substring(0, 4).equals("http")) {
			sb.insert(0,  "http://");
		} else if (URL.substring(0, 5).equals("https")) {
			sb.deleteCharAt(4);
		} 
		
		return sb.toString();
	}
	
	public static String cleanURL(String URL) {
		URL = removeQueryStrings(URL);
		URL = removeEndSlash(URL);
		URL = standardizeHTTPPrefix(URL);

	    return URL;
	}
}

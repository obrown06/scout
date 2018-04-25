package scout;
import java.util.ArrayList;

public class URLCleaner {
	
	public static ArrayList<String> cleanURLs(ArrayList<String> URLs) {
		for (int i = 0; i < URLs.size(); i++) {
			String URL = URLs.get(i);
			URLs.set(i, clean(URL));
		}
		
		return URLs;
	}
	
	private static String clean (String URL) {
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
}

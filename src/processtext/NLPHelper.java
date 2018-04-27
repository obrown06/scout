package processtext;
import org.apache.commons.text.similarity.CosineDistance;

public class NLPHelper {
	
	CosineDistance dist = new CosineDistance();
	
	public double cosineSimilarity(String s1, String s2) {
		return this.dist.apply(s1,  s2);
	}
}

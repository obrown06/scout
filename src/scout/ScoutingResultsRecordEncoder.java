package scout;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import com.google.gson.Gson;
import javax.websocket.EncodeException;

public class ScoutingResultsRecordEncoder implements Encoder.Text<ScoutingResultsRecord>{
	private Gson gson = new Gson();
	
	@Override
	public void init(EndpointConfig ec) { }
	
	@Override
	public void destroy() { }
	
	@Override 
	public String encode(ScoutingResultsRecord record) throws EncodeException {
		return gson.toJson(record);
	}
}

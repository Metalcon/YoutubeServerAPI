package freebaseclient;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class facebookRssFeedFinder {

	public static void main(String[] args) throws IOException, ParseException{
    HttpTransport httpTransport = new NetHttpTransport();
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    //TODO: implement reading name from file.
    String bandName = "IronMaiden";
    GenericUrl graphUrl = new GenericUrl("https://graph.facebook.com/" + bandName);
    HttpRequest request = requestFactory.buildGetRequest(graphUrl);
    HttpResponse httpResponse = request.execute();
    String feedId = parseResponse(httpResponse);
    GenericUrl RssFeedUrl = new GenericUrl("https://www.facebook.com/feeds/page.php?id=" + feedId + "&format=rss20");
    //TODO: maybe write url to file?
    System.out.println(RssFeedUrl);
}
	
	private static String parseResponse(HttpResponse httpResponse) throws ParseException, IOException {
		JSONObject response = new JSONObject();
		JSONParser jsonparser = new JSONParser();
		String responseId = null;
		try {response = (JSONObject) jsonparser.parse(httpResponse
					.parseAsString());
		responseId = response
				.get("id").toString();
		} catch (ClassCastException ce) {
			System.err
					.println("Typecast failed. Response is probably broken. Can be caused by bad request");
		}
		return responseId;
	}
}
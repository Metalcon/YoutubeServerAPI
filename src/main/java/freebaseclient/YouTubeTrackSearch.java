package freebaseclient;

import java.io.FileInputStream;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class YouTubeTrackSearch {
	 public static Properties properties = new Properties();
	  public static void main(String[] args) {
	    try {
	      properties.load(new FileInputStream("freebase.properties"));
	      HttpTransport httpTransport = new NetHttpTransport();
	      HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
	      JSONParser parser = new JSONParser();
	      GenericUrl url = new GenericUrl("https://www.googleapis.com/youtube/v3/search");
	      url.put("part", "snippet");
	      url.put("topicId" , "/m/01qnbj4" );
	      url.put("key", properties.get("API_KEY"));
	      HttpRequest request = requestFactory.buildGetRequest(url);
	      System.out.println(url);
	      HttpResponse httpResponse = request.execute();
	      JSONObject response = (JSONObject)parser.parse(httpResponse.parseAsString());
	      System.out.println(response.toString());
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }}
}

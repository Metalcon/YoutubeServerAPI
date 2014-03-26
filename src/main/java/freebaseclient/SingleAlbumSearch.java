package freebaseclient;

import java.io.FileInputStream;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class SingleAlbumSearch {
	public static Properties properties = new Properties();
	  public static void main(String[] args) {
	    try {
	      properties.load(new FileInputStream("freebase.properties"));
	      HttpTransport httpTransport = new NetHttpTransport();
	      HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
	      JSONParser parser = new JSONParser();
	      GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
//	      url.put("mid", "/m/02g_zp");
//	      url.put("kind", "/music/artist/album");
//	      url.put("que", "");
	      //TODO: implement reading mid from file
	      String query = "[{\"mid\":\"/m/02g_zp\",\"/music/artist/album\":[{\"name\":null , \"mid\":null}]}]";
	      url.put("query", query);

	      System.out.println("my API-Key: " + properties.get("API_KEY"));
	      url.put("key", properties.get("API_KEY"));
	      HttpRequest request = requestFactory.buildGetRequest(url);
	      System.out.println(url);
	      HttpResponse httpResponse = request.execute();
	      JSONObject response = (JSONObject)parser.parse(httpResponse.parseAsString());
	      JSONArray candidates = (JSONArray)response.get("candidate");
//	      for (Object candidate : candidates) {
//	        System.out.print(JsonPath.read(candidate,"$.mid").toString()+" | " + JsonPath.read(candidate,"$.name").toString()+" | " + JsonPath.read(candidate,"$.notable.name").toString() + " (");
//	        System.out.println(JsonPath.read(candidate,"$.confidence").toString()+")");
//	      }
	      System.out.println(response.toString());
	      //TODO: implement extracting information (and writing to file)
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }
	  }

}

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
import com.jayway.jsonpath.JsonPath;



public class SingleReconciliationMetalcon {

  public static Properties properties = new Properties();
  public static void main(String[] args) {
    try {
      properties.load(new FileInputStream("freebase.properties"));
      HttpTransport httpTransport = new NetHttpTransport();
      HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
      JSONParser parser = new JSONParser();
      GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/reconcile");
      url.put("name", "Prometheus");
      url.put("kind", "/film/film");
      url.put("prop", "/film/film/directed_by:Ridley Scott");
      System.out.println("my API-Key: " + properties.get("API_KEY"));
      url.put("key", properties.get("API_KEY"));
      HttpRequest request = requestFactory.buildGetRequest(url);
      HttpResponse httpResponse = request.execute();
      JSONObject response = (JSONObject)parser.parse(httpResponse.parseAsString());
      JSONArray candidates = (JSONArray)response.get("candidate");
//      for (Object candidate : candidates) {
//        System.out.print(JsonPath.read(candidate,"$.mid").toString()+" | " + JsonPath.read(candidate,"$.name").toString()+" | " + JsonPath.read(candidate,"$.notable.name").toString() + " (");
//        System.out.println(JsonPath.read(candidate,"$.confidence").toString()+")");
//      }
      System.out.println(response.toString());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}

package freebaseclient;

import java.io.FileInputStream;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.util.Properties;



public class BulkReconciliationMetalcon {


	  public static Properties properties = new Properties();
	  public static void main(String[] args) {
	    try { properties.load(new FileInputStream("freebase.properties"));
	      HttpTransport httpTransport = new NetHttpTransport();
	      HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
	      JSONParser parser = new JSONParser();
	      GenericUrl url = new GenericUrl("https://www.googleapis.com/rpc");
	      //GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/reconcile");
	      
	      String[] bandList = {"Metallica","Megadeth","Slayer","Anthrax","Deathstars","in legend","lustkind"};
	      
	      JSONArray requestBody = new JSONArray();
	      
	      for (int i = 0; i < bandList.length; i++) {
			
		
	      JSONObject requestBodyContent = new JSONObject(); 
	      //prepare request metadata
	      requestBodyContent.put("jsonrpc", "2.0");
	      
	    //TODO: find out what to put here
	      requestBodyContent.put("id", i);
	      requestBodyContent.put("method", "freebase.reconcile");
	      requestBodyContent.put("apiVersion", "v1");
	      
	      
	      
	      JSONObject exampleRequestContent = new JSONObject();
	      exampleRequestContent.put("name", bandList[i]);
	      
	      JSONArray exampleRequestContentKind = new JSONArray();
	      exampleRequestContentKind.add("/music/artist");
	      exampleRequestContent.put("type", exampleRequestContentKind);
	      
	      JSONArray exampleRequestEmpty = new JSONArray();
	      
	      //to request certain kinds of content, simply write it as a key with null Value
	 //     exampleRequestContent.put("/music/album", exampleRequestEmpty);
	      JSONArray exampleRequestContentProp = new JSONArray();
	      exampleRequestContentProp.add("/music/artist/genre:Heavy metal");
	     exampleRequestContent.put("prop", exampleRequestContentProp);
	      
	      //API-Key is needed for every single concept... wtf?!
	      exampleRequestContent.put("key", properties.get("API_KEY"));
	      
	      requestBodyContent.put("params" , exampleRequestContent);
	      	      
	      requestBody.add(requestBodyContent);
	      }
	      String requestBodyString = requestBody.toString();
	      System.out.println(requestBodyString);
	      
	      //TODO: read textfile from disc instead
//	      String requestBody = "[{\"jsonrpc\" : \"2.0\",\"id\" : \"<request-id>\",\"method\" : \"freebase.reconcile\",\"apiVersion\" : \"v1\",\"params\" : {\"name\" : \"Prometheus\",\"kind\" : [ \"/film/film\" ],\"prop\" : [ \"/film/film/directed_by:Ridley+Scott\" ],\"key\" : \"" + properties.get("API_KEY") + "\"}}]";
          HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString("application/json", requestBodyString));
//	    
          
          //TODO: parse response (JSONArray to String)
          HttpResponse httpResponse = request.execute();
          
          
          
          //JSONArray response = (JSONArray)parser.parse(httpResponse.parseAsString());
          //JSONArray candidates = (JSONArray)response.get("candidate");
//          for (Object candidate : candidates) {
//            System.out.print(JsonPath.read(candidate,"$.mid").toString()+" | " + JsonPath.read(candidate,"$.name").toString()+" | " + JsonPath.read(candidate,"$.notable.name").toString() + " (");
//            System.out.println(JsonPath.read(candidate,"$.confidence").toString()+")");
//          }
          System.out.println(httpResponse.parseAsString());
	      
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }
	  }
}

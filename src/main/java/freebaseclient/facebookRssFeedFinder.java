package freebaseclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import com.jayway.jsonpath.JsonPath;

public class facebookRssFeedFinder {
	public static Properties properties = new Properties();

	public static void main(String[] args) throws IOException, ParseException {

		String bandMid = "/m/014_xj";
		List<GenericUrl> socialMediaUrls = new ArrayList<GenericUrl>();
		socialMediaUrls = getBandSocialMediaPresence(bandMid);
		getFacebookRssUrl(socialMediaUrls);

	}

	private static List<GenericUrl> getBandSocialMediaPresence(String bandMid)
			throws FileNotFoundException, IOException, ParseException {
		properties.load(new FileInputStream("freebase.properties"));
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/freebase/v1/topic" + bandMid + "?filter=/common/topic/social_media_presence");
	//	url.put("key", properties.get("API_KEY"));
	//	System.out.println(url);
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		JSONObject responseProperty = (JSONObject) response.get("property");
		JSONObject responsePropertyValues = (JSONObject) responseProperty.get("/common/topic/social_media_presence");
		JSONArray pages = (JSONArray) responsePropertyValues.get("values");
		
		List<GenericUrl> resultList = new ArrayList<GenericUrl>();
		for (Object page : pages) {
			GenericUrl pageUrl = new GenericUrl(JsonPath.read(page, "$.value").toString());
			resultList.add(pageUrl);
		}
		
		return resultList;
	}

	private static GenericUrl getFacebookRssUrl(List<GenericUrl> socialMediaUrls)
			throws IOException, ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		
		List<GenericUrl> filteredList = new ArrayList<GenericUrl>();
		
		for (int i = 0; i < socialMediaUrls.size(); i++) {
			if (socialMediaUrls.get(i).toString().contains("facebook")){
				filteredList.add(socialMediaUrls.get(i));
				
			}
		}
		
		System.out.println(filteredList.get(0));
		
		String bandName = filteredList.get(0).toString().split("facebook.com/")[1];
		System.out.println(bandName);
		// FIXME: adapt to new implementation (get bandName from facebook-url)
		GenericUrl graphUrl = new GenericUrl("https://graph.facebook.com/"
				+ bandName);
		System.out.println(graphUrl);
		HttpRequest request = requestFactory.buildGetRequest(graphUrl);
		HttpResponse httpResponse = request.execute();
		String feedId = parseFacebookResponse(httpResponse);
		GenericUrl RssFeedUrl = new GenericUrl(
				"https://www.facebook.com/feeds/page.php?id=" + feedId
						+ "&format=rss20");
		
		System.out.println(RssFeedUrl);
		
		return RssFeedUrl;
	}

	private static String parseFacebookResponse(HttpResponse httpResponse)
			throws ParseException, IOException {
		JSONObject response = new JSONObject();
		JSONParser jsonparser = new JSONParser();
		String responseId = null;
		try {
			response = (JSONObject) jsonparser.parse(httpResponse
					.parseAsString());
			responseId = response.get("id").toString();
		} catch (ClassCastException ce) {
			System.err
					.println("Typecast failed. Response is probably broken. Can be caused by bad request");
		}
		return responseId;
	}
}
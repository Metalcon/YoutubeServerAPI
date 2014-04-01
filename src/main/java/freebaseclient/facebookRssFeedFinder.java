package freebaseclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;

public class facebookRssFeedFinder {
	public static Properties properties = new Properties();

	public static void main(String[] args) throws IOException, ParseException {

		String bandMid = "/m/04rcr";
		List<GenericUrl> socialMediaUrls = new ArrayList<GenericUrl>();
		socialMediaUrls = getBandSocialMediaPresence(bandMid);
		List<GenericUrl> otherWebpagesUrls = new ArrayList<GenericUrl>();
		otherWebpagesUrls = getOtherWebpages(bandMid);
		if (socialMediaUrls != null) {
			getFacebookRssUrl(socialMediaUrls);
			getTwitterTimelineRss(socialMediaUrls);
			getLastfmEvents(socialMediaUrls);
		} else {
			System.out.println("No social media presence found");
		}
		getYoutubeClips(otherWebpagesUrls);

	}

	private static List<GenericUrl> filterUrlList(List<GenericUrl> webPageUrls,
			String filter) {
		List<GenericUrl> filteredList = new ArrayList<GenericUrl>();

		for (int i = 0; i < webPageUrls.size(); i++) {
			if (webPageUrls.get(i).toString().contains(filter)) {
				filteredList.add(webPageUrls.get(i));
			}
		}
		return filteredList;
	}

	private static List<String> readInputFile(String inputFile) {
		String line;
		List<String> listArray = new ArrayList<String>();
		try {
			FileReader fileReader = new FileReader(inputFile + ".csv");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			for (int i = 0; (line = bufferedReader.readLine()) != null; ++i) {
				listArray.add(line);
			}
		} catch (IOException e) {
			System.err.println("Problem reading List!");
			e.printStackTrace();
		}
		return listArray;
	}

	public static void writeToFile(String dataToSave, String saveFileName)
			throws IOException {
		File outputFile = new File(saveFileName + ".csv");
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(dataToSave);
		bufferedWriter.close();
	}

	private static void getYoutubeClips(List<GenericUrl> otherWebpagesUrls)
			throws FileNotFoundException, IOException, ParseException {
		// freebase-API-Key == google-API-Key
		properties.load(new FileInputStream("freebase.properties"));

		List<GenericUrl> filteredList = filterUrlList(otherWebpagesUrls,
				"http://www.youtube.com/user");
		if (filteredList.size() > 0) {
			String channelName = filteredList.get(0).toString()
					.split("http://www.youtube.com/user/")[1];
			System.out.println("Name of youtube channel : " + channelName);
			// it seems like there is never more than one youtube-channel per
			// band.
			// If this is false, a way to decide which one to take has to be
			// found
			if (filteredList.size() > 1)
				System.out
						.println("This band has more than one Youtube-Channel!!!");

			String youtubeChannelId = getYoutubeChannelId(channelName);
			System.out.println("Channel ID: " + youtubeChannelId);
			List<String> videoClipIds = getVideoClipIDs(youtubeChannelId);
		} else {
			System.out.println("no youtube channel found");
		}
		// https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=UUaisXKBdNOYqGr2qOXCLchQ
	}

	private static List<String> getVideoClipIDs(String youtubeChannelId)
			throws IOException, ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/youtube/v3/playlistItems");
		url.put("part", "snippet");
		url.put("playlistId", youtubeChannelId);
		url.put("key", properties.get("API_KEY"));
		url.put("maxResults", "50");
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();

		List<String> resultList = new ArrayList<String>();
		List<String> lockedList = new ArrayList<String>();

		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());

		JSONArray responseItems = (JSONArray) response.get("items");

		for (int i = 0; i < responseItems.size(); ++i) {
			JSONObject responseItemsEntry = (JSONObject) responseItems.get(i);
			JSONObject responseItemsEntrySnippet = (JSONObject) responseItemsEntry
					.get("snippet");
			JSONObject responseItemsEntrySnippetResourceid = (JSONObject) responseItemsEntrySnippet
					.get("resourceId");
			String videoID = responseItemsEntrySnippetResourceid.get("videoId")
					.toString();
			boolean checkRegionLock = getRegionLockInfo(videoID);
			if (checkRegionLock) {
				resultList.add(videoID);
			} else {
				lockedList.add(videoID);
			}
		}
		if (response.containsKey("nextPageToken")) {
			String nextPageToken = response.get("nextPageToken").toString();
			System.out.println("Next Page Token: " + nextPageToken);
			getNextPage(youtubeChannelId, nextPageToken, resultList, lockedList);
		}
		System.out.println("List of available Videos(" + resultList.size()
				+ ")");
		System.out.println(resultList);
		System.out.println("List of locked Videos (" + lockedList.size() + ")");
		System.out.println(lockedList);
		return resultList;
	}

	private static void getNextPage(String youtubeChannelId,
			String nextPageToken, List<String> resultList,
			List<String> lockedList) throws IOException, ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/youtube/v3/playlistItems");
		url.put("part", "snippet");
		url.put("playlistId", youtubeChannelId);
		url.put("key", properties.get("API_KEY"));
		url.put("maxResults", "50");
		url.put("pageToken", nextPageToken);
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());

		JSONArray responseItems = (JSONArray) response.get("items");

		for (int i = 0; i < responseItems.size(); ++i) {
			JSONObject responseItemsEntry = (JSONObject) responseItems.get(i);
			JSONObject responseItemsEntrySnippet = (JSONObject) responseItemsEntry
					.get("snippet");
			JSONObject responseItemsEntrySnippetResourceid = (JSONObject) responseItemsEntrySnippet
					.get("resourceId");
			String videoID = responseItemsEntrySnippetResourceid.get("videoId")
					.toString();
			boolean checkRegionLock = getRegionLockInfo(videoID);
			if (checkRegionLock) {
				resultList.add(videoID);
			} else {
				lockedList.add(videoID);
			}

		}
		if (response.containsKey("nextPageToken")) {
			String newNextPageToken = response.get("nextPageToken").toString();
			getNextPage(youtubeChannelId, newNextPageToken, resultList,
					lockedList);
		}

	}

	private static boolean getRegionLockInfo(String videoID)
			throws IOException, ParseException {
		// https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id=91EAEKF2plE&key={YOUR_API_KEY}
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/youtube/v3/videos");
		url.put("part", "contentDetails");
		url.put("id", videoID);
		url.put("key", properties.get("API_KEY"));
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		JSONArray responseItems = (JSONArray) response.get("items");
		JSONObject responseItemsEntry = (JSONObject) responseItems.get(0);
		JSONObject responseItemsEntryContentdetails = (JSONObject) responseItemsEntry
				.get("contentDetails");
		if (responseItemsEntryContentdetails.containsKey("regionRestriction")) {
			return false;
		} else {
			return true;
		}
	}

	// TODO: use this method for refactoring
	private static String getYoutubeChannelId(String channelName)
			throws IOException, ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/youtube/v3/channels");

		url.put("key", properties.get("API_KEY"));
		url.put("forUsername", channelName);
		url.put("part", "contentDetails");
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();

		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		JSONArray responseItems = (JSONArray) response.get("items");
		JSONObject responseItemsEntry = (JSONObject) responseItems.get(0);
		JSONObject responseItemsEntryContentdetails = (JSONObject) responseItemsEntry
				.get("contentDetails");
		JSONObject responseItemsEntryContentdetailsRelatedplaylists = (JSONObject) responseItemsEntryContentdetails
				.get("relatedPlaylists");
		return responseItemsEntryContentdetailsRelatedplaylists.get("uploads")
				.toString();
	}

	private static List<GenericUrl> getOtherWebpages(String bandMid)
			throws FileNotFoundException, IOException, ParseException {
		properties.load(new FileInputStream("freebase.properties"));
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/freebase/v1/topic" + bandMid);
		// url.put("key", properties.get("API_KEY"));
		url.put("filter", "/common/topic/topic_equivalent_webpage");
		url.put("limit", "9001");
		System.out.println(url);
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		JSONObject responseProperty = (JSONObject) response.get("property");
		JSONObject responsePropertyValues = (JSONObject) responseProperty
				.get("/common/topic/topic_equivalent_webpage");
		JSONArray pages = (JSONArray) responsePropertyValues.get("values");

		List<GenericUrl> resultList = new ArrayList<GenericUrl>();
		for (Object page : pages) {
			GenericUrl pageUrl = new GenericUrl(JsonPath.read(page, "$.value")
					.toString());
			resultList.add(pageUrl);
		}
		return resultList;
	}

	// Return type might still be changed!!
	private static List<JSONObject> getLastfmEvents(
			List<GenericUrl> socialMediaUrls) throws FileNotFoundException,
			IOException, ParseException {
		properties.load(new FileInputStream("lastfm.properties"));

		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		GenericUrl url = new GenericUrl("http://ws.audioscrobbler.com/2.0/");
		List<GenericUrl> filteredList = filterUrlList(socialMediaUrls,
				"http://www.last.fm/music/");

		if (filteredList.size() > 0) {
			System.out.println(filteredList.get(0));
			String bandName = filteredList.get(0).toString()
					.split("http://www.last.fm/music/")[1];
			// http://ws.audioscrobbler.com/2.0/?method=artist.getevents&artist=Cher&api_key=APIKEY&format=json
			url.put("method", "artist.getevents");

			url.put("artist", bandName);
			url.put("api_key", properties.get("API_KEY"));
			url.put("format", "json");
			url.put("autocorrect", "1");
			// url.put("festivalsonly", "1");

			HttpRequest request = requestFactory.buildGetRequest(url);
			HttpResponse httpResponse = request.execute();

			JSONParser parser = new JSONParser();
			JSONObject response = (JSONObject) parser.parse(httpResponse
					.parseAsString());
			JSONObject responseEvents = (JSONObject) response.get("events");
			JSONArray responseEventsEvent = (JSONArray) responseEvents
					.get("event");
			List<JSONObject> result = new ArrayList<JSONObject>();
			for (Object event : responseEventsEvent) {
				result.add((JSONObject) event);
				// TODO: change to only extract the relevant data (title,
				// artists, venue, startDate, endDate (if available), website )
				// maybe extract to an appropriate Java-Class instead of JSON
			}

			return result;
		}
		return null;
	}

	private static List<GenericUrl> getTwitterTimelineRss(
			List<GenericUrl> socialMediaUrls) throws FileNotFoundException,
			IOException {
		properties.load(new FileInputStream("twitter.properties"));

		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		GenericUrl url = new GenericUrl(
				"https://api.twitter.com/1.1/statuses/user_timeline.json");

		List<GenericUrl> filteredList = filterUrlList(socialMediaUrls,
				"twitter");

		if (filteredList.size() > 0) {
			System.out.println(filteredList.get(0));

			String bandName = filteredList.get(0).toString()
					.split("twitter.com/")[1];

			// FIXME: this request does not work (response: Bad Authentication
			// data)
			// TODO: find out how to authenticate correctly (API documentation
			// is fubar!)
			// url.put("screen_name", bandName);
			// url.put("oauth_token", properties.get("ACCESS_TOKEN"));
			// url.put("oauth_consumer_key", properties.get("API_KEY"));
			// url.put("oauth_signature_method",
			// properties.get("OAUTH_METHOD"));
			// url.put("oauth_version","1.0");
			// System.out.println(bandName);
			// System.out.println(url);
			// HttpRequest request = requestFactory.buildGetRequest(url);
			// HttpResponse httpResponse = request.execute();
			// System.out.println(httpResponse.parseAsString());
			return null;
		}

		else {
			System.out.println("no Twitter!");
			return null;
		}

	}

	private static List<GenericUrl> getBandSocialMediaPresence(String bandMid)
			throws FileNotFoundException, IOException, ParseException {
		properties.load(new FileInputStream("freebase.properties"));
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();
		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/freebase/v1/topic" + bandMid
						+ "?filter=/common/topic/social_media_presence");
		// url.put("key", properties.get("API_KEY"));
		// System.out.println(url);
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		try {
			JSONParser parser = new JSONParser();
			JSONObject response = (JSONObject) parser.parse(httpResponse
					.parseAsString());
			JSONObject responseProperty = (JSONObject) response.get("property");

			JSONObject responsePropertyValues = (JSONObject) responseProperty
					.get("/common/topic/social_media_presence");
			JSONArray pages = (JSONArray) responsePropertyValues.get("values");

			List<GenericUrl> resultList = new ArrayList<GenericUrl>();
			for (Object page : pages) {
				GenericUrl pageUrl = new GenericUrl(JsonPath.read(page,
						"$.value").toString());
				resultList.add(pageUrl);
			}
			return resultList;
		} catch (NullPointerException E) {
			return null;
		}

	}

	private static GenericUrl getFacebookRssUrl(List<GenericUrl> socialMediaUrls)
			throws IOException, ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		List<GenericUrl> filteredList = filterUrlList(socialMediaUrls,
				"facebook");

		try {
			System.out.println(filteredList.get(0));

			String bandName = filteredList.get(0).toString()
					.split("facebook.com/")[1];
			System.out.println(bandName);
			// FIXME: adapt to new implementation (get bandName from
			// facebook-url)
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

		catch (HttpResponseException E) {
			System.out.println("no facebook!");
			return null;
		}
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
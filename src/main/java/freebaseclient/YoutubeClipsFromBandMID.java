package freebaseclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import api.YoutubeMetaData;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * 
 * @author tobi
 * 
 */

public class YoutubeClipsFromBandMID {

	public static Properties properties = new Properties();

	public static void main(String[] args) throws IOException, ParseException {
		// Placeholder for file input MID
		String bandMid = "/m/04473z";
		String title = "Beyond the Dark Sun";
		// List<YoutubeMetaData> list = getVideosForTrackAndBand(bandMid,
		// title);
		List<YoutubeMetaData> container = new ArrayList<YoutubeMetaData>();
		properties.load(new FileInputStream("freebase.properties"));

		JSONObject response = youtubeSongSearch(50, title, bandMid);
		processingSearchResults(response, container);
		System.out.println(container.get(0).getYoutubeID());

	}

	/**
	 * @param maxResults
	 *            specifies the amount of results you get (1-50)
	 * @param query
	 *            The search term (in our case song title)
	 * @param topicID
	 *            The topicID from freebase (from band)
	 * 
	 * @return returns JSONObject with searchresults
	 * @throws IOException
	 * @throws ParseException
	 * 
	 *             This method is a generic youtube search. It needs a band
	 *             topic ID from freebase and retrieves a list of songs to a
	 *             query of a songTitle from that band
	 */

	public static JSONObject youtubeSongSearch(int maxResults,
			String songTitle, String topicID) throws IOException,
			ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/youtube/v3/search");
		url.put("part", "id");
		url.put("maxResults", maxResults);
		url.put("q", songTitle);
		url.put("topicId", topicID);
		url.put("key", properties.get("API_KEY"));
		System.out.println(url);
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		return response;
	}

	/**
	 * 
	 * @param part
	 *            specifies the type of Youtube call. Relevant types are:
	 *            snippet, contentDetails, statistics, topicDetails
	 * @param youtubeID
	 *            id of the youtube clip you need more informations about
	 * @return
	 * @return returns a JSONObject from Youtube
	 * @throws IOException
	 * 
	 *             This method can retrieve different kinds of informations to a
	 *             youtube clip and is used to fill the YoutubeMetaData Objects
	 *             "snippet" provides informations for YoutubeID, ChannelID,
	 *             title, publishedAt "contentDetails" gives back the duration
	 *             "statistics" returns informations about viewcount, likes,
	 *             dislikes and comments "topicDetails" not used at the moment,
	 *             contains other freebase ids
	 * @throws ParseException
	 */
	public static JSONObject genericYoutubeCall(String part, String youtubeID)
			throws IOException, ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/youtube/v3/videos");
		url.put("part", "contentDetails");
		url.put("part", "snippet");
		url.put("part", "statistics");
		url.put("id", youtubeID);
		url.put("key", properties.get("API_KEY"));
		System.out.println(url);
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		return response;
	}

	/**
	 * 
	 * @param response
	 *            Input is an JSONObject produced from the method youtubeSearch
	 * @param container
	 *            gives reference to the container that should hold all finished
	 *            Youtube Objects
	 * @throws ParseException
	 * @throws IOException
	 * 
	 *             This method iterates over all entries it got from the common
	 *             youtube search and tries to fill all fields for the youtube
	 *             objects
	 * 
	 */

	public static void processingSearchResults(JSONObject response,
			List<YoutubeMetaData> container) throws IOException, ParseException {
		JSONArray responseItems = (JSONArray) response.get("items");
		for (int i = 0; i < responseItems.size(); i++) {
			JSONObject responseItemsEntry = (JSONObject) responseItems.get(i);
			JSONObject responseItemsId = (JSONObject) responseItemsEntry
					.get("id");
			String youtubeId = responseItemsId.get("videoId").toString();
			System.out.println("video id: " + youtubeId);
			YoutubeMetaData temp = new YoutubeMetaData();
			temp.setYoutubeID(youtubeId);
			JSONObject detailedResults = genericYoutubeCall("contentDetails",
					youtubeId);
			System.out.println(detailedResults);
			processingDetailedResults(detailedResults, temp);
			container.add(temp);
		}
	}

	/**
	 * 
	 * @param response
	 *            gets the detailed informations JSON produced eariler,
	 *            containing all informations about a sinlge video
	 * @param youtubeTemp
	 *            is a reference to the youtube object we try to fill in the for
	 *            loop
	 * 
	 *            the method iterates over the details JSON and fills all fields
	 */

	public static void processingDetailedResults(JSONObject response,
			YoutubeMetaData youtubeTemp) {
		JSONArray responseItems = (JSONArray) response.get("items");
		JSONObject responseItemsEntry = (JSONObject) responseItems.get(0);
		JSONObject responseSnippet = (JSONObject) responseItemsEntry
				.get("snippet");
		youtubeTemp.setChannelID(responseSnippet.get("channelId").toString());
		System.out.println("channelId "
				+ responseSnippet.get("channelId").toString());
		youtubeTemp.setTitle(responseSnippet.get("title").toString());
		youtubeTemp.setPublishedAt(responseSnippet.get("publishedAt")
				.toString());
		System.out.println("publishedAt "
				+ responseSnippet.get("publishedAt").toString());
		JSONObject responseContentDetails = (JSONObject) responseItemsEntry
				.get("contentDetails");
		youtubeTemp.setDuration(responseContentDetails.get("duration")
				.toString());
		System.out.println("duration "
				+ responseContentDetails.get("duration").toString());
		JSONObject responseStatistics = (JSONObject) responseItemsEntry
				.get("statistics");
		youtubeTemp
				.setViewCount(responseStatistics.get("viewCount").toString());
		System.out.println("viewCount"
				+ responseStatistics.get("viewCount").toString());
		youtubeTemp
				.setLikeCount(responseStatistics.get("likeCount").toString());
		System.out.println("likeCount"
				+ responseStatistics.get("likeCount").toString());
		youtubeTemp.setDislikeCount(responseStatistics.get("dislikeCount")
				.toString());
		System.out.println("dislikeCount"
				+ responseStatistics.get("dislikeCount").toString());
		youtubeTemp.setCommentCount(responseStatistics.get("commentCount")
				.toString());
		System.out.println("commentCount"
				+ responseStatistics.get("commentCount").toString());
	}

	/*
	 * public static List<YoutubeMetaData> getVideosForTrackAndBand( String
	 * bandMID, String trackName) {
	 * 
	 * JSON = apicall; ArrayList<YoutubeMetaData> response = new
	 * ArrayList<YoutubeMetaData>(); for (entrz in json){ YoutubeMetaData tmp =
	 * new YoutubeMetaData(); tmp.setChannelID(.YoutubeClipsFromBandMID...);
	 * response.add(tmp); } return response;
	 * 
	 * return null; }
	 */

}

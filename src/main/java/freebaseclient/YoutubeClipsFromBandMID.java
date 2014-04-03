package freebaseclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

	public static void main(String[] args) throws IOException, ParseException,
			java.text.ParseException {
		// Placeholder for file input MID
		String bandMid = "/m/0875sv";
		String title = "Morphogenesis";
		// List<YoutubeMetaData> list = getVideosForTrackAndBand(bandMid,
		// title);
		List<YoutubeMetaData> container = new ArrayList<YoutubeMetaData>();
		properties.load(new FileInputStream("freebase.properties"));

		JSONObject response = youtubeSongSearch(5, title, bandMid);
		processingSearchResults(response, container);
		System.out.println("First entry: ");
		System.out.println("youtubeId "
				+ container.get(0).getYoutubeID().toString());
		System.out.println("channelId "
				+ container.get(0).getChannelID().toString());
		System.out.println("title " + container.get(0).getTitle().toString());
		System.out.println("publishedAt "
				+ container.get(0).getPublishedAt().toString());
		System.out.println("duration "
				+ container.get(0).getDuration().toString());
		System.out.println("viewCount "
				+ container.get(0).getViewCount().toString());
		System.out.println("likeCount "
				+ container.get(0).getLikeCount().toString());
		System.out.println("dislikeCount "
				+ container.get(0).getDislikeCount().toString());
		System.out.println("commentCount "
				+ container.get(0).getCommentCount().toString());
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
		url.put("part", part);
		url.put("id", youtubeID);
		url.put("key", properties.get("API_KEY"));
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
	 * @throws java.text.ParseException
	 * 
	 */

	public static void processingSearchResults(JSONObject response,
			List<YoutubeMetaData> container) throws IOException,
			ParseException, java.text.ParseException {
		JSONArray responseItems = (JSONArray) response.get("items");
		for (int i = 0; i < responseItems.size(); i++) {
			JSONObject responseItemsEntry = (JSONObject) responseItems.get(i);
			JSONObject responseItemsId = (JSONObject) responseItemsEntry
					.get("id");
			String youtubeId = responseItemsId.get("videoId").toString();
			YoutubeMetaData temp = new YoutubeMetaData();
			temp.setYoutubeID(youtubeId);
			System.out.println("www.youtube.com/watch?v=" + youtubeId);
			JSONObject detailedResults = genericYoutubeCall(
					"snippet, contentDetails, statistics", youtubeId);
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
	 * @throws java.text.ParseException
	 */

	public static void processingDetailedResults(JSONObject response,
			YoutubeMetaData youtubeTemp) throws java.text.ParseException {
		JSONArray responseItems = (JSONArray) response.get("items");
		JSONObject responseItemsEntry = (JSONObject) responseItems.get(0);
		JSONObject responseSnippet = (JSONObject) responseItemsEntry
				.get("snippet");
		System.out.println("channelId "
				+ responseSnippet.get("channelId").toString());
		youtubeTemp.setChannelID(responseSnippet.get("channelId").toString());
		youtubeTemp.setTitle(responseSnippet.get("title").toString());
		System.out.println("title: " + responseSnippet.get("title").toString());
		String tempDate = responseSnippet.get("publishedAt").toString();
		DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss");
		Date date = formatter.parse(tempDate);
		youtubeTemp.setPublishedAt(date);
		System.out.println("publishedAt: " + youtubeTemp.getPublishedAt());
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
		System.out.println("------------------------------");
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

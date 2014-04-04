package freebaseclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
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
		String bandMid = "/m/02gpxc";
		String trackTitle = "När jättar marschera";
		String recordTitle = "Blodsvept";
		String youtubeChannelName = "inlegendband";
		properties.load(new FileInputStream("freebase.properties"));

		List<YoutubeMetaData> trackContainer = new ArrayList<YoutubeMetaData>();
		List<YoutubeMetaData> recordContainer = new ArrayList<YoutubeMetaData>();
		List<YoutubeMetaData> channelResultContainer = new ArrayList<YoutubeMetaData>();
		List<YoutubeMetaData> channelLockedContainer = new ArrayList<YoutubeMetaData>();
		List<YoutubeMetaData> searchTermContainer = new ArrayList<YoutubeMetaData>();
		List<YoutubeMetaData> searchTopicContainer = new ArrayList<YoutubeMetaData>();

		String youtubeChannelID = getYoutubeChannelId(youtubeChannelName);
		getVideoClipIDs(youtubeChannelID, channelResultContainer,
				channelLockedContainer);

		youtubeSongSearch(5, recordTitle, bandMid, recordContainer);
		youtubeSongSearch(5, trackTitle, bandMid, trackContainer);
		youtubeSongSearch(5, trackTitle, searchTermContainer);
		youtubeSongSearch(5, bandMid, searchTopicContainer);

		int maxDuration = recordContainer.get(0).getDurationInSeconds();
		YoutubeMetaData bestRecord = recordContainer.get(0);
		for (int i = 1; i < recordContainer.size(); i++) {
			if (maxDuration < recordContainer.get(i).getDurationInSeconds()) {
				maxDuration = recordContainer.get(i).getDurationInSeconds();
				bestRecord = recordContainer.get(i);
			}
		}

		System.out.println("Most likely full album: ");
		System.out.println("www.youtube.com/watch?v="
				+ bestRecord.getYoutubeID());

		System.out.println("--------------------------------");
		System.out.println("First search entry. Query: (trackTitle=\""
				+ trackTitle + "\", bandMid=\"" + bandMid + "\") ");
		System.out.println(trackContainer.get(0).toString());
		System.out.println("--------------------------------");
		System.out.println();

		System.out.println("---------------------------------");
		System.out
				.println("First Channel video entry. Query: (youtubeChannelName=\""
						+ youtubeChannelName + "\")");
		System.out.println(channelResultContainer.get(0).toString());
		System.out.println("----------------------------------");
		System.out.println();

		System.out.println("---------------------------------");
		System.out.println("First Search Term entry. Query: (trackTitle=\""
				+ trackTitle + "\")");
		System.out.println(searchTermContainer.get(0).toString());
		System.out.println("----------------------------------");
		System.out.println();

		System.out.println("-----------------------------------");
		System.out.println("First Search Topic entry. Query:  (bandMid=\""
				+ bandMid + "\")");
		System.out.println(searchTopicContainer.get(0).toString());
		System.out.println("-----------------------------------");
		System.out.println();
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
	 * @throws java.text.ParseException
	 */

	public static void youtubeSongSearch(int maxResults, String query,
			String topicID, List<YoutubeMetaData> container)
			throws IOException, ParseException, java.text.ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/youtube/v3/search");
		url.put("part", "id");
		url.put("maxResults", maxResults);
		url.put("q", query);
		url.put("topicId", topicID);
		url.put("type", "video");
		url.put("key", properties.get("API_KEY"));
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		processingSearchResults(response, container);
	}

	/**
	 * 
	 * @param maxResults
	 *            defines how many results should be requested
	 * @param query
	 *            defines the search query. Takes either a common search term
	 *            like a normal search in youtube or it can take a freebase id
	 * @return returns a JSON from youtube containing all video ids from the
	 *         requested search term
	 * @throws IOException
	 * @throws ParseException
	 * 
	 *             This method is a common youtube search request and requires a
	 *             search term or a freebase id
	 * @throws java.text.ParseException
	 */

	public static void youtubeSongSearch(int maxResults, String query,
			List<YoutubeMetaData> container) throws IOException,
			ParseException, java.text.ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/youtube/v3/search");
		url.put("part", "id");
		url.put("maxResults", maxResults);
		if (query.startsWith("/m/")) {
			url.put("topicId", query);
		} else {
			url.put("q", query);
		}
		url.put("type", "video");
		url.put("key", properties.get("API_KEY"));
		HttpRequest request = requestFactory.buildGetRequest(url);
		HttpResponse httpResponse = request.execute();
		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		processingSearchResults(response, container);
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
	 * @param videoID
	 *            needs a Youtube videoID
	 * @return true if the video is viewable in Germany, false if it is blocked
	 * @throws IOException
	 * @throws ParseException
	 * 
	 *             This method checks if a youtube video is available to view in
	 *             germany or regionLocked by GEMA
	 */

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

	/**
	 * 
	 * @param channelName
	 *            requires a String with a Youtube Channel Name
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * 
	 *             This Method retrieves
	 */

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

	/**
	 * 
	 * @param youtubeChannelId
	 *            requires a youtube Channel ID, probably created with
	 *            getYoutubeChannelId
	 * @return returns a list with all videos from the called channel, only
	 *         videos available in germany are beeing returned, GEMA blocked
	 *         videos are ignored
	 * @throws IOException
	 * @throws ParseException
	 * 
	 *             This method retrieves all videos from a specific youtube
	 *             channel, the call is made with a unique youtube channel id,
	 *             you can get it with the getYoutubeChannelId method and a
	 *             channel name
	 * @throws java.text.ParseException
	 */

	private static void getVideoClipIDs(String youtubeChannelId,
			List<YoutubeMetaData> resultContainer,
			List<YoutubeMetaData> lockedContainer) throws IOException,
			ParseException, java.text.ParseException {
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

		JSONParser parser = new JSONParser();
		JSONObject response = (JSONObject) parser.parse(httpResponse
				.parseAsString());
		System.out.println("response:  " + response);

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
			YoutubeMetaData temp = new YoutubeMetaData();
			if (checkRegionLock) {
				temp.setYoutubeID(videoID);
				JSONObject detailedResults = genericYoutubeCall(
						"snippet, contentDetails, statistics", videoID);
				processingDetailedResults(detailedResults, temp);
				resultContainer.add(temp);
			} else {
				temp.setYoutubeID(videoID);
				JSONObject detailedResults = genericYoutubeCall(
						"snippet, contentDetails, statistics", videoID);
				processingDetailedResults(detailedResults, temp);
				lockedContainer.add(temp);
			}
		}
		if (response.containsKey("nextPageToken")) {
			String nextPageToken = response.get("nextPageToken").toString();
			getNextPage(youtubeChannelId, nextPageToken, resultContainer,
					lockedContainer);
		}
		System.out.println("List of available Videos(" + resultContainer.size()
				+ ")");
		System.out.println("List of locked Videos (" + lockedContainer.size()
				+ ")");
	}

	/**
	 * 
	 * @param youtubeChannelId
	 *            requires a youtube channel id
	 * @param nextPageToken
	 *            youtube marker that is used to navigate between search pages
	 * @param resultContainer
	 *            List containing all videos from a channel
	 * @param lockedList
	 *            containing all GEMA blocked videos from a channel
	 * @throws IOException
	 * @throws ParseException
	 * 
	 *             this is a submethod to the getVideoClipIDs(String
	 *             youtubeChannelId){} method to retrieve more than 50 video
	 *             results
	 */

	private static void getNextPage(String youtubeChannelId,
			String nextPageToken, List<YoutubeMetaData> resultContainer,
			List<YoutubeMetaData> lockedContainer) throws IOException,
			ParseException {
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
			YoutubeMetaData temp = new YoutubeMetaData();
			if (checkRegionLock) {
				temp.setYoutubeID(videoID);
				resultContainer.add(temp);
			} else {
				temp.setYoutubeID(videoID);
				lockedContainer.add(temp);
			}

		}
		if (response.containsKey("nextPageToken")) {
			String newNextPageToken = response.get("nextPageToken").toString();
			getNextPage(youtubeChannelId, newNextPageToken, resultContainer,
					lockedContainer);
		}

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
		System.out.println(responseItems);
		for (int i = 0; i < responseItems.size(); i++) {
			JSONObject responseItemsEntry = (JSONObject) responseItems.get(i);
			JSONObject responseItemsId = (JSONObject) responseItemsEntry
					.get("id");
			String youtubeId = responseItemsId.get("videoId").toString();
			if (getRegionLockInfo(youtubeId)) {
				YoutubeMetaData temp = new YoutubeMetaData();
				temp.setYoutubeID(youtubeId);
				System.out.println("www.youtube.com/watch?v=" + youtubeId);
				JSONObject detailedResults = genericYoutubeCall(
						"snippet, contentDetails, statistics", youtubeId);
				processingDetailedResults(detailedResults, temp);
				container.add(temp);
			} else {
				System.out.println("The video with the id: " + youtubeId
						+ " is not available!");
			}
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
		PeriodFormatter pf = ISOPeriodFormat.standard();
		Period p = pf.parsePeriod(responseContentDetails.get("duration")
				.toString());
		Seconds s = p.toStandardSeconds();
		System.out.println("durationInSecond:  " + s.getSeconds());
		youtubeTemp.setDurationInSeconds(s.getSeconds());
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

}

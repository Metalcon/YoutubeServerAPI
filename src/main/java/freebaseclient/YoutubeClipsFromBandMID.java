package freebaseclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.minidev.json.JSONArray;

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

	public static void main(String[] args) {
		// Placeholder for file input MID
		String bandMid = "/m/04rcr";
		String title = "";
		List<YoutubeMetaData> list = getVideosForTrackAndBand(bandMid, title);
		YoutubeMetaData container = new YoutubeMetaData();

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

	public static JSONObject youtubeSearch(int maxResults, String songTitle,
			String topicID) throws IOException, ParseException {
		HttpTransport httpTransport = new NetHttpTransport();
		HttpRequestFactory requestFactory = httpTransport
				.createRequestFactory();

		GenericUrl url = new GenericUrl(
				"https://www.googleapis.com/youtube/v3/videos");
		url.put("maxResults", maxResults);
		url.put("q", songTitle);
		url.put("topicId", topicID);
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
	 * @return
	 */

	public static List<String> processingSearchResults(JSONObject response) {
		JSONArray responseItems = (JSONArray) response.get("items");
		List<String> returnList = new ArrayList<String>();
		for (int i = 0; i < responseItems.size(); i++) {
			JSONObject responseItemsEntry = (JSONObject) responseItems.get(i);
			JSONObject responseItemsId = (JSONObject) responseItemsEntry
					.get("id");
			String bandId = responseItemsId.get("videoId").toString();
			returnList.add(bandId);

		}
		return returnList;
	}

	public static List<YoutubeMetaData> getVideosForTrackAndBand(
			String bandMID, String trackName) {
		/*
		 * JSON = apicall; ArrayList<YoutubeMetaData> response = new
		 * ArrayList<YoutubeMetaData>(); for (entrz in json){ YoutubeMetaData
		 * tmp = new YoutubeMetaData();
		 * tmp.setChannelID(.YoutubeClipsFromBandMID...); response.add(tmp); }
		 * return response;
		 */
		return null;
	}

}

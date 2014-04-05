package youtube;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import api.MetalconYoutubeApi;
import api.YoutubeMetaData;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * 
 * @author tobi, rpickhardt
 * 
 */

public class YoutubeApiClient implements MetalconYoutubeApi {

    public Properties properties;

    public YoutubeApiClient() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("freebase.properties"));
        } catch (FileNotFoundException e) {
            System.out
                    .println("you need a file freebase.properties. look in your git for freebase.properties.sample and rename it");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see youtube.MetalconYoutubeApi#getVideoClipIDs(java.lang.String, java.util.List, java.util.List)
     */

    public void getVideoClipIDs(
            String youtubeChannelName,
            List<YoutubeMetaData> resultContainer,
            List<YoutubeMetaData> lockedContainer) {
        String youtubeChannelId = getYoutubeChannelId(youtubeChannelName);
        GenericUrl url =
                new GenericUrl(
                        "https://www.googleapis.com/youtube/v3/playlistItems");
        url.put("part", "snippet");
        url.put("playlistId", youtubeChannelId);
        url.put("key", properties.get("API_KEY"));
        url.put("maxResults", "50");
        JSONObject response = makeHttpRequest(url);
        System.out.println("response:  " + response);

        JSONArray responseItems = (JSONArray) response.get("items");

        for (int i = 0; i < responseItems.size(); ++i) {
            JSONObject responseItemsEntry = (JSONObject) responseItems.get(i);
            JSONObject responseItemsEntrySnippet =
                    (JSONObject) responseItemsEntry.get("snippet");
            JSONObject responseItemsEntrySnippetResourceid =
                    (JSONObject) responseItemsEntrySnippet.get("resourceId");
            String videoID =
                    responseItemsEntrySnippetResourceid.get("videoId")
                            .toString();
            boolean checkRegionLock = getRegionLockInfo(videoID);
            YoutubeMetaData temp = new YoutubeMetaData();
            if (checkRegionLock) {
                temp.setYoutubeID(videoID);
                JSONObject detailedResults =
                        genericYoutubeCall(
                                "snippet, contentDetails, statistics", videoID);
                processingDetailedResults(detailedResults, temp);
                resultContainer.add(temp);
            } else {
                temp.setYoutubeID(videoID);
                JSONObject detailedResults =
                        genericYoutubeCall(
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

    /* (non-Javadoc)
     * @see youtube.MetalconYoutubeApi#youtubeSongSearch(int, java.lang.String, java.lang.String)
     */

    public List<YoutubeMetaData> youtubeSongSearch(
            int maxResults,
            String query,
            String topicID) {
        GenericUrl url =
                new GenericUrl("https://www.googleapis.com/youtube/v3/search");
        url.put("part", "id");
        url.put("maxResults", maxResults);
        url.put("q", query);
        url.put("topicId", topicID);
        url.put("type", "video");
        url.put("key", properties.get("API_KEY"));
        return processingSearchResults(makeHttpRequest(url));
    }

    /* (non-Javadoc)
     * @see youtube.MetalconYoutubeApi#youtubeSongSearch(int, java.lang.String)
     */

    public List<YoutubeMetaData>
        youtubeSongSearch(int maxResults, String query) {
        GenericUrl url =
                new GenericUrl("https://www.googleapis.com/youtube/v3/search");
        url.put("part", "id");
        url.put("maxResults", maxResults);
        if (query.startsWith("/m/")) {
            url.put("topicId", query);
        } else {
            url.put("q", query);
        }
        url.put("type", "video");
        url.put("key", properties.get("API_KEY"));
        return processingSearchResults(makeHttpRequest(url));
    }

    /* (non-Javadoc)
     * @see youtube.MetalconYoutubeApi#youtubeCall(java.lang.String, java.lang.String)
     */
    public YoutubeMetaData youtubeCall(String part, String youtubeID) {
        JSONObject obj = genericYoutubeCall(part, youtubeID);
        YoutubeMetaData ymd = new YoutubeMetaData();
        ymd.setYoutubeID(youtubeID);
        return processingDetailedResults(obj, ymd);
    }

    /**
     * helper function to make an http request to youtube
     * 
     * @param url
     * @return
     */
    private JSONObject makeHttpRequest(GenericUrl url) {
        HttpTransport httpTransport = new NetHttpTransport();
        HttpRequestFactory requestFactory =
                httpTransport.createRequestFactory();
        HttpRequest request;
        try {
            request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = request.execute();
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(httpResponse.parseAsString());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

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
    private JSONObject genericYoutubeCall(String part, String youtubeID) {
        GenericUrl url =
                new GenericUrl("https://www.googleapis.com/youtube/v3/videos");
        url.put("part", part);
        url.put("id", youtubeID);
        url.put("key", properties.get("API_KEY"));
        return makeHttpRequest(url);
    }

    /**
     * 
     * @param videoID
     *            needs a Youtube videoID
     * @return true if the video is viewable in Germany, false if it is blocked
     *         or if the service is currently unavailable
     * @throws IOException
     * @throws ParseException
     * 
     *             This method checks if a youtube video is available to view in
     *             germany or regionLocked by GEMA
     */

    private boolean getRegionLockInfo(String videoID) {
        // https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id=91EAEKF2plE&key={YOUR_API_KEY}
        GenericUrl url =
                new GenericUrl("https://www.googleapis.com/youtube/v3/videos");
        url.put("part", "contentDetails");
        url.put("id", videoID);
        url.put("key", properties.get("API_KEY"));
        JSONObject response = makeHttpRequest(url);
        if (response == null) {
            return false;
        }
        JSONArray responseItems = (JSONArray) response.get("items");
        JSONObject responseItemsEntry = (JSONObject) responseItems.get(0);
        JSONObject responseItemsEntryContentdetails =
                (JSONObject) responseItemsEntry.get("contentDetails");
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
     * 
     *         This Method retrieves
     */

    private String getYoutubeChannelId(String channelName) {
        GenericUrl url =
                new GenericUrl("https://www.googleapis.com/youtube/v3/channels");

        url.put("key", properties.get("API_KEY"));
        url.put("forUsername", channelName);
        url.put("part", "contentDetails");
        JSONObject response = makeHttpRequest(url);
        JSONArray responseItems = (JSONArray) response.get("items");
        JSONObject responseItemsEntry = (JSONObject) responseItems.get(0);
        JSONObject responseItemsEntryContentdetails =
                (JSONObject) responseItemsEntry.get("contentDetails");
        JSONObject responseItemsEntryContentdetailsRelatedplaylists =
                (JSONObject) responseItemsEntryContentdetails
                        .get("relatedPlaylists");
        return responseItemsEntryContentdetailsRelatedplaylists.get("uploads")
                .toString();
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

    private void getNextPage(
            String youtubeChannelId,
            String nextPageToken,
            List<YoutubeMetaData> resultContainer,
            List<YoutubeMetaData> lockedContainer) {
        GenericUrl url =
                new GenericUrl(
                        "https://www.googleapis.com/youtube/v3/playlistItems");
        url.put("part", "snippet");
        url.put("playlistId", youtubeChannelId);
        url.put("key", properties.get("API_KEY"));
        url.put("maxResults", "50");
        url.put("pageToken", nextPageToken);
        JSONObject response = makeHttpRequest(url);
        JSONArray responseItems = (JSONArray) response.get("items");

        for (int i = 0; i < responseItems.size(); ++i) {
            JSONObject responseItemsEntry = (JSONObject) responseItems.get(i);
            JSONObject responseItemsEntrySnippet =
                    (JSONObject) responseItemsEntry.get("snippet");
            JSONObject responseItemsEntrySnippetResourceid =
                    (JSONObject) responseItemsEntrySnippet.get("resourceId");
            String videoID =
                    responseItemsEntrySnippetResourceid.get("videoId")
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
     * @return
     * @throws ParseException
     * @throws IOException
     * 
     *             This method iterates over all entries it got from the common
     *             youtube search and tries to fill all fields for the youtube
     *             objects
     * @throws java.text.ParseException
     * 
     */

    private List<YoutubeMetaData> processingSearchResults(JSONObject response) {
        if (response == null) {
            return null;
        }
        List<YoutubeMetaData> container = new ArrayList<YoutubeMetaData>();
        JSONArray responseItems = (JSONArray) response.get("items");
        System.out.println(responseItems);
        for (int i = 0; i < responseItems.size(); i++) {
            JSONObject responseItemsEntry = (JSONObject) responseItems.get(i);
            JSONObject responseItemsId =
                    (JSONObject) responseItemsEntry.get("id");
            String youtubeId = responseItemsId.get("videoId").toString();
            if (getRegionLockInfo(youtubeId)) {
                YoutubeMetaData temp = new YoutubeMetaData();
                temp.setYoutubeID(youtubeId);
                System.out.println("www.youtube.com/watch?v=" + youtubeId);
                JSONObject detailedResults =
                        genericYoutubeCall(
                                "snippet, contentDetails, statistics",
                                youtubeId);
                temp = processingDetailedResults(detailedResults, temp);
                if (temp == null) {
                    continue;
                }
                container.add(temp);
            } else {
                System.out.println("The video with the id: " + youtubeId
                        + " is not available!");
            }
        }
        return container;
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

    private YoutubeMetaData processingDetailedResults(
            JSONObject response,
            YoutubeMetaData youtubeTemp) {
        JSONArray responseItems = (JSONArray) response.get("items");
        JSONObject responseItemsEntry = (JSONObject) responseItems.get(0);
        JSONObject responseSnippet =
                (JSONObject) responseItemsEntry.get("snippet");
        System.out.println("channelId "
                + responseSnippet.get("channelId").toString());
        youtubeTemp.setChannelID(responseSnippet.get("channelId").toString());
        youtubeTemp.setTitle(responseSnippet.get("title").toString());
        System.out.println("title: " + responseSnippet.get("title").toString());
        String tempDate = responseSnippet.get("publishedAt").toString();
        DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = formatter.parse(tempDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return null;
        }
        youtubeTemp.setPublishedAt(date);
        System.out.println("publishedAt: " + youtubeTemp.getPublishedAt());
        JSONObject responseContentDetails =
                (JSONObject) responseItemsEntry.get("contentDetails");
        youtubeTemp.setDuration(responseContentDetails.get("duration")
                .toString());
        PeriodFormatter pf = ISOPeriodFormat.standard();
        Period p =
                pf.parsePeriod(responseContentDetails.get("duration")
                        .toString());
        Seconds s = p.toStandardSeconds();
        System.out.println("durationInSecond:  " + s.getSeconds());
        youtubeTemp.setDurationInSeconds(s.getSeconds());
        System.out.println("duration "
                + responseContentDetails.get("duration").toString());
        JSONObject responseStatistics =
                (JSONObject) responseItemsEntry.get("statistics");
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
        return youtubeTemp;
    }

}

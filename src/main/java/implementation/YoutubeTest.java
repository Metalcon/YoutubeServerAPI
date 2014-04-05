package implementation;

import java.util.ArrayList;
import java.util.List;

import youtube.YoutubeApiClient;
import api.MetalconYoutubeApi;
import api.YoutubeMetaData;

public class YoutubeTest {

    public static void main(String[] args) {
        String bandMid = "/m/02gpxc";
        String trackTitle = "När jättar marschera";
        String recordTitle = "Blodsvept";
        String youtubeChannelName = "inlegendband";

        MetalconYoutubeApi youtubeApiClient = new YoutubeApiClient();

        new ArrayList<YoutubeMetaData>();
        List<YoutubeMetaData> channelResultContainer =
                new ArrayList<YoutubeMetaData>();
        List<YoutubeMetaData> channelLockedContainer =
                new ArrayList<YoutubeMetaData>();

        youtubeApiClient.getVideoClipIDs(youtubeChannelName,
                channelResultContainer, channelLockedContainer);

        List<YoutubeMetaData> recordContainer =
                youtubeApiClient.youtubeSongSearch(5, recordTitle, bandMid);
        List<YoutubeMetaData> trackContainer =
                youtubeApiClient.youtubeSongSearch(5, trackTitle, bandMid);
        List<YoutubeMetaData> searchTermContainer =
                youtubeApiClient.youtubeSongSearch(5, trackTitle);
        List<YoutubeMetaData> searchTopicContainer =
                youtubeApiClient.youtubeSongSearch(5, bandMid);

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

}

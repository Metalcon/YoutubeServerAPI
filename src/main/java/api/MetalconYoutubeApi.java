package api;

import java.util.List;

public interface MetalconYoutubeApi {

    /**
     * 
     * @param youtubeChannelId
     *            requires a youtube ChanelName. if the youtube url was
     *            www.youtube.com/metallicaTV the channel name would be
     *            metallicaTV
     * @return returns a list with all videos from the called channel, only
     *         videos available in germany are beeing returned, GEMA blocked
     *         videos are ignored
     * 
     *         This method retrieves all videos from a specific youtube
     *         channel, the call is made with a unique youtube channel id,
     *         you can get it with the getYoutubeChannelId method and a
     *         channel name
     */

    public abstract void getVideoClipIDs(
            String youtubeChannelName,
            List<YoutubeMetaData> resultContainer,
            List<YoutubeMetaData> lockedContainer);

    /**
     * @param maxResults
     *            specifies the amount of results you get (1-50)
     * @param query
     *            The search term (in our case song title)
     * @param topicID
     *            The topicID from freebase (from band)
     * 
     * @return a list of YoutubeMetaData containing all video ids from the
     *         requested search term.
     * 
     *         This method is a generic youtube search. It needs a band
     *         topic ID from freebase and retrieves a list of songs to a
     *         query of a songTitle from that band
     */

    public abstract List<YoutubeMetaData> youtubeSongSearch(
            int maxResults,
            String query,
            String topicID);

    /**
     * 
     * @param maxResults
     *            defines how many results should be requested
     * @param query
     *            defines the search query. Takes either a common search term
     *            like a normal search in youtube or it can take a freebase id
     * @return a list of YoutubeMetaData containing all video ids from the
     *         requested search term.
     * 
     *         This method is a common youtube search request and requires a
     *         search term or a freebase id
     */

    public abstract List<YoutubeMetaData> youtubeSongSearch(
            int maxResults,
            String query);

    /**
     * 
     * @param part
     *            specifies the type of Youtube call. Relevant types are:
     *            snippet, contentDetails, statistics, topicDetails
     * @param youtubeID
     *            id of the youtube clip you need more informations about
     * @return
     * @return returns a JSONObject from Youtube
     * 
     *         This method can retrieve different kinds of informations to a
     *         youtube clip and is used to fill the YoutubeMetaData Objects
     *         "snippet" provides informations for YoutubeID, ChannelID,
     *         title, publishedAt "contentDetails" gives back the duration
     *         "statistics" returns informations about viewcount, likes,
     *         dislikes and comments "topicDetails" not used at the moment,
     *         contains other freebase ids
     */
    public abstract YoutubeMetaData youtubeCall(String part, String youtubeID);

}

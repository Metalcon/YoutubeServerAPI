package api;

import java.util.List;

/**
 * 
 * @author rpickhardt
 * 
 */
public interface YoutubeRequest {
	// retrieves full record clip from youtube
	List<YoutubeMetaData> getVideosForRecord(String metabwebID);

	List<YoutubeMetaData> getVideosForTrackAndBand(String bandMID,
			String trackName);

	/**
	 * 
	 * @param channelName
	 *            is just the unique name for the channel example:
	 *            "https://www.youtube.com/user/MetallicaTV" would just require
	 *            "MetallicaTV"
	 * @return
	 */
	List<YoutubeMetaData> getAllVideosFromChannel(String channelName);
}

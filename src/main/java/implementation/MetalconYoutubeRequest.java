package implementation;

import java.util.List;

import api.YoutubeMetaData;
import api.YoutubeRequest;
import freebaseclient.YoutubeClipsFromBandMID;

public class MetalconYoutubeRequest implements YoutubeRequest {

	public List<YoutubeMetaData> getVideosForRecord(String metabwebID) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<YoutubeMetaData> getVideosForTrackAndBand(String bandMid,
			String trackName) {
		return YoutubeClipsFromBandMID.getVideosForTrackAndBand(bandMid,
				trackName);
	}

	public List<YoutubeMetaData> getAllVideosFromChannel(String channelName) {
		// TODO Auto-generated method stub
		return null;
	}

}

package api;

import java.util.List;

/**
 * 
 * @author rpickhardt
 * 
 */
public interface YoutubeRequest {

    List<YoutubeMetaData> getVideosForRecord(String metabwebID);
}

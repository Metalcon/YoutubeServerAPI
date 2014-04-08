package api;

import java.util.Date;

/**
 * stores information from one or several successfull youtube requests.
 * 
 * TODO: extend with more class fields and create getters and setters
 * 
 * @author rpickhardt
 * 
 */
public class YoutubeMetaData {

	private String youtubeID;
	private String channelID;
	private String title;
	private Date publishedAt;
	private String duration;
	private int durationInSeconds;
	private String viewCount;
	private String likeCount;
	private String dislikeCount;
	private String commentCount;
	private boolean creativeCommon;

	public String getYoutubeID() {
		return youtubeID;
	}

	public void setYoutubeID(String youtubeID) {
		this.youtubeID = youtubeID;
	}

	public String getChannelID() {
		return channelID;
	}

	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(Date publishedAt) {
		this.publishedAt = publishedAt;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getViewCount() {
		return viewCount;
	}

	public void setViewCount(String viewCount) {
		this.viewCount = viewCount;
	}

	public String getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(String likeCount) {
		this.likeCount = likeCount;
	}

	public String getDislikeCount() {
		return dislikeCount;
	}

	public void setDislikeCount(String dislikeCount) {
		this.dislikeCount = dislikeCount;
	}

	public String getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(String commentCount) {
		this.commentCount = commentCount;
	}

	public int getDurationInSeconds() {
		return durationInSeconds;
	}

	public void setDurationInSeconds(int durationInSeconds) {
		this.durationInSeconds = durationInSeconds;
	}

	public boolean isCreativeCommon() {
		return creativeCommon;
	}

	public void setCreativeCommon(boolean creativeCommon) {
		this.creativeCommon = creativeCommon;
	}

	public String toString() {
		String response = "";

		response = "--------------------------------" + "\n" + "youtubeId: "
				+ youtubeID + "\n" + "channelId: " + channelID + "\n"
				+ "title: " + title + "\n" + "publishedAt: " + publishedAt
				+ "\n" + "duration: " + duration + "\n" + "durationInSeconds :"
				+ durationInSeconds + "\n" + "viewCount: " + viewCount + "\n"
				+ "likeCount: " + likeCount + "\n" + "dislikeCount: "
				+ dislikeCount + "\n" + "commentCount: " + commentCount + "\n"
				+ "CreativeCommon: " + creativeCommon + "\n\n";

		return response;
	}

}

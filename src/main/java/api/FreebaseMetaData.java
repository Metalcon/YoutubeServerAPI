package api;

/**
 * Class which stores the freebase ids
 * 
 * TODO: extend with more class fields and create getters and setters
 * 
 * @author rpickhardt
 * 
 */
public class FreebaseMetaData {

	private String mid;
	private double confidence;
	private String type;

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}

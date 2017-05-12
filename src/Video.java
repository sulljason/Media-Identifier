
public class Video {
	
	private final String video;
	private ScreenHash closestMatch;
	private int distance = -1;
	
	public Video(String path) {
		this.video = path;
	}
	
	public String getVideo() {
		return this.video;
	}
	
	public ScreenHash getClosestMatch() {
		return this.closestMatch;
	}
	
	public long getClosestDistance() {
		return this.distance;
	}
	
	public void setClosestMatch(ScreenHash closest) {
		this.closestMatch = closest;
	}
	
	public void setClosestDistance(int distance) {
		this.distance = distance;
	}
}

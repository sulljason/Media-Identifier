
public class ScreenHash {
	
	private final String screenshot;
	private final long hash;
	
	public ScreenHash(String screenshot, long hash) {
		this.screenshot = screenshot;
		this.hash = hash;
	}
	
	public String getScreenshot() {
		return this.screenshot;
	}
	
	public long getHash() {
		return this.hash;
	}
	
}

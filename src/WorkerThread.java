import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.bytedeco.javacpp.RealSense.frame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.FrameGrabber.Exception;

public class WorkerThread extends Thread {
	
	private final LinkedBlockingQueue<String> workQueue;
	private String currentFile;
	private long[] frameHashes;
	private final List<Video> videoHashes;
	private final ArrayList<ScreenHash> screenHashes;
	private final DHasher hasher = new DHasher();
	private boolean running = false;
	private boolean workCompleted = true;
	
	public WorkerThread(List<Video> videoHashes, ArrayList<ScreenHash> screenHashes, LinkedBlockingQueue queue) {
		this.videoHashes = videoHashes;
		this.workQueue = queue;
		this.screenHashes = screenHashes;
	}
	
	private BufferedImage img;
	@Override
	public void run() {
		this.running = true;
		this.workCompleted = false;
		while(this.running) {
			try {
				this.currentFile = this.workQueue.take();
				FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(this.currentFile);
				this.frameHashes = new long[frameGrabber.getLengthInFrames()];
				
				frameGrabber.start();
				img = new BufferedImage(frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), BufferedImage.TYPE_3BYTE_BGR);
				for(int i = 0; i < this.frameHashes.length; i++) {
					ByteBuffer ffmpegBuffer = (ByteBuffer)  frameGrabber.grabImage().image[0];
					byte[] imgBuffer = ((DataBufferByte)img.getData().getDataBuffer()).getData();
					ffmpegBuffer.get(imgBuffer); //Copy the FFmpeg image buffer into a buffered image.
					ffmpegBuffer.flip(); //DO A FLIP!
					this.frameHashes[i] = ImagePHasher.getHash(img);
				}
				frameGrabber.stop();
				frameGrabber.flush();
				frameGrabber = null;
				
				System.out.println(this.getName() + ": Finished processing and hashing " + this.currentFile + ".");
				System.out.println(this.getName() + ": Now searching screenshots for best match.");
				
				//Find the closest pair of a video frame and a screenshot.
				Video videoHash = new Video(this.currentFile);
				for(int i = 0; i < this.screenHashes.size(); i++) {
					for(int j = 0; j < this.frameHashes.length; j++) {
						int distance = BitManip.hamDistance(this.screenHashes.get(i).getHash(), this.frameHashes[j]);
						if(distance < videoHash.getClosestDistance()) {
							videoHash.setClosestMatch(this.screenHashes.get(i));
							videoHash.setClosestDistance(distance);
						}
					}
				}
				this.videoHashes.add(videoHash);
			} catch (InterruptedException e) {
				this.running = false;
				System.out.println(this.getName() + ": STOP signal recieved. Completing current job then stopping.");
			} catch (Exception e) {
				System.err.println("Failed to read frame from " + currentFile + " skipping file.");
			} catch (java.lang.Exception e) {
				System.err.println("Failed to read frame from " + currentFile + " skipping file.");
			}
		}
		this.workCompleted = true;
	}
	
	public void stopWorker() {
		this.running = false;
	}
	
	public boolean isCompleted() {
		return this.workCompleted;
	}

}

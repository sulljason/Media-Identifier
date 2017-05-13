import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;

public class WorkerThread extends Thread {
	
	private final LinkedBlockingQueue<Video> workQueue;
	private String currentFile;
	private long[] frameHashes;
	private final ArrayList<ScreenHash> screenHashes;
	private final DHasher hasher = new DHasher();
	private boolean running = false;
	private boolean workCompleted = true;
	
	public WorkerThread(ArrayList<ScreenHash> screenHashes, LinkedBlockingQueue<Video> queue) {
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
				this.currentFile = this.workQueue.take().getVideo();
				FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(this.currentFile);
				frameGrabber.start();
				this.frameHashes = new long[frameGrabber.getLengthInFrames()];
				
				img = new BufferedImage(frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), BufferedImage.TYPE_3BYTE_BGR);
				for(int i = 0; i < 1000; i++) {
					ByteBuffer ffmpegBuffer = (ByteBuffer)frameGrabber.grabImage().image[0];
					byte[] imgBuffer = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
					ffmpegBuffer.get(imgBuffer); //Copy the FFmpeg image buffer into a buffered image.
					ffmpegBuffer.flip(); //DO A FLIP!
					this.frameHashes[i] = hasher.hash(img);
				}
				//frameGrabber.stop();
				//frameGrabber.close();
				//frameGrabber = null;
				
				System.out.println(this.getName() + ": Finished processing and hashing " + this.currentFile + ".");
				System.out.println(this.getName() + ": Now searching screenshots for best match.");
				
				//Find the closest pair of a video frame and a screenshot.
				Video videoHash = new Video(this.currentFile);
				int besthashloc = 0;
				for (int i = 0; i < this.screenHashes.size(); i++) {
					for (int j = 0; j < this.frameHashes.length; j++) {
						int distance = BitManip.hamDistance(this.screenHashes.get(i).getHash(), this.frameHashes[j]);
						if (distance < videoHash.getClosestDistance()) {
							besthashloc = i;
							videoHash.setClosestMatch(this.screenHashes.get(i));
							videoHash.setClosestDistance(distance);
						}
					}
				}
				System.out.println(videoHash.getClosestDistance());
				//frameGrabber.start();
				//frameGrabber.setFrameNumber(besthashloc);
				img = new BufferedImage(frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), BufferedImage.TYPE_3BYTE_BGR);
				ByteBuffer ffmpegBuffer = (ByteBuffer)frameGrabber.grabImage().image[0];
				byte[] imgBuffer = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
				ffmpegBuffer.get(imgBuffer); //Copy the FFmpeg image buffer into a buffered image.
				ImageIO.write(img, "bmp", new File(this.currentFile + ".bmp"));
				frameGrabber.stop();
				frameGrabber.close();
				//Deal with stuff
				
			} catch (InterruptedException e) {
				this.running = false;
				System.out.println(this.getName() + ": STOP signal recieved. Completing current job then stopping.");
			} catch (Exception e) {
				System.err.println("Failed to read frame from " + currentFile + " skipping file.");
				e.printStackTrace();
			} catch (java.lang.Exception e) {
				System.err.println("Failed to read frame from " + currentFile + " skipping file.");
				e.printStackTrace();
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

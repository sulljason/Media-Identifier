import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

public class Launch {
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		System.setProperty("sun.java2d.noddraw", "true");
		/**
		if(args.length < 3 | Integer.parseInt(args[0]) < 1) {
			System.err.println("Correct usage: java -jar <jar> <workers> <screenshotDir> <videoDir>");
		}
		hashVideos(args[2], hashScreenshots(args[1]), Integer.parseInt(args[0]));*/
		hashVideos(null, null, 1);
	}
	
	private static ArrayList<ScreenHash> hashScreenshots(String screenDir) {
		File screenshotsDir = new File(screenDir);
		if(!screenshotsDir.exists() | !screenshotsDir.isDirectory()) {
			System.err.println("Screenshot directory could not be found.");
			System.exit(0);
		}
		File[] screenshots = screenshotsDir.listFiles();
		
		ArrayList<ScreenHash> screenshotHashes = new ArrayList<ScreenHash>(screenshots.length);
		
		AvgHasher hasher = new AvgHasher();
		for (File file : screenshots) {
			if( file.isFile() && file.canRead()) {
				try {
					BufferedImage image = ImageIO.read(file);
					screenshotHashes.add(new ScreenHash(file.getAbsolutePath(), hasher.getHash(image)));
					System.out.println(file.getName() + " hashed.");
				} catch (IOException e) {
					System.err.println(file.getName() + " is not an image. Supported formats are bmp,png, and jpg.");
				}
			}
		}
		return screenshotHashes;
	}
	
	private static void hashVideos(String vidDir, ArrayList<ScreenHash>scrnHashes, int workers) {
		/*
		File videosDir = new File(vidDir);
		File[] videoFiles = videosDir.listFiles();
		if(!videosDir.exists() | !videosDir.isDirectory()) {
			System.err.println("Video directory could not be found.");
			System.exit(0);
		}
		ArrayList<Video> videos = new ArrayList<Video>(videoFiles.length);
		for (File file : videoFiles) {
			if (file.isFile() && file.canRead()) {
				videos.add(new Video(file.getAbsolutePath()));
			}
		}
		System.out.println("Spinning up " + workers + " worker threads.");
		BasicWorkerPool workerPool = new BasicWorkerPool(workers, scrnHashes, null);
		System.out.println("Putting coffee in them...");**/

		BufferedImage screen = null;
		try {
			screen = ImageIO.read(new File("testVideos/derp.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("testVideos/derp.mp4");
		BufferedImage img = new BufferedImage(1280, 720, BufferedImage.TYPE_3BYTE_BGR);
		byte[] imgBuffer = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
		DHasher hasher = new DHasher();
		long screenHash = hasher.hash(screen);
		try {
			grabber.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int iter = 1000;
		int sum = 0;
		long start = System.nanoTime();
		for (int i = 0; i < iter; i++) {
			Frame frame = null;
			try {
				frame = grabber.grabImage();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ByteBuffer buffer = (ByteBuffer)frame.image[0];
			buffer.get(imgBuffer);
			buffer.flip(); //DO A FLIP!
			int dist = BitManip.hamDistance(screenHash, hasher.hash(img));
			//System.out.println(dist);
			//sum += hasher.hash(screen);
		}
		try {
			grabber.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(((System.nanoTime() - start) / iter) / 1000000F);
		System.out.println(hasher.hash(screen));
		try {
			ImageIO.write(img, "png", new File("derp.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
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

public class Launch {
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		System.setProperty("sun.java2d.noddraw", "true");	
		if(args.length < 3 || Integer.parseInt(args[0]) < 1) {
			System.err.println("Correct usage: java -jar <jar> <workers> <screenshotDir> <videoDir>");
			System.exit(0);
		}
		hashVideos(args[2], hashScreenshots(args[1]), Integer.parseInt(args[0]));
		//hashVideos(null, null, 1);
	}
	
	private static ArrayList<ScreenHash> hashScreenshots(String screenDir) {
		File screenshotsDir = new File(screenDir);
		if(!screenshotsDir.exists() | !screenshotsDir.isDirectory()) {
			System.err.println("Screenshot directory could not be found.");
			System.exit(0);
		}
		File[] screenshots = screenshotsDir.listFiles();
		
		ArrayList<ScreenHash> screenshotHashes = new ArrayList<ScreenHash>(screenshots.length);
		
		DHasher hasher = new DHasher();
		for (File file : screenshots) {
			if(file.isFile() && file.canRead()) {
				try {
					BufferedImage image = ImageIO.read(file);
					screenshotHashes.add(new ScreenHash(file.getAbsolutePath(), hasher.hash(image)));
					System.out.println(file.getName() + " hashed.");
				} catch (IOException e) {
					System.err.println(file.getName() + " is not an image. Supported formats are bmp,png, and jpg.");
				}
			}
		}
		return screenshotHashes;
	}
	
	private static void hashVideos(String vidDir, ArrayList<ScreenHash>scrnHashes, int workers) {
		File videosDir = new File(vidDir);
		File[] videoFiles = videosDir.listFiles();
		if(!videosDir.exists() | !videosDir.isDirectory()) {
			System.err.println("Video directory could not be found.");
			System.exit(0);
		}
		System.out.println("Spinning up " + workers + " worker threads.");
		BasicWorkerPool workerPool = new BasicWorkerPool(workers, scrnHashes, null);
		System.out.println("Putting coffee in them...");
		workerPool.startWorkers();
		
		for (File file : videoFiles) {
			if (file.isFile() && file.canRead()) {
				workerPool.addVideo(new Video(file.getAbsolutePath()));
			}
		}
		
		try {
			Thread.sleep(10000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.resizers.BilinearResizer;


public class AvgHasher {

	private static final int size = 8;
	private BufferedImage smallImg;
	private static final BilinearResizer resizer = new BilinearResizer();
	
	public AvgHasher() {
		smallImg = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
	}
	
	public final long getHash(BufferedImage img) {
		//Shrink the image to the set size and convert it to gray scale.
		resizer.resize(img, smallImg);
		byte[] imgBuffer = ((DataBufferByte)smallImg.getData().getDataBuffer()).getData();
		
		//Average all the pixels.
		int total = 0;
		for (int i = 0; i < imgBuffer.length; i++) {
			total += imgBuffer[i];
		}
		int avgPixel = total / imgBuffer.length;
		
		//Test each pixel versus the average and at the bit to 1 if higher or 0 if lower.
		long hash = 0L;
		for (int i = 0; i < imgBuffer.length; i++) {
			if (imgBuffer[i] > avgPixel)
				hash = hash ^ (1L << i);
		}
		return hash;
	}
	
}

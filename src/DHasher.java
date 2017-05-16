import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import net.coobird.thumbnailator.resizers.BilinearResizer;


public class DHasher {
	
	private static final BilinearResizer resizer = new BilinearResizer();
	private final BufferedImage image = new BufferedImage(9, 8, BufferedImage.TYPE_3BYTE_BGR);
	private final byte[] imgBuffer = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
	
	public long hash(BufferedImage img) {
		//Resize image to 8x8 and reduce to greyscale.
		resizer.resize(img, image);
		
		//Compare each pixel to it's neighbor and set a hash bit to 1 or 0 if it's brighter.
		long hash = 0L;
		for (int i = 0; i < 64; i++) {
			if (imgBuffer[i] < imgBuffer[i + 1])
				hash = hash ^ (1L << i);
		}
		return hash;
	}

}

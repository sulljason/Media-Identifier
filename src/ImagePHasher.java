import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.resizers.BilinearResizer;

/**
 * A pHash-like image hash.
 * Based On: http://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html
 * @author Elliot Shepherd (elliot@jarofworms.com
 */
public class ImagePHasher {
	
	private static BilinearResizer resizer = new BilinearResizer();

	//The Pixel dimensions for the hashing function
	private static final int size = 64;
	private static final int smallerSize = 8;

	//Precomputed stuff.
	private static final float[][] cosines = preComputeCos();

	/**
	 * Precomputes a crap load of cosines to save large amounts of CPU time.
	 */
	private static float[][] preComputeCos() {
		float[][] preCompCos = new float[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				preCompCos[i][j] = (float)(Math.cos(((2 * i + 1 ) / (2.0 * size)) * j * Math.PI));
			}
		}
		return preCompCos;
	}

	/**
	 * Returns a long representing the hash.
	 * @param img a buffered image.
	 * @return the hash.
	 * @throws Exception
	 */
	public static long getHash(BufferedImage img) {
		/* 3. Compute the DCT.
		 * The DCT separates the image into a collection of frequencies
		 * and scalars. While JPEG uses an 8x8 DCT, this algorithm uses
		 * a 32x32 DCT.
		 */
		float[][] dctVals = new float[1][1];
		try {
			dctVals = applyDCT(prepImage(img));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* 4. Reduce the DCT.
		 * This is the magic step. While the DCT is 32x32, just keep the
		 * top-left 8x8. Those represent the lowest frequencies in the
		 * picture.
		 */
		/* 5. Compute the average value.
		 * Like the Average Hash, compute the mean DCT value (using only
		 * the 8x8 DCT low-frequency values and excluding the first term
		 * since the DC coefficient can be significantly different from
		 * the other values and will throw off the average).
		 */
		float total = 0.0f;

		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				total += dctVals[x][y];
			}
		}
		
		float avg = (total - dctVals[0][0]) / 63.0f;

		/* 6. Further reduce the DCT.
		 * This is the magic step. Set the 64 hash bits to 0 or 1
		 * depending on whether each of the 64 DCT values is above or
		 * below the average value. The result doesn't tell us the
		 * actual low frequencies; it just tells us the very-rough
		 * relative scale of the frequencies to the mean. The result
		 * will not vary as long as the overall structure of the image
		 * remains the same; this can survive gamma and color histogram
		 * adjustments without a problem.
		 */
		return convertToHash(dctVals, avg);
	}
	
	private static long convertToHash(float[][] dctVals, float avg) {
		long hash = 0L;

		int counter = 1;
		for (int x = 0; x < smallerSize; x++) {
			for (int y = 0; y < smallerSize; y++) {
				if (dctVals[x][y] > avg)
					hash = hash ^ (1L << counter);
				counter++;
			}
		}

		return hash;
	}

	/**
	 * Prepares an image for DCT work.
	 * This resizes the image to the correct small size (possibly with acceleration)
	 * It then converts it to grayscale.
	 * @param image the BufferedImage to mix down
	 * @param width desired width of the image, in pixels
	 * @param height desired height of the image, in pixels
	 * @return the mixed-down image
	 * @throws IOException 
	 */
	private static float[][] prepImage(BufferedImage image) throws IOException {
		float[][] vals = new float[size][size];
		BufferedImage resizedImage = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, size, size, null);
		g.dispose();
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				vals[x][y] = (float)(image.getRGB(x , y));
			}
		}
		//ImageIO.write(resizedImage, "png", new File("smaller.png"));
		return vals;
	}

	/**
	 * Does the DCT magic:
	 * DCT function stolen from http://stackoverflow.com/questions/4240490/problems-with-dct-and-idct-algorithm-in-java
	 * @param a a 2d-array of pixel grayscale values.
	 * @return the computed array
	 */
	private static float[][] applyDCT(float[][] a) {
		float[][] dct = new float[size][size];
		for (int u = 0; u < size; u++) {
			for (int v = 0; v < size; v++) {
				float sum = 0.0f;

				for (int i=0; i < size; i++) {
					for (int j=0; j < size; j++) {
						sum += cosines[i][u] * cosines[j][v] * a[i][j];
					}
				}
				
				if (u == 0 || v == 0)
					dct[u][v] = sum * 1.41421f;
				else
					dct[u][v] = sum * 0.25f;
			}
		}
		return dct;
	}
}
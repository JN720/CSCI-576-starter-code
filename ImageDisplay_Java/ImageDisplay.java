
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

enum ColorMode {
	RGB,
	YUV
}

public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage imgOne;
	BufferedImage imgTwo;

	// Modify the height and width values here to read and display an image with
  	// different dimensions. 
	int width = 512;
	int height = 512;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		RandomAccessFile raf = null;
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally {
			try {
				if (raf != null)
					raf.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private double[] rgbToYuv(int r, int g, int b) {
		double values[] = {0, 0, 0};

		double red = (double) r;
		double green = (double) g;
		double blue = (double) b;

		values[0] = (red * 0.299) + (green * 0.587) + (blue * 0.114);
		values[1] = (red * -0.417) + (green * -0.289) + (blue * 0.436);
		values[2] = (red * 0.615) + (green * -0.515) + (blue * -0.100);

		return values;
	}
		
	private int[] yuvToRgb(double y, double u, double v) {
		int values[] = {0, 0, 0};

		// values[0] = Math.round((float)(y + (1.1398 * v)));
		// values[1] = Math.round((float)(y - (0.3946 * u) - (0.5806 * v)));
		// values[2] = Math.round((float)(y + (2.0321 * u)));

		values[0] = (int)((y * 0.956) + (u * -0.058) + (v * 1.122));
		values[1] = (int)((y * 0.854) + (u * -0.447) + (v * -0.718));
		values[2] = (int)((y * 1.48) + (u * 1.942) + (v * 0.597));

		// if (Math.random() > 0.99) {
		// 	System.out.print("y ");
		// 	System.out.println(y);
		// 	System.out.print("u ");
		// 	System.out.println(u);
		// 	System.out.print("v ");
		// 	System.out.println(v);
		// 	System.out.print("r ");
		// 	System.out.println(values[0]);
		// 	System.out.print("g ");
		// 	System.out.println(values[1]);
		// 	System.out.print("b ");
		// 	System.out.println(values[2]);
		// 	System.out.println();
		// }

		for (int i = 0; i < 3; ++i) {
			values[i] = Math.min(255, Math.max(0, values[i]));
		}

		return values;
	}

	private int uniformQuantizeRGB(int[] values, int[] bits) {
		if (values.length != 3) {
			return -1;
		}
		int newValues[] = {0, 0, 0};

		for (int i = 0; i < 3; ++i) {
			int spaceSize = 1 << (bits[i]);
			int rangeSize = 256 / spaceSize;
			int rangeBottom = values[i] - (values[i] % rangeSize);
			int rangeCenter = rangeBottom + (rangeSize / 2);
			newValues[i] = rangeCenter;
		}

		int finalValue = 0;
		finalValue += newValues[0] << 16;
		finalValue += newValues[1] << 8;
		finalValue += newValues[2];

		return finalValue;
	}

	private double[] uniformQuantizeYUV(double[] values, int[] bits) {
		if (values.length != 3 || bits.length != 3) {
			throw new Error("invalid input");
		}

		double newValues[] = {0, 0, 0};

		double[] mins = {0, -180.03, -156.825};
		double[] maxs = {262.65, 111.18, 156.825};

		for (int i = 0; i < 3; ++i) {
			double fullRange = maxs[i] - mins[i];
			double spaceSize = 1 << (bits[i]);
			double rangeSize = fullRange / spaceSize;
			double rangeBottom = values[i] - (values[i] % rangeSize);
			double rangeCenter = rangeBottom + (rangeSize / 2);
			if (Math.random() > 0.99) {
				System.out.print("original ");
				System.out.println(values[i]);
				System.out.print("full range ");
				System.out.print(mins[i]);
				System.out.print(" - ");
				System.out.println(maxs[i]);
				System.out.println(fullRange);
				System.out.print("space size ");
				System.out.println(spaceSize);
				System.out.print("range size ");
				System.out.println(rangeSize);
				System.out.print("range bottom ");
				System.out.println(rangeBottom);
				System.out.print("range center ");
				System.out.println(rangeCenter);
				System.out.println();
			}
			newValues[i] = rangeCenter;
		}


		return newValues;
	}

	private BufferedImage modifyImage(BufferedImage img, String[] args)
	{
		int width = img.getWidth();
		int height = img.getHeight();

		ColorMode colorMode = ColorMode.RGB;
		if (args.length > 1 && args[1].equals("2"))
		{
			colorMode = ColorMode.YUV;
		}

		boolean optimizedQuantization = (args.length > 2 && args[2].equals("2"));
		
		int bits[] = {8, 8, 8};
		if (args.length > 5)
		{
			bits[0] = Integer.parseInt(args[3]);
			bits[1] = Integer.parseInt(args[4]);
			bits[2] = Integer.parseInt(args[5]);
		}

		BufferedImage image = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);

		RGBQuantizer optimizedQuantizer = new KMeansRGBQuantizer();
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int pixel = img.getRGB(x, y);
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if (optimizedQuantization) {
					if (colorMode == ColorMode.YUV) {
						// double[] yuv = rgbToYuv(r, g, b);
						throw new Error("not implemented");
					}
					else {
						if (!optimizedQuantizer.getIsFit())
							optimizedQuantizer.fit(img, bits);
						pixel = optimizedQuantizer.quantize(pixel);
					}
				} else {
					if (colorMode == ColorMode.YUV) {
						double[] yuv = rgbToYuv(r, g, b);
						double[] quantized = uniformQuantizeYUV(yuv, bits);
						int[] rgb = yuvToRgb(quantized[0], quantized[1], quantized[2]);
						pixel = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
						// System.out.print("r ");
						// System.out.println(r);
						// System.out.println(rgb[0]);
						// System.out.print("g ");
						// System.out.println(g);
						// System.out.println(rgb[1]);
						// System.out.print("b ");
						// System.out.println(b);
						// System.out.println(rgb[2]);
						// System.out.println();
					} else {
						pixel = uniformQuantizeRGB(new int[] {r, g, b}, bits);
					}
				}
				try {
					image.setRGB(x, y, pixel);
				} catch (Exception e) {
					System.out.println(e.getMessage());
					System.out.println(x + " " + y);
					System.exit(0);
				}
				
			}
		}



		return image;
	}

	public void showIms(String[] args){

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		imgTwo = modifyImage(imgOne, args);

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));
		lbIm2 = new JLabel(new ImageIcon(imgTwo));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		c.gridx = 1; 
		c.gridy = 1;
		frame.getContentPane().add(lbIm2, c);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}

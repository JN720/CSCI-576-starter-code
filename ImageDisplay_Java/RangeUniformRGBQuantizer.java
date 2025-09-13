import java.awt.image.BufferedImage;

public class RangeUniformRGBQuantizer extends RGBQuantizer {
    double[] mins;
    double[] maxs;
    int[] bits;

    public int quantize(int rgb) {
        if (!getIsFit()) {
            throw new Error("Must be fit before quantizing");
        }
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        int values[] = {r, g, b};
        for (int i = 0; i < 3; i++) {
            double fullRange = maxs[i] - mins[i];
            double spaceSize = 1 << (bits[i]);
            double rangeSize = fullRange / spaceSize;
            double rangeBottom = values[i] - (values[i] % rangeSize);
            double rangeCenter = rangeBottom + (rangeSize / 2);
            values[i] = (int)Math.round(rangeCenter);
        }

        return (values[0] << 16) | (values[1] << 8) | values[2];
    }

    
    @Override
    public void fit(BufferedImage image, int[]bits) {
        int width = image.getWidth();
        int height = image.getHeight();

        this.bits = bits;

        mins = new double[]{255, 255, 255};
        maxs = new double[]{0, 0, 0};

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                if (r < mins[0]) {
                    mins[0] = r;
                }
                if (r > maxs[0]) {
                    maxs[0] = r;
                }
                if (g < mins[1]) {
                    mins[1] = g;
                }
                if (g > maxs[1]) {
                    maxs[1] = g;
                }
                if (b < mins[2]) {
                    mins[2] = b;
                }
                if (b > maxs[2]) {
                    maxs[2] = b;
                }

            }
        }

        System.out.println(mins[0] + " " + maxs[0]);
        System.out.println(mins[1] + " " + maxs[1]);
        System.out.println(mins[2] + " " + maxs[2]);
        System.out.println();

        super.fit(image, bits);
    }
}

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class MedianRGBQuantizer extends RGBQuantizer {
    List<Double> redReps;
    List<Double> greenReps;
    List<Double> blueReps;

    public int quantize(int rgb) {
        if (!getIsFit()) {
            throw new Error("Must be fit before quantizing");
        }
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        int red = (int) Math.round(getRegionCenter(r, redReps));
        int green = (int) Math.round(getRegionCenter(g, greenReps));
        int blue = (int) Math.round(getRegionCenter(b, blueReps));

        return (red << 16) | (green << 8) | blue;
    }

    private List<Double> fitReps(PriorityQueue<Integer> elements, int bits) {
        // Add 0 and max values for padding
        int numReps = (1 << (bits + 1)) + 0;
        ArrayList<Double> reps = new ArrayList<Double>();
        int repPeriod = elements.size() / numReps;
        for (int i = 0; i < elements.size(); i++) {
            int result = elements.poll();
            if (i % repPeriod == 0) {
                reps.add((double)result);
            }
        }
        // Get rid of padding
        // reps.remove(reps.size() - 1);
        // reps.remove(0);
        return reps;
    }


    @Override
    public void fit(BufferedImage image, int[]bits) {
        int width = image.getWidth();
        int height = image.getHeight();

        PriorityQueue<Integer> reds = new PriorityQueue<Integer>();
        PriorityQueue<Integer> greens = new PriorityQueue<Integer>();
        PriorityQueue<Integer> blues = new PriorityQueue<Integer>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                reds.add(r);
                greens.add(g);
                blues.add(b);
            }
        }

        redReps = fitReps(reds, bits[0]);
        for (int i = 0; i < redReps .size(); i++) {
            System.out.print(redReps.get(i) + " ");
        }
        System.out.println();
        System.out.println();

        greenReps = fitReps(greens, bits[1]);
        for (int i = 0; i < greenReps.size(); i++) {
            System.out.print(greenReps.get(i) + " ");
        }
        System.out.println();
        System.out.println();

        blueReps = fitReps(blues, bits[2]);
        for (int i = 0; i < blueReps.size(); i++) {
            System.out.print(blueReps.get(i) + " ");
        }
        System.out.println();


        super.fit(image, bits);
    }
}

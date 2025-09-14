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

        int red = (int) Math.round(findNearestDouble(r, redReps));
        int green = (int) Math.round(findNearestDouble(g, greenReps));
        int blue = (int) Math.round(findNearestDouble(b, blueReps));

        if (Math.random() > 0.9999) {
            System.out.println("r "+r+" "+red);
            System.out.println("g "+g+" "+green);
            System.out.println("b "+b+" "+blue);
            System.out.println("");

        }

        return (red << 16) | (green << 8) | blue;
    }

    private List<Double> weightedNormalizeReps(List<Double> reps) {
        int numReps = reps.size();
        int rangeSize = 256 / numReps;
        for (int i = 0; i < reps.size(); i++) {
            int rangeBottom = i * rangeSize;
            double rangeCenter = (double)rangeBottom + ((double)rangeSize / 2);
            reps.set(i, ((1 * reps.get(i)) + rangeCenter) / 2);
        }
        return reps;
    }

    private List<Double> fitReps(PriorityQueue<Integer> elements, int bits, int max) {
        int numReps = (1 << (bits + 1));
        ArrayList<Double> reps = new ArrayList<Double>();
        int repPeriod = elements.size() / numReps;
        for (int i = 0; i < elements.size(); i++) {
            int result = elements.poll();
            if (i % repPeriod == 0) {
                reps.add((double)result);
            }
        }
        reps.set(reps.size() - 1, (double)max);
        return weightedNormalizeReps(reps);
    }


    @Override
    public void fit(BufferedImage image, int[]bits) {
        int width = image.getWidth();
        int height = image.getHeight();

        PriorityQueue<Integer> reds = new PriorityQueue<Integer>();
        PriorityQueue<Integer> greens = new PriorityQueue<Integer>();
        PriorityQueue<Integer> blues = new PriorityQueue<Integer>();

        int redMax = -1000;
        int greenMax = -1000;
        int blueMax = -1000;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                if (r > redMax) {
                    redMax = r;
                }
                if (g > greenMax) {
                    greenMax = g;
                }
                if (b > blueMax) {
                    blueMax = b;
                }

                reds.add(r);
                greens.add(g);
                blues.add(b);
            }
        }


        redReps = fitReps(reds, bits[0], redMax);
        for (int i = 0; i < redReps .size(); i++) {
            System.out.print(redReps.get(i) + " ");
        }
        System.out.println();
        System.out.println();

        greenReps = fitReps(greens, bits[1], greenMax);
        for (int i = 0; i < greenReps.size(); i++) {
            System.out.print(greenReps.get(i) + " ");
        }
        System.out.println();
        System.out.println();

        blueReps = fitReps(blues, bits[2], blueMax);
        for (int i = 0; i < blueReps.size(); i++) {
            System.out.print(blueReps.get(i) + " ");
        }
        System.out.println();


        super.fit(image, bits);
    }
}

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KMeansRGBQuantizer extends RGBQuantizer {
    List<Double> redClusters;
    List<Double> greenClusters;
    List<Double> blueClusters;

    private List<Integer> getIndicesForCluster(double clusterValue, HashMap<Integer, Double> indexClusterMap) {
        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < indexClusterMap.size(); i++) {
            if (indexClusterMap.get(i) == clusterValue) {
                indices.add(i);
            }
        }

        return indices;
    }

    private List<Double> fitChannel(List<Integer> channel, int bits) {
        int k = 1 << bits;
        if (k > channel.size())
            throw new Error("Must have at least k pixels");
        // Initial set of values to represent each cluster
        List<Double> clusterReps = new ArrayList<Double>();
        for (int i = 0; i < k; i++) {
            clusterReps.add((double)channel.get(i * (channel.size() / k)));
        }
        // Maps the index of each pixel to its cluster value
        HashMap<Integer, Double> indexClusterMap = new HashMap<Integer, Double>();
        boolean willRepeat = true;
        while (willRepeat) {
            double totalChange = 0;
            indexClusterMap.clear();
            willRepeat = false;
            // Populate the map
            for (int i = 0; i < channel.size(); i++) {
                double clusterValue = findNearestDouble(channel.get(i), clusterReps);
                indexClusterMap.put(i, clusterValue);
            }
            // Adjust centroids
            for (int i = 0; i < k; i++) {
                List<Integer> indices = getIndicesForCluster(clusterReps.get(i), indexClusterMap);
                int sum = 0;
                for (int j = 0; j < indices.size(); j++) {
                    sum += channel.get(indices.get(j));
                }
                double newCentroid = (double)sum / indices.size();
                totalChange += Math.abs(clusterReps.get(i) - newCentroid);
                clusterReps.set(i, newCentroid);
            }
            willRepeat = totalChange > 5;
            System.out.println(totalChange);
        }
        System.out.println("Completed clustering");

        return clusterReps;
    }

    // We use K means to cluster values for each color.
    // K will be based on the number of bits
    @Override
    public void fit(BufferedImage image, int[] bits) {
        int width = image.getWidth();
        int height = image.getHeight();

        List<Integer> reds = new ArrayList<Integer>();
        List<Integer> greens = new ArrayList<Integer>();
        List<Integer> blues = new ArrayList<Integer>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                if (!reds.contains(pixel >> 16))
                    reds.add(pixel >> 16);
                if (!greens.contains(pixel >> 8))
                    greens.add(pixel >> 8);
                if (!blues.contains(pixel & 0xFF))
                    blues.add(pixel & 0xFF);
            }
        }
        redClusters = fitChannel(reds, bits[0]);
        blueClusters = fitChannel(blues, bits[1]);
        greenClusters = fitChannel(greens, bits[2]);

        super.fit(image, bits);
    }

    public int quantize(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // Scale each color component to the new range (0-31)
        int quantizedR = (int) Math.round(findNearestDouble(r, redClusters));
        int quantizedG = (int) Math.round(findNearestDouble(g, greenClusters));
        int quantizedB = (int) Math.round(findNearestDouble(b, blueClusters));

        // Reconstruct the quantized RGB value
        return (quantizedR << (16)) | (quantizedG << 8) | quantizedB;
    }
}

import java.util.List;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class KMeansQuantizer extends KMeansRGBQuantizer {
    private ImageDisplay imageDisplay;

    protected List<Double> yClusters;
    protected List<Double> uClusters;
    protected List<Double> vClusters;

    public KMeansQuantizer(ImageDisplay imageDisplay) {
        this.imageDisplay = imageDisplay;
    }

    private List<Double> fitChannelYUV(List<Double> channel, int bits) {
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
            willRepeat = totalChange > 3;
        }

        return clusterReps;
    }

    public void fitYUV(BufferedImage image, int[] bits) {
        int width = image.getWidth();
        int height = image.getHeight();

        List<Double> Y = new ArrayList<Double>();
        List<Double> u = new ArrayList<Double>();
        List<Double> v = new ArrayList<Double>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g  = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                double yuv[] = imageDisplay.rgbToYuv(r, g, b);
                Y.add(yuv[0]);
                u.add(yuv[1]);
                v.add(yuv[2]);
            }
        }
        // These aren't really rgb but it just follows it
        yClusters = fitChannelYUV(Y, bits[0]);
        uClusters = fitChannelYUV(u, bits[1]);
        vClusters = fitChannelYUV(v, bits[2]);
       
        isFit = true;
    }

    public double[] quantizeYUV(double yuv[]) {
        double y = yuv[0];
        double u = yuv[1];
        double v = yuv[2];

        double result[] = {0, 0, 0};
        
        result[0] = findNearestDouble(y, yClusters);
        result[1] = findNearestDouble(u, uClusters);
        result[2] = findNearestDouble(v, vClusters);

        return result;
    }
}

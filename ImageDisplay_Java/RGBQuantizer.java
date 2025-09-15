import java.util.List;
import java.awt.image.BufferedImage;

public abstract class RGBQuantizer {
    public abstract int quantize(int rgb);
    
    protected boolean isFit;
    
    public RGBQuantizer() {
        isFit = false;
    }
    public boolean getIsFit() {
        return isFit;
    }

    public void fit(BufferedImage image, int[]bits) {
        isFit = true;
    }

    protected double findNearestDouble(int value, List<Double> elements) {
        double nearestElement = elements.get(0);
        double bestDistance = Math.abs(value - nearestElement);
        for (int i = 1; i < elements.size(); i++) {
            double candidate = elements.get(i);
            double distance = Math.abs(value - candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearestElement = candidate;
            }
        }
        return nearestElement;
    }

    protected double findNearestDouble(double value, List<Double> elements) {
        double nearestElement = elements.get(0);
        double bestDistance = Math.abs(value - nearestElement);
        for (int i = 1; i < elements.size(); i++) {
            double candidate = elements.get(i);
            double distance = Math.abs(value - candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearestElement = candidate;
            }
        }
        return nearestElement;
    }

    protected double getRegionCenter(int value, List<Double> elements) {
        int regionStartIndex = -1;
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (value > elements.get(i)) {
                regionStartIndex = i;
                break;
            }
        }
        double regionStart = (regionStartIndex == -1) ? 0 : elements.get(regionStartIndex);
        double regionEnd = (regionStartIndex == (elements.size() - 1)) ? 255 : elements.get(regionStartIndex + 1);

        return (regionStart + regionEnd / 2);
    }

}

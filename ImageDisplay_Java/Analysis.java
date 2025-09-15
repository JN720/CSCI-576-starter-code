import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Analysis {
    
    private static double calculateMSE(BufferedImage original, BufferedImage modified) {
        double sum = 0;
        int width = original.getWidth();
        int height = original.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int originalPixel = original.getRGB(x, y);
                int originalR = (originalPixel >> 16) & 0xFF;
                int originalG = (originalPixel >> 8) & 0xFF;
                int originalB = originalPixel & 0xFF;
                int modifiedPixel = modified.getRGB(x, y);
                int modifiedR = (modifiedPixel >> 16) & 0xFF;
                int modifiedG = (modifiedPixel >> 8) & 0xFF;
                int modifiedB = modifiedPixel & 0xFF;
                int differenceR = originalR - modifiedR;
                int differenceG = originalG - modifiedG;
                int differenceB = originalB - modifiedB;
                sum += (differenceR * differenceR) + (differenceG * differenceG) + (differenceB * differenceB);
            }
        }

        return sum;
    }
    public static void main(String args[]) {
        ImageDisplay imageDisplay = new ImageDisplay();

        int height = 512;
        int width = 512;

        System.out.println(args[0]);
        System.out.println(args[1]);
        int N = Integer.parseInt(args[0]);
        String fileName = args[1];

		BufferedImage original = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);
        imageDisplay.readImageRGB(512, 512, fileName, original);
        for (int C = 1; C < 3; C++) {
            for (int M = 1; M < 3; M++) {
                for (int q1 = 1; q1 <= N - 2; q1++) {
                    for (int q2 = 1; q2 <= N - q1 - 1; q2++) {
                        int q3 = N - q1 - q2;
                        String modifyArgs[] = {"", String.valueOf(C), String.valueOf(M), String.valueOf(q1), String.valueOf(q2), String.valueOf(q3)};
                        BufferedImage modified = imageDisplay.modifyImage(original, modifyArgs);
                        double error = calculateMSE(original, modified);
                        System.out.println("<"+C+" "+M+" "+q1+" "+q2+" "+q3+"> "+error);
                        if (N == 4) {
                            File outputfile = new File("images/modified-"+C+"-"+M+"-"+q1+"-"+q2+"-"+q3+".png");
                            try {
                                ImageIO.write(modified, "png", outputfile);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Main {
    private static BufferedImage orgImage;
    private static BufferedImage copImage;
    private static JFrame frame;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors(); // Number of cores
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    public static void main(String[] args) {
        String fileName = args[0];
        int squareSize = Integer.parseInt(args[1]);
        char processingMode = args[2].charAt(0);

        orgImage = loadImage(fileName);
        copImage = copyImg(orgImage);
        displayFrame(copImage);

        // Process the image based on given letter
        if (processingMode == 'S') {
            singleThread(copImage, squareSize);
        } else if (processingMode == 'M') {
            multiThread(copImage, squareSize);
        }
    }
    private static BufferedImage loadImage(String filePath) {
        try {
            File inputFile = new File(filePath);
            BufferedImage orgImage = ImageIO.read(inputFile);

            // Get screen dimensions for resizing the image
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            int maxWidth = (int) dim.getWidth();
            int maxHeight = (int) dim.getHeight();

            // Getting the height and weight for resizing
            int nWidth = orgImage.getWidth();
            int nHeight = orgImage.getHeight();
            if (orgImage.getWidth() > maxWidth) {
                nWidth = maxWidth;
                nHeight = (nWidth * orgImage.getHeight()) / orgImage.getWidth();
            }
            if (nHeight > maxHeight) {
                nHeight = maxHeight;
                nWidth = (nHeight * orgImage.getWidth()) / orgImage.getHeight();
            }

            // Resize the image
            Image resImage = orgImage.getScaledInstance(nWidth, nHeight, Image.SCALE_DEFAULT);
            BufferedImage resizedBufferedImage = new BufferedImage(nWidth, nHeight, BufferedImage.TYPE_INT_RGB);
            Graphics g = resizedBufferedImage.getGraphics();
            g.drawImage(resImage, 0, 0, null);
            g.dispose();

            return resizedBufferedImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static BufferedImage copyImg(BufferedImage bi) {
        BufferedImage copImg = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        Graphics g = copImg.createGraphics();
        ((Graphics2D) g).drawRenderedImage(bi, null);
        g.dispose();
        return copImg;
    }
    private static void displayFrame(BufferedImage image) {
        if (frame == null) {
            frame = new JFrame("Image Processing");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(image.getWidth(), image.getHeight());

            JLabel label = new JLabel(new ImageIcon(image));
            frame.add(label);

            frame.setVisible(true);
        } else {
            ((ImageIcon) ((JLabel) frame.getContentPane().getComponent(0)).getIcon()).setImage(image);
            frame.repaint();
        }
    }
    private static void singleThread(BufferedImage image, int squareSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y += squareSize) {
            for (int x = 0; x < width; x += squareSize) {
                // Calculate the bounds of the square
                int sWidth = Math.min(squareSize, width - x);
                int sHeight = Math.min(squareSize, height - y);

                // Calculate the average color of the square
                int cRed = 0, cGreen = 0, cBlue = 0;
                for (int sY = y; sY < y + sHeight; sY++) {
                    for (int sX = x; sX < x + sWidth; sX++) {
                        Color pixelColor = new Color(image.getRGB(sX, sY));
                        cRed += pixelColor.getRed();
                        cGreen += pixelColor.getGreen();
                        cBlue += pixelColor.getBlue();
                    }
                }
                int nPixels = sWidth * sHeight;
                int avgRed = cRed / nPixels;
                int avgGreen = cGreen / nPixels;
                int avgBlue = cBlue / nPixels;

                // Set the color of the square to the average color
                Color avColor = new Color(avgRed, avgGreen, avgBlue);
                for (int sY = y; sY < y + sHeight; sY++) {
                    for (int sX = x; sX < x + sWidth; sX++) {
                        image.setRGB(sX, sY, avColor.getRGB());
                    }
                }

                displayFrame(image);

                // Add a  delay to see the progress
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Save the resulting image to a file
        try {
            File outputFile = new File("result.jpg");
            ImageIO.write(image, "jpg", outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void multiThread(BufferedImage image, int squareSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int numCores = Runtime.getRuntime().availableProcessors();
        int rowsPerCore = height / numCores;

        // Using a CompletionService for managing parallel tasks
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);

        for (int core = 0; core < numCores; core++) {
            final int coreIndex = core;

            completionService.submit(() -> {
                // Calculate the starting and ending positions for this core
                int startY = coreIndex * rowsPerCore;
                int endY = (coreIndex + 1) * rowsPerCore;

                for (int y = startY; y < endY; y += squareSize) {
                    for (int x = 0; x < width; x += squareSize) {
                        int startX = x;

                        // Calculate the bounds of the square
                        int sWidth = Math.min(squareSize, width - startX);
                        int sHeight = Math.min(squareSize, height - y);

                        // Calculate the average color of the square
                        int cRed = 0, cGreen = 0, cBlue = 0;
                        for (int sY = y; sY < y + sHeight; sY++) {
                            for (int sX = startX; sX < startX + sWidth; sX++) {
                                Color pixelColor = new Color(image.getRGB(sX, sY));
                                cRed += pixelColor.getRed();
                                cGreen += pixelColor.getGreen();
                                cBlue += pixelColor.getBlue();
                            }
                        }
                        int nPixels = sWidth * sHeight;
                        int avgRed = cRed / nPixels;
                        int avgGreen = cGreen / nPixels;
                        int avgBlue = cBlue / nPixels;

                        // Set the color of the  square to the average color
                        Color avColor = new Color(avgRed, avgGreen, avgBlue);
                        for (int sY = y; sY < y + sHeight; sY++) {
                            for (int sX = startX; sX < startX + sWidth; sX++) {
                                image.setRGB(sX, sY, avColor.getRGB());
                            }
                        }
                        displayFrame(image);

                        // Add a delay to see the progress
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, null);
        }

        // Process results
        for (int i = 0; i < numCores; i++) {
            try {
                completionService.take().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        displayFrame(image);

        // Save the resulting image to a file
        try {
            File outputFile = new File("result.jpg");
            ImageIO.write(image, "jpg", outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }
}
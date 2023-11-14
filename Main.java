import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Main {
    private static BufferedImage originalImage;
    private static BufferedImage processedImage;
    private static JFrame frame;
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors(); // Number of cores
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    public static void main(String[] args) {
        String fileName = args[0];
        int squareSize = Integer.parseInt(args[1]);
        char processingMode = args[2].charAt(0);

        originalImage = loadImage(fileName);
        processedImage = copyImg(originalImage);
        displayFrame(processedImage);

        // Process the image based on given letter
        if (processingMode == 'S') {
            processSingleThreaded(processedImage, squareSize);
        } else if (processingMode == 'M') {
            processMultiThreaded(processedImage, squareSize);
        } else {
            System.out.println("Invalid processing mode. Use 'S' for single-threaded or 'M' for multi-threaded.");
            System.exit(1);
        }
    }
    private static BufferedImage loadImage(String filePath) {
        try {
            File inputFile = new File(filePath);
            BufferedImage originalImage = ImageIO.read(inputFile);

            // Get screen dimensions for resizing the image
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int maxWidth = (int) screenSize.getWidth();
            int maxHeight = (int) screenSize.getHeight();

            // Getting the height and weight for resizing
            int newWidth = originalImage.getWidth();
            int newHeight = originalImage.getHeight();
            if (originalImage.getWidth() > maxWidth) {
                newWidth = maxWidth;
                newHeight = (newWidth * originalImage.getHeight()) / originalImage.getWidth();
            }
            if (newHeight > maxHeight) {
                newHeight = maxHeight;
                newWidth = (newHeight * originalImage.getWidth()) / originalImage.getHeight();
            }

            // Resize the image
            Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
            BufferedImage resizedBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics g = resizedBufferedImage.getGraphics();
            g.drawImage(resizedImage, 0, 0, null);
            g.dispose();

            return resizedBufferedImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
    private static void processSingleThreaded(BufferedImage image, int squareSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y += squareSize) {
            for (int x = 0; x < width; x += squareSize) {
                // Calculate the bounds of the square
                int squareWidth = Math.min(squareSize, width - x);
                int squareHeight = Math.min(squareSize, height - y);

                // Calculate the average color of the square
                int cRed = 0, cGreen = 0, cBlue = 0;
                for (int squareY = y; squareY < y + squareHeight; squareY++) {
                    for (int squareX = x; squareX < x + squareWidth; squareX++) {
                        Color pixelColor = new Color(image.getRGB(squareX, squareY));
                        cRed += pixelColor.getRed();
                        cGreen += pixelColor.getGreen();
                        cBlue += pixelColor.getBlue();
                    }
                }
                int nPixels = squareWidth * squareHeight;
                int avgRed = cRed / nPixels;
                int avgGreen = cGreen / nPixels;
                int avgBlue = cBlue / nPixels;

                // Set the color of the square to the average color
                Color avgColor = new Color(avgRed, avgGreen, avgBlue);
                for (int squareY = y; squareY < y + squareHeight; squareY++) {
                    for (int squareX = x; squareX < x + squareWidth; squareX++) {
                        image.setRGB(squareX, squareY, avgColor.getRGB());
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

        System.out.println("Result saved to 'result.jpg'.");
    }
    private static void processMultiThreaded(BufferedImage image, int squareSize) {
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
                        int squareWidth = Math.min(squareSize, width - startX);
                        int squareHeight = Math.min(squareSize, height - y);

                        // Calculate the average color of the square
                        int cRed = 0, cGreen = 0, cBlue = 0;
                        for (int squareY = y; squareY < y + squareHeight; squareY++) {
                            for (int squareX = startX; squareX < startX + squareWidth; squareX++) {
                                Color pixelColor = new Color(image.getRGB(squareX, squareY));
                                cRed += pixelColor.getRed();
                                cGreen += pixelColor.getGreen();
                                cBlue += pixelColor.getBlue();
                            }
                        }
                        int nPixels = squareWidth * squareHeight;
                        int avgRed = cRed / nPixels;
                        int avgGreen = cGreen / nPixels;
                        int avgBlue = cBlue / nPixels;

                        // Set the color of the  square to the average color
                        Color avgColor = new Color(avgRed, avgGreen, avgBlue);
                        for (int squareY = y; squareY < y + squareHeight; squareY++) {
                            for (int squareX = startX; squareX < startX + squareWidth; squareX++) {
                                image.setRGB(squareX, squareY, avgColor.getRGB());
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

        System.out.println("Result saved to 'result.jpg'.");
        executorService.shutdown();
    }
    private static BufferedImage copyImg(BufferedImage bi) {
        BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        Graphics g = copy.createGraphics();
        ((Graphics2D) g).drawRenderedImage(bi, null);
        g.dispose();
        return copy;
    }
}
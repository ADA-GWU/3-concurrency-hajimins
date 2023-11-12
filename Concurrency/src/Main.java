import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import javax.swing.*;


public class Main {

    private static BufferedImage originalImage;
    private static BufferedImage processedImage;
    private static JFrame frame;

    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors(); // Number of available cores

    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java ImageProcessor <file_name> <square_size> <processing_mode>");
            System.exit(1);
        }

        String fileName = args[0];
        int squareSize = Integer.parseInt(args[1]);
        char processingMode = args[2].charAt(0);

        // Load the original image
        originalImage = loadImage(fileName);

        // Create a copy of the original image for processing
        processedImage = deepCopy(originalImage);

        // Display the frame
        displayFrame(processedImage);

        // Process the image based on the given mode
        if (processingMode == 'S') {
            processSingleThreaded(processedImage, squareSize);
        } else if (processingMode == 'M') {
            processMultiThreadedEqualDistributionWithVisualization(processedImage, squareSize);
        } else {
            System.out.println("Invalid processing mode. Use 'S' for single-threaded or 'M' for multi-threaded.");
            System.exit(1);
        }
    }

    private static BufferedImage loadImage(String fileName) {
        try {
            return ImageIO.read(new File(fileName));
        } catch (IOException e) {
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
                int totalRed = 0, totalGreen = 0, totalBlue = 0;
                for (int squareY = y; squareY < y + squareHeight; squareY++) {
                    for (int squareX = x; squareX < x + squareWidth; squareX++) {
                        Color pixelColor = new Color(image.getRGB(squareX, squareY));
                        totalRed += pixelColor.getRed();
                        totalGreen += pixelColor.getGreen();
                        totalBlue += pixelColor.getBlue();
                    }
                }
                int numPixels = squareWidth * squareHeight;
                int avgRed = totalRed / numPixels;
                int avgGreen = totalGreen / numPixels;
                int avgBlue = totalBlue / numPixels;

                // Set the color of the entire square to the average color
                Color avgColor = new Color(avgRed, avgGreen, avgBlue);
                for (int squareY = y; squareY < y + squareHeight; squareY++) {
                    for (int squareX = x; squareX < x + squareWidth; squareX++) {
                        image.setRGB(squareX, squareY, avgColor.getRGB());
                    }
                }

                // Update the frame to display the modified image
                displayFrame(image);

                // Add a short delay to see the progress (adjust as needed)
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Save the resulting image to a file (you might want to change the file name)
        try {
            File outputFile = new File("result.jpg");
            ImageIO.write(image, "jpg", outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Image processing complete. Result saved to 'result.jpg'.");
    }

    private static void processMultiThreadedEqualDistribution(BufferedImage image, int squareSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int numCores = Runtime.getRuntime().availableProcessors();

        // Calculate the number of rows each core should process
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
                        int totalRed = 0, totalGreen = 0, totalBlue = 0;
                        for (int squareY = y; squareY < y + squareHeight; squareY++) {
                            for (int squareX = startX; squareX < startX + squareWidth; squareX++) {
                                Color pixelColor = new Color(image.getRGB(squareX, squareY));
                                totalRed += pixelColor.getRed();
                                totalGreen += pixelColor.getGreen();
                                totalBlue += pixelColor.getBlue();
                            }
                        }
                        int numPixels = squareWidth * squareHeight;
                        int avgRed = totalRed / numPixels;
                        int avgGreen = totalGreen / numPixels;
                        int avgBlue = totalBlue / numPixels;

                        // Set the color of the entire square to the average color
                        Color avgColor = new Color(avgRed, avgGreen, avgBlue);
                        for (int squareY = y; squareY < y + squareHeight; squareY++) {
                            for (int squareX = startX; squareX < startX + squareWidth; squareX++) {
                                image.setRGB(squareX, squareY, avgColor.getRGB());
                            }
                        }

                        // Add a short delay to see the progress (adjust as needed)
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, null);
        }

        // Process results as soon as they become available
        for (int i = 0; i < numCores; i++) {
            try {
                completionService.take().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Update the frame to display the modified image
        displayFrame(image);

        // Save the resulting image to a file (you might want to change the file name)
        try {
            File outputFile = new File("result_multi_threaded_equal_distribution.jpg");
            ImageIO.write(image, "jpg", outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Image processing complete. Result saved to 'result_multi_threaded_equal_distribution.jpg'.");

        // Shutdown the executor service
        executorService.shutdown();
    }


    private static void processMultiThreadedEqualDistributionWithVisualization(BufferedImage image, int squareSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int numCores = Runtime.getRuntime().availableProcessors();

        // Calculate the number of rows each core should process
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
                        int totalRed = 0, totalGreen = 0, totalBlue = 0;
                        for (int squareY = y; squareY < y + squareHeight; squareY++) {
                            for (int squareX = startX; squareX < startX + squareWidth; squareX++) {
                                Color pixelColor = new Color(image.getRGB(squareX, squareY));
                                totalRed += pixelColor.getRed();
                                totalGreen += pixelColor.getGreen();
                                totalBlue += pixelColor.getBlue();
                            }
                        }
                        int numPixels = squareWidth * squareHeight;
                        int avgRed = totalRed / numPixels;
                        int avgGreen = totalGreen / numPixels;
                        int avgBlue = totalBlue / numPixels;

                        // Set the color of the entire square to the average color
                        Color avgColor = new Color(avgRed, avgGreen, avgBlue);
                        for (int squareY = y; squareY < y + squareHeight; squareY++) {
                            for (int squareX = startX; squareX < startX + squareWidth; squareX++) {
                                image.setRGB(squareX, squareY, avgColor.getRGB());
                            }
                        }

                        // Update the frame to display the modified image
                        displayFrame(image);

                        // Add a short delay to see the progress (adjust as needed)
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, null);
        }

        // Process results as soon as they become available
        for (int i = 0; i < numCores; i++) {
            try {
                completionService.take().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Update the frame to display the final modified image
        displayFrame(image);

        // Save the resulting image to a file (you might want to change the file name)
        try {
            File outputFile = new File("result_multi_threaded_equal_distribution_with_visualization.jpg");
            ImageIO.write(image, "jpg", outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Image processing complete. Result saved to 'result_multi_threaded_equal_distribution_with_visualization.jpg'.");

        // Shutdown the executor service
        executorService.shutdown();
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        Graphics g = copy.createGraphics();
        ((Graphics2D) g).drawRenderedImage(bi, null);
        g.dispose();
        return copy;
    }
}

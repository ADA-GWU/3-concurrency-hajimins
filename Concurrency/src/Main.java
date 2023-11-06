import static java.lang.Integer.parseInt;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Number of Command Line Argument = "+args.length);
        String fileName = args[0];
        int squareSize = parseInt(args[1]);
        String processingMode = args[2];
        System.out.println(args[0]+args[1]+args[2]);

        BufferedImage img=ImageIO.read(new File("monalisa.jpg"));
        ImageIcon icon=new ImageIcon(img);
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(500,756);
        JLabel lbl=new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
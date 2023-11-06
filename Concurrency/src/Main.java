import static java.lang.Integer.parseInt;

public class Main {
    public static void main(String[] args) {
        System.out.println("Number of Command Line Argument = "+args.length);
        String fileName = args[0];
        int squareSize = parseInt(args[1]);
        String processingMode = args[2];
        System.out.println(args[0]+args[1]+args[2]);
    }
}
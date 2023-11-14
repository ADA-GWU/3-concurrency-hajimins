# Assignment2 - Image Processing Project
This project is a simple image processing application written in Java. It allows you to read an image, resize it to fit the computer screen, and perform multi-threaded image processing with visualization, and save the image to a file. Firstly we read the image using the command line, and choose the mode we want to raster the image and see while the process happens. Here S is used for Single Threaded processes, while M is used for Multi Threaded processes.

## How to run the program
<b>1.</b> Make sure that Java Development Kit(JDK) is installed. Simply enter <b>java -version</b> to check it.

<b>2.</b>  Clone the repository to your local machine:
    git clone https://github.com/ADA-GWU/3-concurrency-hajimins.git
    
<b>3.</b>  Open the terminal and navigate to the directory where the project locates using cd.

<b>4.</b> Compile the java file using              <b>javac Main.java</b>

<b>5.</b> Run the project with the given command. Here the arguments are name of the project, name of the image, square size, and thread mode respectively.

<b>java Main monalisa.jpg 5 S</b> for single threaded

<b>java Main monalisa.jpg 5 M</b> for multi threaded
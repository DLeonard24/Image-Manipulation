import java.io.IOException;

public class run {
    public static void main(String [] args)throws IOException{

        Image img = new Image("Taj Mahal.jpg");
        ImageFolder folder = new ImageFolder("squareFlowers");

        img.photomosaic(25, folder.getFolder());
        img.toFile("Taj Mahal photomosaic.jpg");

        
    }
}

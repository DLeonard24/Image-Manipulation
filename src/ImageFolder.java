import java.io.File;
import java.io.IOException;

public class ImageFolder {
    
    private File [] folder;

    ImageFolder(String path){
        folder = new File(path).listFiles();
    }

    public File[] getFolder(){
        return folder;
    }

    //squares all images in a folder and outputs to the specified output
    public void square(int length, String output) throws IOException{
        for(File filename : folder){
            Image a = new Image(filename);
            a.square(length);
            String f = "";
            int b = (filename.toString()).lastIndexOf('\\');
            f = (filename.toString()).substring(b + 1);
            a.toFile(output + f);
        }
    }
}

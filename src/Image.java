import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class Image {
    
    private BufferedImage img;
    private String format;
    private int height;
    private int width;

    Image(BufferedImage image){
        img = image;
        height = img.getHeight();
        width = img.getWidth();
    }

    Image(String image)throws IOException{
        File f = new File(image);
        img = ImageIO.read(f);
        height = img.getHeight();
        width = img.getWidth();
        format = Image.findFormat(image);
    }

    Image(File f)throws IOException{
        img = ImageIO.read(f);
        height = img.getHeight();
        width = img.getWidth();
        format = Image.findFormat(f.toString());
    }

    public BufferedImage getImg(){
        return img;
    }

    public String getFormat(){
        return format;
    }

    public int getHeight(){
        return height;
    }

    public int getWidth(){
        return width;
    }

    //gets the format of an image from a file path
    public static String findFormat(String path){
        String format = "";
        int a = path.lastIndexOf('.');
        format = path.substring(a + 1);
        return format;
    }

    //resizes based off a percentage
    public void resize(double percent)throws IOException{
        width = (int) (width * percent);
        height = (int) (height * percent);
        this.resize(height, width);
    }

    //resizes based ooff new dimensions
    public void resize(int newWidth, int newHeight){
        BufferedImage original = img;
        img = new BufferedImage(newWidth, newHeight, img.getType());
        Graphics g2d = img.createGraphics();
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        this.height = newHeight;
        this.width = newWidth;
    }

    //checks if an image should be resized
    //useful for ASCII convert, big images dont look right in text
    private void reSizeCheck()throws IOException{
        while(width > 500 || height > 500){
            resize(0.9);
            System.out.println("width is " + width + " height is " + height);
        }
    }
    
    //finds the avg rgb value of a pixel and returns a string based of light pixel
    static String ASCIIPixel(int p){      
        int r = (p>>16)&0xff;
        int g = (p>>8)&0xff;
        int b = p&0xff;

        int avg = (r+g+b)/3;
    return ASCIIvalue(avg);
    }
    
    //takes in the rgb value, the brighter it is, the brighter the string it returns
    private static String ASCIIvalue(int g) {
        String str = " ";
        if (g >= 240) {
            str = " ";
        } else if (g >= 210) {
            str = ".";
        } else if (g >= 190) {
            str = "*";
        } else if (g >= 170) {
            str = "+";
        } else if (g >= 120) {
            str = "^";
        } else if (g >= 110) {
            str = "&";
        } else if (g >= 80) {
            str = "8";
        } else if (g >= 60) {
            str = "#";
        } else {
            str = "@";
        }
    return str;
    }

    //takes an Image, and returns a string of it turned into ASCII, not that useful
    public String toASCII()throws IOException{
        String ASCII = "";
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
              int p = img.getRGB(j,i);
      
              ASCII += ASCIIPixel(p);
            }
            ASCII += "/n";
        }
    return ASCII;
    }

    //turns an Image into ASCII, and outputs it to a txt file at the location
    //specified by the output String
    public void toASCII(String output)throws IOException{  
        File a = new File(output);
        PrintStream outToFile = new PrintStream(a);
        reSizeCheck();
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
              int p = img.getRGB(j,i);
      
              outToFile.print(ASCIIPixel(p));
            }
            outToFile.print("\n");
        }
        outToFile.close();
    }

    //pixelates an Image, pixelsize determines how much the image is split up
    public void pixelate(int pixelSize){
        for(int i = 0; i < width; i+=pixelSize){
            for(int j = 0; j < height; j+=pixelSize){
                Color pixelColor = new Color(img.getRGB(i, j));
                Graphics graphic = img.getGraphics();
                graphic.setColor(pixelColor);
                graphic.fillRect(i, j, pixelSize, pixelSize);
            }
        }
    }

    //pixelates without specifying the size of new pixels
    public void pixelate(){
        int pixelSize = (int) (height * 0.03);
        pixelate(pixelSize);
    }

    //outputs an Image to the specified file path
    public void toFile(String output)throws IOException{
        File a = new File(output);
        ImageIO.write(img, format, a);
    }

    //squares an Image to the specifeid length
    public void square(int length){
        this.resize(length, length);
    }

    //loads the cache to use for source images
    public void photomosaic(int pixelSize)throws IOException{
        imageCache a = new imageCache();
        a.loadCache();
        HashMap<Color, BufferedImage> hm = a.getImageCache(); //loads the cache into a hasmap
        for(int i = 0; i < width; i+=pixelSize){
            for(int j = 0; j < height; j+=pixelSize){
                Color pixelColor = new Color(img.getRGB(i, j)); //gets the color of this pixel of the Image
                double distance = 255; // set arbitrarily high
                BufferedImage b = new BufferedImage(100, 100, 1); //set arbitraarily
                for(HashMap.Entry<Color, BufferedImage> color : hm.entrySet()){//iterates throught the hash map
                    Color c = color.getKey();                                  //to find the closest match
                    if(colorDiff(pixelColor, c) < distance){
                        distance = colorDiff(pixelColor, c);
                        b = color.getValue();
                    }
                }
                Graphics graphic = img.getGraphics();
                graphic.drawImage(b, i, j, pixelSize, pixelSize, null);
                //draws the closest matching image onto the Image, resized to fit the pixelsize
            }
        }
    }

    //creates photomosaic of Image by passing in an array of Files
    public void photomosaic(int pixelSize, File[] file)throws IOException{
        HashMap<Color, BufferedImage> hm = processImages(file); //turns the Array of files into a hashmap
        imageCache a = new imageCache(hm);
        a.saveCache(); //saves the source images to the cache to be used later
        for(int i = 0; i < width; i+=pixelSize){
            for(int j = 0; j < height; j+=pixelSize){
                Color pixelColor = new Color(img.getRGB(i, j));
                double distance = 255;
                BufferedImage b = new BufferedImage(100, 100, 1); 
                for(HashMap.Entry<Color, BufferedImage> color : hm.entrySet()){
                    Color c = color.getKey();
                    if(colorDiff(pixelColor, c) < distance){
                        distance = colorDiff(pixelColor, c);
                        b = color.getValue();
                    }
                }
                Graphics graphic = img.getGraphics();
                graphic.drawImage(b, i, j, pixelSize, pixelSize, null);
            }
        }
    }
    
    //finds the avg RGB value of an Image
    public Color avgRGB(){
        int n = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                int p = img.getRGB(i, j);
                r += (p>>16)&0xff;
                g += (p>>8)&0xff;
                b += p&0xff;
                n++;
            }
        }
        r /= n;
        g /= n;
        b /= n;
    return new Color(r, g, b);
    }  
    
    //finds the distance of two colors using Pythagoras's theorem
    public double colorDiff(Color a, Color b){
        double Red = Math.pow(Math.abs(a.getRed() - b.getRed()), 2);
        double Green = Math.pow(Math.abs(a.getGreen() - b.getGreen()), 2);
        double Blue = Math.pow(Math.abs(a.getBlue() - b.getBlue()), 2);

        double distance = Math.sqrt((Red + Green + Blue));
    return distance;
    }

    //turns an array of files into a hashmap
    public HashMap<Color, BufferedImage> processImages(File[] file) throws IOException{
        HashMap<Color, BufferedImage> hm = new HashMap<>();
        for(File filename : file){
            Image a = new Image(filename);
            Color temp = a.avgRGB();
            hm.put(temp, a.getImg());
        }
    return hm;
    }
}

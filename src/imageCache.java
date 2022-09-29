import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class imageCache {
    
    private HashMap<Color, BufferedImage> imageCache;

    private String cacheLocation = "pictures.map";

    imageCache(HashMap<Color, BufferedImage> images){
        imageCache = images;
    }

    //constructor without specifying any fields
    imageCache(){
    }

    //writes a hashmap to cache
    public void writeCache(ObjectOutputStream oos, HashMap<Color, BufferedImage> data) throws IOException {
        oos.writeInt(data.size());
        for (Entry<Color, BufferedImage> entry : data.entrySet()) {   
            oos.writeObject(entry.getKey());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(entry.getValue(), "png", baos);
            byte[] bytes = baos.toByteArray();
            oos.writeObject(bytes);
        }
    }

    //reads the cache into a hashmap
    public HashMap<Color, BufferedImage> readCache(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        int size = ois.readInt();
        HashMap<Color, BufferedImage> result = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Color color = (Color) (ois.readObject());
            ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) ois.readObject());
            BufferedImage image = ImageIO.read(bais);            
            result.put(color, image);
        }
        return result;
    }

    //loads the cache to this imageCache object
    public void loadCache() {
        imageCache = new HashMap<>();
        File file = new File(cacheLocation);
        if (file.isFile()) {
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(file);
                ois = new ObjectInputStream(fis);
                imageCache = readCache(ois);
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(imageCache.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    ois.close();
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(imageCache.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        System.out.println("Cache loaded with " + imageCache.size() + " elements");
    }

    //writes imageCache into the cache
    public void saveCache() {
        File file = new File(cacheLocation);
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            if (file.isFile()) {
                file.delete();
            }
            file.createNewFile();
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            writeCache(oos, imageCache);
        } catch (IOException ex) {
            Logger.getLogger(imageCache.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                System.out.println("Cache saved with " + imageCache.size() + " elements");
                oos.close();
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(imageCache.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean contains(Color color) {
        return imageCache.containsKey(color);
    }

    public BufferedImage get(Color color) {
        return imageCache.get(color);
    }

    public void put(Color color, BufferedImage img) {
        imageCache.put(color, img);
    }

    public HashMap<Color, BufferedImage> getImageCache(){
        return imageCache;
    }
}

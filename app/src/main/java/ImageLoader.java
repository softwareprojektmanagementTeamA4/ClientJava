import javafx.scene.image.Image;

import java.io.File;
import java.util.HashMap;

public class ImageLoader {

    public HashMap<String, Image> loadImagesFromFolder(String folderPath) {
        HashMap<String, Image> images = new HashMap<>();

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Der angegebene Pfad ist ungültig oder kein Ordner.");
            return images;
        }

        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                    Image image = new Image("file:" + file.getPath());
                    images.put(nameWithoutExtension, image);
                }
            }
        }

        return images;
    }
}
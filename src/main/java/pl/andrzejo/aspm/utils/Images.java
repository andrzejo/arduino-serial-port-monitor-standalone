package pl.andrzejo.aspm.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Images {

    public static BufferedImage fromResource(String path) {
        BufferedImage myPicture = null;
        try {
            return ImageIO.read(Files.fromResource(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

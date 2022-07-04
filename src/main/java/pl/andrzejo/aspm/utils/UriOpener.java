package pl.andrzejo.aspm.utils;

import java.awt.*;
import java.net.URI;

public class UriOpener {

    public static void open(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                //todo logger
            }
        }
    }
}

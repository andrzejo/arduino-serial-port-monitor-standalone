/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2022.
 */

package pl.andrzejo.aspm.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Images {

    public static BufferedImage fromResource(String path) {
        try {
            return ImageIO.read(AppFiles.resourceAsStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

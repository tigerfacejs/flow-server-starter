package org.tigerface.flow.starter.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageService {
    public void discolor(InputStream is) throws IOException {
        BufferedImage img = ImageIO.read(is);
        final int width = img.getWidth();
        final int height = img.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = img.getRGB(i, j);
                img.setRGB(i, j, grayRGB(Integer.toHexString(rgb)));
            }
        }
        ImageIO.write(img, "jpg", new File("/Users/zyh/Downloads/testGray.jpg"));
    }

    private int grayRGB(String argb) {
        //ARGB前两位是透明度,后面开始是RGB
        int r = Integer.parseInt(argb.substring(2, 4), 16);
        int g = Integer.parseInt(argb.substring(4, 6), 16);
        int b = Integer.parseInt(argb.substring(6, 8), 16);
        //平均值
        String average = Integer.toHexString((r + g + b) / 3);

        if (average.length() == 1) {
            average = "0" + average;
        }
        //RGB都变成平均值
        return Integer.parseInt(average + average + average, 16);
    }
}

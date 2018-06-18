package org.epics.vtype;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.epics.util.array.ArrayByte;
import org.epics.util.array.ListNumber;
import org.junit.Test;

public class VImageTest {

    /**
     * A test for the creation of a {@link VImage} data structure using an example image
     */
    @Test
    public void of() {
        BufferedImage img = null;
        VImage vImage = null;
        boolean done = false;
        try {
            img = ImageIO.read(VType.class.getResource("Tulips.jpg"));
            vImage = VImage.of(img.getHeight(), img.getWidth(),
                    ArrayByte.of(((DataBufferByte) img.getRaster().getDataBuffer()).getData()), VImageDataType.pvByte,
                    VImageType.TYPE_3BYTE_BGR,
                    Alarm.none(), Time.now());
            BufferedImage vImg = toImage(vImage);
            for (int x = 0; x < vImage.getWidth(); x++) {
                for (int y = 0; y < vImage.getHeight(); y++) {
                    assertEquals(img.getRGB(x, y), vImg.getRGB(x, y));
                }
            }
            done = true;
        } catch (IOException e) {
        } finally {
            if (!done) {
                BufferedImage bf = toImage(vImage);
                try {
                    ImageIO.write(bf, "png", new File("src/test/resources/org/diirt/vtype/Tuplips-failed.jpg"));
                } catch (IOException e) {
                }
            }
        }
    }

    private static BufferedImage toImage(VImage vImage) {
        if (vImage.getVImageType() == VImageType.TYPE_3BYTE_BGR) {
            BufferedImage image = new BufferedImage(vImage.getWidth(), vImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            ListNumber data = vImage.getData();
            for (int i = 0; i < data.size(); i++) {
                ((DataBufferByte) image.getRaster().getDataBuffer()).getData()[i] = data.getByte(i);
            }
            return image;
        } else {
            throw new UnsupportedOperationException(
                    "No support for creating a BufferedImage from Image Type: " + vImage.getVImageType());
        }
    }
}

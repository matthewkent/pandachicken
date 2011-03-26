package org.pandachicken;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

public class SlapTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BufferedImage source = ImageIO.read(new FileInputStream(new File("images/panda.jpg")));
		BufferedImage dest = ImageIO.read(new FileInputStream(new File("images/chicken.jpg")));
		
		Slapper slapper = new Slapper();
		BufferedImage result = slapper.slap(source, dest);

		ImageIO.write(result, "jpg", new File("tmp/pandachicken.jpg"));
	}

}

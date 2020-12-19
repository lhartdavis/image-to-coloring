import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageTools {

	public static void main(String[] args) {
		
		BufferedImage img = ImageTools.loadImage("images/moon.jpg");
		ImageTools.desaturate(img);
		ImageTools.saveImage(img, "output/moon-desat.jpg");

	}
	
	//loads image from drive
	public static BufferedImage loadImage(String filepath) {
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File(filepath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}
	
	//desaturates image by averaging RGB values
	public static void desaturate(BufferedImage img) {
		for(int x = 0; x < img.getWidth(); x++) {
			for(int y = 0; y < img.getHeight(); y++) {
				Color pixel = new Color(img.getRGB(x, y));
				int avrVal = (pixel.getRed()+pixel.getGreen()+pixel.getBlue())/3;
				img.setRGB(x, y, 0x10000*avrVal+0x100*avrVal+avrVal);
			}
		}
	}
	
	//saves image to disk
	public static void saveImage(BufferedImage img, String filepath) {
		File outputfile = new File(filepath);
		try {
			ImageIO.write(img, "jpg", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}

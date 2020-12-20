import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

//creates a k-color palette version of an image
//colors are stored as integers
public class ImageKMeans {
	
	BufferedImage img;
	int k;
	int[] palette;
	ArrayList<Double> WCSS;
	int[][] cluster;
	boolean[] unusedClusters;
	
	ImageKMeans(BufferedImage _img, int _k){
		k = _k;
		img = _img;
		WCSS = new ArrayList<Double>();
		palette = new int[k];
		initializePalette();
		cluster = new int[img.getWidth()][img.getHeight()];
		unusedClusters = new boolean[k];
	}
	
	//print line : shorter to type :p
	private void sop(String s) {
		System.out.println(s);
	}
	
	//initializes palette with random colors
	private void initializePalette() {
		Random r = new Random();
		for(int i = 0 ; i < k; i++) {
			palette[i] = img.getRGB((int)(r.nextFloat()*img.getWidth()), (int)(r.nextFloat()*img.getHeight()));
		}
		
		//checks if there is two times the same color
		boolean ok = true;
		for(int i = 0; i < k; i ++) {
			for(int j = i+1; j < k; j++) {
				if(palette[i] == palette[j]) {
					ok = false;
				}
			}
		}
		
		if(!ok) {
			initializePalette();
		}
		
	}
	
	//returns sum-of-squares distance between two colors
	private int dist(int colorA, int colorB) {
		Color a = new Color(colorA);
		Color b = new Color(colorB);
		return (a.getRed() - b.getRed())*(a.getRed() - b.getRed()) + (a.getGreen() - b.getGreen())*(a.getGreen() - b.getGreen()) + (a.getBlue() - b.getBlue())*(a.getBlue() - b.getBlue());
	}
	
	//returns closest color index
	private int closestColor(int color) {
		int minIndex = 0;
		int minValue = dist(color, palette[minIndex]);
		for(int i = 1; i < k; i++) {
			int d = dist(color, palette[i]);
			if(d < minValue) {
				d = minValue;
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	//fills the cluster matrix with closest color
	private void clusterize() {
		for(int x = 0; x < img.getWidth(); x++) {
			for(int y = 0; y < img.getHeight(); y++) {
				cluster[x][y] = closestColor(img.getRGB(x, y));
			}
		}
	}
	
	//calculate clusters average color and updates palette
	private void updatePalette() {
		int[][] sums = new int[k][3];
		int[] counts = new int[k];

		for(int x = 0; x < img.getWidth(); x++) {
			for(int y = 0; y < img.getHeight(); y++) {
				int clusterNumber = cluster[x][y];
				Color pixelColor = new Color(img.getRGB(x, y));
				sums[clusterNumber][0] += pixelColor.getRed();
				sums[clusterNumber][1] += pixelColor.getGreen();
				sums[clusterNumber][2] += pixelColor.getBlue();
				counts[clusterNumber]++;
			}
		}
		
		//compute average and update
		for(int i = 0; i < k; i++) {
			if(counts[i] > 0) {
				palette[i] = 65536 * (int)(sums[i][0]/counts[i]) + 256 * (int)(sums[i][1]/counts[i]) + (int)(sums[i][2]/counts[i]);
				unusedClusters[i] = false;
			}else {
				//sop("Palette color " + Integer.toString(i) + " has no associated pixel"); // --> try to shuffle
				unusedClusters[i] = true;
			}
		}
	}
	
	private void reinitUnusedPaletteColors() {
		Random r = new Random();
		
		
		for(int i = 0; i < k; i++) {
			if(unusedClusters[i]) {
				
				//choose random used palette color
				int index = 0;
				do {
					index = (int)(r.nextFloat()*k);
				}while(unusedClusters[index]);
				int color = palette[index];

				//tweek it randomly
				if(r.nextBoolean()) {
					color += 1;
				}
				if(r.nextBoolean()) {
					color += 256;
				}
				if(r.nextBoolean()) {
					color += 65536;
				}
				
				palette[i] = color;
				if(r.nextFloat() < 0.1) {
					palette[i] = img.getRGB((int)(r.nextFloat()*img.getWidth()), (int)(r.nextFloat()*img.getHeight()));
				}
			}
		}
		
		
		
	
	}

	private int getUnusedClusterCount() {
		int s = 0;
		for(int i = 0; i < k; i++) {
			if(unusedClusters[i]) {
				s += 1;
			}
		}
		return s;
	}
	
	private String getUnusedClusterString() {
		int counter = 0;
		String s = "";
		for(int i = 0; i < k; i++) {
			if(unusedClusters[i]) {
				s += "1";
				counter++;
			}else {
				s += "0";
			}
		}
		return s + " : " + Integer.toString(counter);
	}
	
	//compute sum of within cluster sum of distances to cluster palette color
	private void computeWCSS() {
		
		double s = 0;
		
		for(int x = 0; x < img.getWidth(); x++) {
			for(int y = 0; y < img.getHeight(); y++) {
				int clusterNumber = cluster[x][y];
				s += dist(palette[clusterNumber], img.getRGB(x, y));
			}
		}
		
		WCSS.add(s);
	}
	
	//convergence test
	private boolean hasConverged() {
		if(WCSS.size() >= 2) {
			if(Math.abs(WCSS.get(WCSS.size()-1) - WCSS.get(WCSS.size()-2)) < 1000) {
				return true;
			}
		}
		return false;
	}
	
	//paint a white canvas and return it
	public BufferedImage getPaintedImage() {
		BufferedImage paintedImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		for(int x = 0; x < img.getWidth(); x++) {
			for(int y = 0; y < img.getHeight(); y++) {
				paintedImage.setRGB(x, y, palette[cluster[x][y]]);
			}
		}
		
		return paintedImage;
	}
	
	public void runKMeans() {
		
		int iter = 0;
		while(!hasConverged()) {
			sop("Iteration number : " + Integer.toString(iter));
			
			do{
				reinitUnusedPaletteColors();
				clusterize();
				updatePalette();
				sop(getUnusedClusterString());
			}while((getUnusedClusterCount() > 0) || (iter > 100));
			
			if(iter > 100) {
				clusterize();
				updatePalette();
			}
			
			computeWCSS();
			if(WCSS.size() > 1) {
				sop("WCSS : " + WCSS.get(WCSS.size() -1).toString() + " delta : " + Double.toString(Math.abs(WCSS.get(WCSS.size()-1) - WCSS.get(WCSS.size()-2))));
			}else {
				sop("WCSS : " + WCSS.get(WCSS.size() -1).toString());
			}
			iter++;
		}
	}

}

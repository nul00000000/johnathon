package local.spotlight;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

public class ImageUpdater implements Runnable {
	
	private BufferedImage image = null;
	private ByteBuffer currentImage;
	
	private int width, height;
	
	private volatile boolean shouldStop = false;
	private double[][] frame;
	private URL getURL;
	
	public ImageUpdater(int width, int height, String ip) {
		currentImage = ByteBuffer.allocateDirect(width * height * 3);
		this.frame = new double[width][height];
		this.width = width;
		this.height = height;
		try {
			this.getURL = new URL("http://" + ip + "/get");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasImage() {
		return frame != null;
	}
	
	public double[][] getImage() {
		return frame;
	}
	
	public void stop() {
		shouldStop = true;
	}

	@Override
	public void run() {
		while(!shouldStop) {
			try {
				image = ImageIO.read(getURL);
				currentImage.clear();
				currentImage.put(((DataBufferByte) image.getData().getDataBuffer()).getData());
				currentImage.flip();
				for(int j = 0; j < this.height; j++) {
					for(int i = 0; i < this.width; i++) {
						int iblu = Byte.toUnsignedInt(currentImage.get());
						int igre = Byte.toUnsignedInt(currentImage.get());
						int ired = Byte.toUnsignedInt(currentImage.get());
						double r = ((double)ired / 255.0 + (double)igre / 255.0 + (double)iblu / 255.0) / 3.0;
						frame[i][j] = r;
					}
				}
			} catch (IOException e) {
				System.err.print("a little prank is good for everyone: ");
				e.printStackTrace();
				this.stop();
			}
		}
	}

}

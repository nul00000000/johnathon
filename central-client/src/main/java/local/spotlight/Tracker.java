package local.spotlight;

import java.awt.Color;
import java.awt.image.BufferedImage;

import local.spotlight.graphics.IObject;
import local.spotlight.graphics.ObjectGraphics;
import local.spotlight.input.Input;

public class Tracker implements IObject {
		
	private int width;
	private int height;
	
	private double[][] frameA;
	private double[][] frameB; //only used in motion detection (deprecated (and removed) because background-difference works better on my lawn)
	private double[][] frameD;
	private double[][] frameDMax;
	private double[][] frameDMin;
	
	private int[] xs;
	private int[] ys;
	
	private BufferedImage image;
	
	private double trackerX;
	private double trackerY;
	private int t;
	
	private int timer = 0;
	
	private Input input;
	private Spotlight light;
	
	private boolean ready = false;
		
	public Tracker(Input input, Spotlight light) {
//		this.width = 400; CIF
//		this.height = 296;
		
		this.width = Main.CAM_WIDTH;
		this.height = Main.CAM_HEIGHT;
		
		this.frameA = new double[width][height];
		this.frameB = new double[width][height];
		this.frameD = new double[width][height];
		this.frameDMax = new double[width][height];
		this.frameDMin = new double[width][height];
		
		this.xs = new int[width];
		this.ys = new int[height];
				
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		this.input = input;
		this.light = light;
	}
	
	public void close() {
		light.setPos(0, 0);
		light.setLight(false);
		light.getLog();
	}

	@Override
	public void update() {
		if(!light.hasFrame()) return;
		double[][] get = light.getFrame();
		
		for(int j = 0; j < frameA[0].length; j++) {
			for(int i = 0; i < frameA.length; i++) {
				frameB[i][j] = frameA[i][j];
				frameA[i][j] = get[width - i - 1][height - j - 1];
			}
		}

		if(input.isButtonDown(Input.LEFT_CLICK)) {
			for(int j = 0; j < frameA[0].length; j++) {
				for(int i = 0; i < frameA.length; i++) {
					if(frameA[i][j] > frameDMax[i][j]) {
						frameDMax[i][j] = frameA[i][j];
					}
					if(frameA[i][j] < frameDMin[i][j]) {
						frameDMin[i][j] = frameA[i][j];
					}
					frameD[i][j] = (frameDMax[i][j] + frameDMin[i][j]) / 2.0;
				}
			}
		}
		if(input.isButtonDown(Input.RIGHT_CLICK)) {
			for(int j = 0; j < frameA[0].length; j++) {
				for(int i = 0; i < frameA.length; i++) {
					frameD[i][j] = 0.0;
					frameDMax[i][j] = 0.0;
					frameDMin[i][j] = 1.0;
				}
			}
			System.out.println("reset");
		}
		t = 0;
		for(int i = 0; i < width; i++) {
			xs[i] = 0;
		}
		
		for(int i = 0; i < height; i++) {
			ys[i] = 0;
		}
		
		for(int j = 0; j < frameA[0].length; j++) {
			for(int i = 0; i < frameA.length; i++) {
				int iblu = (int) (frameA[i][j] * 255);
				int igre = (int) (frameA[i][j] * 255);
				int ired = (int) (frameA[i][j] * 255);
								
				int irDMax = (int) (frameDMax[i][j] * 255.0);
				int irDMin = (int) (frameDMin[i][j] * 255.0);
				
				if(frameA[i][j] - frameDMax[i][j] > 0.05 || frameDMin[i][j] - frameA[i][j] > 0.05) {
					xs[i]++;
					ys[j]++;
					t++;
				}
				
				if(input.isButtonDown(Input.LEFT_CLICK)) {
					if(i > width / 2) {
						image.setRGB(i, j, irDMax << 16 | irDMax << 8 | irDMax);
					} else {
						image.setRGB(i, j, irDMin << 16 | irDMin << 8 | irDMin);
					}
				} else {
					if(frameA[i][j] - frameDMax[i][j] > 0.05 || frameDMin[i][j] - frameA[i][j] > 0.05) { //display green on non-background pixels
						image.setRGB(i, j, 0x00ff00);
					} else {
						image.setRGB(i, j, ired << 16 | igre << 8 | iblu); //i'll implement actual color at some point (its grayscale currently)
					}
				}
			}
		}
		
		//get median x and y of pixels that do not match background
		
		int maxX = 0;
		int maxY = 0;
		
		int count = 0;
		
		for(int i = 0; i < width; i++) {
			count += xs[i];
			if(count >= t / 2) {
				maxX = i;
				break;
			}
		}
		
		count = 0;
		
		for(int i = 0; i < height; i++) {
			count += ys[i];
			if(count >= t / 2) {
				maxY = i;
				break;
			}
		}
				
		trackerX = (double) maxX / width * 2.0 - 1.0;
		trackerY = (double) maxY / height * 2.0 - 1.0;
		
		//turns on light after an object is detected long enough
		if(timer > 10 && ready) {
			light.setLight(true);
		} else {
			light.setLight(false);
		}
		
		//increases object timer if object is detected or resets timer otherwise
		if(t > 40) {
			timer++;
		} else {
			timer = 0;
		}
		
		//point light (approximately) towards detected object TODO: needs ability to calibrate (needs brighter light first)
		if(t > 40) {
			light.setPos(trackerX * -2, -trackerY);
		}
		
		//clear log (i think there could be memory issues on the esp32 if i dont do this)
		light.getLog();
	}

	@Override
	public void draw(ObjectGraphics g) {
		g.drawImage(0, 0, 1, 1, image);
		if(t > 40) { //display red circle around detected object TODO: change size based on object size
			g.setColor(Color.RED);
			g.drawCircle(trackerX, trackerY, 0.1);
		}
	}

}

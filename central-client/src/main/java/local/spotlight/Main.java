package local.spotlight;


import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URISyntaxException;

import local.spotlight.graphics.ObjectGraphics;
import local.spotlight.input.Input;

public class Main {
	
	public static boolean devenv = false;
	public static int CAM_WIDTH = 640, CAM_HEIGHT = 480;
		
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("-d")) {
				devenv = true;
			}
		}
		Input input = new Input();
		ObjectGraphics g = new ObjectGraphics("Spotlight", CAM_WIDTH, CAM_HEIGHT, (Input) input);
		
		long tick = 0;
		
		long a;
		long b = 0;
		
		int FPS_CAP = 30;
		float SPF_MIN = 1f / FPS_CAP * 1000000000;
		
		Spotlight light = new Spotlight("spotlight.local", "camera.local");
				
		Tracker tracker = new Tracker(input, light);
						
		while(!g.shouldClose()) {
			a = System.nanoTime();
						
			g.updateZoom();
			if(!input.isKeyDown(KeyEvent.VK_F) || tick % 200 == 0) {
				tracker.draw(g);
				g.drawToScreen();
			}
			input.update();
			
			b = System.nanoTime() - a;
			
			if(!input.isKeyDown(KeyEvent.VK_F) && b < SPF_MIN) {
				try {
					Thread.sleep((long)((SPF_MIN - b) / 1000000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			tick++;
		}
		g.close();
		tracker.close();
		light.close();
	}
	
}

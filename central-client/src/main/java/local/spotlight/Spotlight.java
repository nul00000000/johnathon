package local.spotlight;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class Spotlight {
	
	private HttpClient client;
	private String espIP, camIP;
	private ImageUpdater updater;
	Thread updaterThread;
	
	public Spotlight(String espIP, String camIP) {
		client = HttpClient.newHttpClient();
		this.espIP = espIP;
		this.camIP = camIP;
		this.updater = new ImageUpdater(Main.CAM_WIDTH, Main.CAM_HEIGHT, camIP);
		updaterThread = new Thread(updater);
		
		updaterThread.start();
	}
	
	public boolean hasFrame() {
		return updater.hasImage();
	}
	
	public double[][] getFrame() {
		return updater.getImage();
	}
	
	public void setLight(boolean on) {
		try {
			HttpRequest request = HttpRequest.newBuilder(
					new URI("http://" + espIP + "/set?s=" + (on ? "1" : "0"))).GET().build();
			client.send(request, BodyHandlers.ofString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setPos(double trackerX, double trackerY) {
		try {
			HttpRequest request = HttpRequest.newBuilder(
					new URI(String.format("http://" + espIP + "/set?x=%.3f&y=%.3f", trackerX, trackerY))).GET().build();
			client.send(request, BodyHandlers.ofString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void getLog() {
		try {
			HttpRequest request = HttpRequest.newBuilder(
					new URI("http://" + camIP + "/info")).GET().build();
			client.send(request, BodyHandlers.ofString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		updater.stop();
	}

}

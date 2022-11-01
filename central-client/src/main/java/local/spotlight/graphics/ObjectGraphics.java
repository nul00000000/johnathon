package local.spotlight.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import local.spotlight.input.Input;

public class ObjectGraphics {
	
	public final Graphics2D g;
	private final int cX, cY, radius;
	
	private double tx;
	private double ty;
	private double txRamp;
	private double tyRamp;
	private double scale = 1;
	
	private double stroke = 2.0;
	
	private IEntity follow;
	
	private BufferedImage frame;
	private JFrame window;
	private JPanel panel;
	private Graphics windowGraphics;
	private Input input;
	
	public ObjectGraphics(String title, int width, int height, Input input) {
		this.input = input;
		window = new JFrame(title);
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(width, height));
		panel.setFocusable(true);
		panel.requestFocus();
		window.add(panel);
		window.pack();
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.setResizable(true);
		window.setLocationRelativeTo(null);
		window.setBackground(Color.BLACK);
		window.setVisible(true);
		
		windowGraphics = panel.getGraphics();
		
		this.input.attach(panel, this);
		
		this.frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		this.g = frame.createGraphics();
		this.cX = width / 2;
		this.cY = height / 2;
		this.radius = Math.max(height, width) / 2;
		g.setStroke(new BasicStroke((float) (stroke * scale)));
	}
	
	public void setFollow(IEntity follow) {
		this.follow = follow;
	}
	
	public double getMouseX() {
		return this.getWorldX(input.getMouseX());
	}
	
	public double getMouseY() {
		return this.getWorldY(input.getMouseY());
	}
	
	public double getWorldX(double appX) {
		return appX / scale + tx;
	}
	
	public double getWorldY(double appY) {
		return appY / scale + ty;
	}
	
	public double getWorldS(double appS) {
		return appS / radius / scale;
	}
	
	public double getAppX(double worldX) {
		return ((worldX - tx) * scale);
	}
	
	public double getAppY(double worldY) {
		return ((worldY - ty) * scale);
	}
	
	public double getAppS(double worldS) {
		return (worldS * scale);
	}
	
	public int igetAppX(double worldX) {
		return (int) (radius * (worldX - tx) * scale + cX);
	}
	
	public int igetAppY(double worldY) {
		return (int) (radius * (worldY - ty) * scale + cY);
	}
	
	public int igetAppS(double worldS) {
		return (int) (worldS * radius * scale);
	}
	
	public void setFont(String font, int style, int size) {
		g.setFont(new Font(font, style, (int) (size * scale)));
	}
	
	public void setColor(Color c) {
		g.setColor(c);
	}
	
	public void setColor(int c) {
		g.setColor(new Color(c));
	}
	
	public void setColor(double grey) {
		this.g.setColor(new Color((float)grey, (float)grey, (float)grey));
	}
	
	public void setColor(double r, double g, double b) {
		this.g.setColor(new Color((float)r, (float)g, (float)b));
	}
	
	public void setColor(float r, float g, float b) {
		this.g.setColor(new Color(r, g, b));
	}
	
	public void setStroke(double width) {
		this.stroke = width;
		g.setStroke(new BasicStroke((float) (stroke * scale * radius)));
	}
	
	public void fillRect(double cX, double cY, double radX, double radY) {
		g.fillRect(this.igetAppX(cX - radX), this.igetAppY(cY - radY), this.igetAppS(radX * 2), this.igetAppS(radY * 2));
	}
	
	public void drawImage(double cX, double cY, double radX, double radY, Image image) {
		g.drawImage(image, this.igetAppX(cX - radX), this.igetAppY(cY - radY), this.igetAppS(radX * 2), this.igetAppS(radY * 2), null);
	}
	
	public void fillBackground() {
		g.fillRect(cX - radius, cY - radius, cX + radius, cX + radius);
	}
	
	public void fillCircle(double cX, double cY, double radius) {
		g.fillOval(this.igetAppX(cX - radius), this.igetAppY(cY - radius), this.igetAppS(radius * 2), this.igetAppS(radius * 2));
	}
	
	public void drawRect(double cX, double cY, double radX, double radY) {
		g.drawRect(this.igetAppX(cX - radX), this.igetAppY(cY - radY), this.igetAppS(radX * 2), this.igetAppS(radY * 2));
	}
	
	public void drawCircle(double cX, double cY, double radius) {
		g.drawOval(this.igetAppX(cX - radius), this.igetAppY(cY - radius), this.igetAppS(radius * 2), this.igetAppS(radius * 2));
	}
	
	public void drawLine(double x1, double y1, double x2, double y2) {
		g.drawLine(this.igetAppX(x1), this.igetAppY(y1), this.igetAppX(x2), this.igetAppY(y2));
	}
	
	public void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3) {
		g.drawPolygon(new int[] {this.igetAppX(x1), this.igetAppX(x2), this.igetAppX(x3)}, 
				new int[] {this.igetAppY(y1), this.igetAppY(y2), this.igetAppY(y3)}, 3);
	}
	
	public void fillTriangle(double x1, double y1, double x2, double y2, double x3, double y3) {
		g.fillPolygon(new int[] {this.igetAppX(x1), this.igetAppX(x2), this.igetAppX(x3)}, 
				new int[] {this.igetAppY(y1), this.igetAppY(y2), this.igetAppY(y3)}, 3);
	}
	
	//stolen and adapted from https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
	public void drawString(double cX, double cY, String str) {
	    FontMetrics metrics = g.getFontMetrics();
	    int x = this.igetAppX(cX) - metrics.stringWidth(str) / 2;
	    int y = this.igetAppY(cY) - metrics.getHeight() / 2 + metrics.getAscent();
	    g.drawString(str, x, y);
	}
	
	public void updateZoom() {
		if(follow != null) {
			txRamp = follow.getX();
			tyRamp = follow.getY();
			tx += (txRamp - tx) * 0.1;
			ty += (tyRamp - ty) * 0.1;
		} else if(input.buttons.contains(Input.MIDDLE_CLICK)) {
			tx -= (double) (input.getMouseX() - input.getLastMouseX()) / scale;
			ty -= (double) (input.getMouseY() - input.getLastMouseY()) / scale;
		}
		float thisZoomF = 1.0f;
		if(input.mouseWheelState > 0) {
			thisZoomF = 1.0f / 1.1f;
		} else if (input.mouseWheelState < 0) {
			thisZoomF = 1.1f;
		}
		double worldX;
		double worldY;
		if(follow != null) {
			worldX = follow.getX();
			worldY = follow.getY();
		} else {
			worldX = this.getMouseX();
			worldY = this.getMouseY();
		}
		tx = (tx-worldX)/thisZoomF+worldX;
		ty = (ty-worldY)/thisZoomF+worldY;
		scale *= thisZoomF;
		if(thisZoomF != 1.0f) {
			g.setStroke(new BasicStroke((float) (stroke * scale)));
		}
	}
	
	public void drawToScreen() {
		windowGraphics.drawImage(frame, 0, 0, null);
	}
	
	public void close() {
		g.dispose();
		windowGraphics.dispose();
		window.dispose();
	}
	
	public boolean shouldClose() {
		return !window.isDisplayable();
	}

}

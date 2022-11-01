package local.spotlight.input;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import local.spotlight.graphics.ObjectGraphics;

public class Input implements KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {
	
	public ArrayList<Integer> down = new ArrayList<>();
	public ArrayList<Integer> jDown = new ArrayList<>();
	public ArrayList<Integer> buttons = new ArrayList<>();
	public ArrayList<Integer> jButtons = new ArrayList<>();
	public ArrayList<Integer> jUpButtons = new ArrayList<>();
	public double mouseX, mouseY, mouseDragX, mouseDragY;
	public double lastMouseX, lastMouseY, lastMouseDragX, lastMouseDragY;
	public int mouseWheelState;
	
	private int width;
	private int height;
	
	private ObjectGraphics g;
	
	public static final Integer LEFT_CLICK = MouseEvent.BUTTON1;
	public static final Integer MIDDLE_CLICK = MouseEvent.BUTTON2;
	public static final Integer RIGHT_CLICK = MouseEvent.BUTTON3;
	
	public boolean isKeyDown(int code) {
		return down.contains(code);
	}

	public boolean isKeyJDown(int code) {
		return jDown.contains(code);
	}

	public boolean isButtonDown(int code) {
		return buttons.contains(code);
	}

	public boolean isButtonJDown(int code) {
		return jButtons.contains(code);
	}

	public boolean isButtonJUp(int code) {
		return jUpButtons.contains(code);
	}
	
	public double getWorldMouseX() {
		return g.getWorldX(getMouseX());
	}
	
	public double getWorldMouseY() {
		return g.getWorldY(getMouseY());
	}

	public double getMouseX() {
		return mouseX / width * 2.0 - 1.0;
	}

	public double getMouseY() {
		return mouseY / height * 2.0 - 1.0;
	}

	public double getLastMouseX() {
		return lastMouseX / width * 2.0 - 1.0;
	}

	public double getLastMouseY() {
		return lastMouseY / height * 2.0 - 1.0;
	}

	public int getMouseWheelState() {
		return mouseWheelState;
	}

	public void update() {
		reset();
		setLastMouse();
	}
		
	public void attach(Component w, ObjectGraphics g) {
		w.addKeyListener(this);
		w.addMouseListener(this);
		w.addMouseMotionListener(this);
		w.addMouseWheelListener(this);
		this.width = w.getWidth();
		this.height = w.getHeight();
		this.g = g;
	}

	public void reset() {
		jDown.clear();
		jButtons.clear();
		jUpButtons.clear();
		mouseWheelState = 0;
	}
	
	public void setLastMouse() {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		lastMouseDragX = mouseDragX;
		lastMouseDragY = mouseDragY;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(!down.contains(e.getKeyCode())) {
			down.add(e.getKeyCode());
			jDown.add(e.getKeyCode());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		down.remove((Integer) e.getKeyCode());
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseDragX = mouseX = e.getX();
		mouseDragY = mouseY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(!buttons.contains(e.getButton())) {
			buttons.add(e.getButton());
			jButtons.add(e.getButton());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(buttons.contains(e.getButton())) {
			jUpButtons.add(e.getButton());
		}
		buttons.remove((Integer) e.getButton());
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		mouseWheelState = e.getWheelRotation();
	}

}

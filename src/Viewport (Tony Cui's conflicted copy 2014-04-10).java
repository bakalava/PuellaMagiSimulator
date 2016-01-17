import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class Viewport extends JPanel 
{
	public Spacetime grid = new Spacetime (50, 50);
	public Ant ant = new Ant (50, 50);
	public Wall wall = new Wall (100, 100, 2, 100, 0);

	public Point offset = new Point ();
	private boolean moving = true;
	private Point click = new Point ();
	private Point mouse = new Point ();
	private Point zoomCenter = new Point ();
	public int zoom = 1;

	public Viewport() 
	{
		setBackground (Color.black);
		grid.addEntity (ant);
		grid.addEntity (wall);
		addMouseListener (new MyMouseListener ());
		addMouseMotionListener (new MyMouseListener ());
		addMouseWheelListener (new MyMouseListener());

		startTimer ();
	}

	@Override
	public void paintComponent (Graphics g)
	{		
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;

		AffineTransform at = AffineTransform.getScaleInstance(zoom, zoom);		
		AffineTransform at2 = AffineTransform.getTranslateInstance(offset.x, offset.y);
		at.preConcatenate(at2);
		g2.transform(at);

		grid.draw(this, g2);		
	}	

	public void startTimer ()
	{
		Timer timer = new Timer ();
		timer.scheduleAtFixedRate (new TestTestTimerTask (), 0, 30);

	}

	private class TestTestTimerTask extends TimerTask
	{
		@Override
		public void run() 
		{
			grid.advance ();
			repaint();
		}		
	}

	/** Enables moving mode. Sets it so that clicking 
	 * and dragging will navigate around the grid. 
	 */
	public void startMoving ()
	{
		moving = true;
	}

	/** Called by the MouseListener.
	 * Updates the current offset by comparing 
	 * the current mouse location and the location of 
	 * the last checked mouse location. Does nothing if 
	 * currently not in moving mode. 
	 * 
	 * @param now	the current location of the mouse
	 */
	public void updateMove (Point now)
	{
		if (moving)
		{
			int dx = (now.x - click.x);
			int dy = (now.y - click.y);

			click = now;
			offset.translate(dx, dy);			
		}
	}

	/** Updates the zoom. Called by the MouseWheelListener.
	 * Adjusts the Viewport offset so that the point underneath
	 * the mouse remains centered.
	 * 
	 * @param wheelRotation 	the mouse wheel increment
	 */
	public void updateZoom (int wheelRotation)
	{		
		zoomCenter = new Point (mouse);
		
		// Increase or decrease zoom value, as necessary

		int newZoom = -wheelRotation + zoom;		
		if (newZoom < 1)
			newZoom = 1;
		
		// Adjust Viewport offset
		
		double scalar = newZoom * 1.0 / zoom;		
		int dx = (int) ((zoomCenter.x - offset.x) - scalar * (zoomCenter.x - offset.x));
		int dy = (int) ((zoomCenter.y - offset.y) - scalar * (zoomCenter.y - offset.y));
		offset.translate(dx, dy);
		
		// Set new zoom
		
		zoom = newZoom;
	}

	/** Disables moving mode. Sets it so that clicking
	 * won't navigate around the grid. 
	 * 
	 */
	public void stopMoving ()
	{
		moving = false;
	}

	public void inputKey (KeyEvent e)
	{
		if (e.getID() == KeyEvent.KEY_PRESSED)
		{		
			// User-controlled ant movements, for debugging purposes only
			
			char ch = Character.toLowerCase (e.getKeyChar());			

			if (ch == 'w')
				ant.accelerate(0, 2);
			else if (ch == 's')				
				ant.accelerate(Math.PI, 2);
			else if (ch == 'q')		
				ant.accelerate(-Math.PI / 2, 2);
			else if (ch == 'e')
				ant.accelerate(Math.PI / 2, 2);
			else if (ch == 'a')
				ant.accelerateSpin(-Math.PI / 36);
			else if (ch == 'd')
				ant.accelerateSpin(Math.PI / 36);
			//grid.advance();
			repaint();
		}
		else if (e.getID() == KeyEvent.KEY_RELEASED)
		{			
		}
		else if (e.getID() == KeyEvent.KEY_TYPED)
		{			
		}
	}

	/** Converts the specified point on the screen for this 
	 * Viewport to coordinates on the Spacetime grid.
	 * This method takes into account the Viewport's zoom and offset.
	 * 
	 * @param p		the screen coordinates to be converted
	 * @return the space-time grid coordinates
	 */
	public Point mouseToGridLocation (Point p)
	{
		Point result = new Point ();
		result.x = (p.x - offset.x) / zoom;
		result.y = (p.y - offset.y) / zoom;
		System.out.println (p);
		return result;
	}

	/** Converts the specified point on the space-time
	 * grid to coordinates on the Viewport screen. This method takes
	 * into account the Viewport's zoom and offset.
	 * 
	 * @param p
	 * @return
	 */
	public Point gridToMouseLocation (Point p)
	{
		Point result = new Point ();
		result.x = p.x * zoom + offset.x;
		result.y = p.x * zoom + offset.y;
		return result;
	}


	/** Class for MouseEvents. This class receives
	 * and acts upon various MouseEvents.
	 */
	private class MyMouseListener extends MouseAdapter
	{	
		public void mousePressed (MouseEvent e)
		{
			click = e.getPoint();	
		}

		public void mouseDragged (MouseEvent e)
		{	
			Point now = e.getPoint();

			if (moving) // if in moving mode
				updateMove (now);		

			repaint();
		}

		public void mouseMoved (MouseEvent e)
		{
			mouse = e.getPoint();
		}

		public void mouseWheelMoved (MouseWheelEvent e)
		{		
			updateZoom (e.getWheelRotation());
			repaint ();
		}
	}	

}

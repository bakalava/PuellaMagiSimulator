import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JPanel;

/** A JPanel that manages the navigation and drawing of a Spacetime object.
 * 
 * @author Sally Hui
 * @author Jiayin Huang
 * @author Tony Cui
 */
public class Viewport extends JPanel implements Serializable, MouseListener, MouseWheelListener, MouseMotionListener
{	
	private int type;

	private boolean isAddingEntities = false, 
			isDeletingEntities = false, 
			isNavigating = true, 
			isSelecting = false,
			isFirstPoint = true;

	private Rectangle2D.Double selection = new Rectangle2D.Double(0, 0, 0, 0);

	private Point point1, point2;

	private boolean isMovingEntities = false, findingEntities = false, dumped = true;

	protected ArrayList<Entity> cursorEntity = new ArrayList<Entity>();

	private static final long serialVersionUID = 1L;

	/** The minimum zoom value. Default is 1/&#8730;2.
	 */
	public static final double minZoom = Math.pow(2, -0.5);

	/** The maximum zoom value. Default is 16.
	 */
	public static final double maxZoom = Math.pow(2, 4);

	/** The Viewport will follow this specified Entity around, if it is not null. 
	 */
	private Entity focus;	

	/** The current location of the mouse, relative to the JPanel. In pixels. 
	 */
	protected Point mouse = new Point ();

	/** The location of the last mouse click, relative to the JPanel. In pixels. 
	 */
	protected Point click = new Point ();	

	/** The number of pixels to translate the Spacetime object, x-wise and y-wise. 
	 */
	public Point offset = new Point ();

	/** Current zoom value; lengths and widths are all scaled by
	 * this value. 
	 */
	public double zoom = 1;		

	/** The Spacetime object that this Viewport looks into. 
	 */
	public Spacetime grid;


	/** Creates a new Viewport looking into the specified Spacetime object.
	 * Automatically adds the MouseListeners necessary for navigation.
	 * 
	 * @param grid		the Spacetime to look into
	 */
	public Viewport (Spacetime grid)
	{
		this.grid = grid;
		setBackground (Color.black);
		addMouseListeners();				
	}	

	/** Adds a MouseListener, MouseMotionListener, and
	 * MouseWheelListener, all of which are necessary
	 * for moving and zooming.
	 */
	public void addMouseListeners ()
	{
		addMouseListener (this);
		addMouseMotionListener (this);
		addMouseWheelListener (this);
	}

	/** Manually advances the simulation. 
	 */
	public void advanceVP ()
	{
		if (focus != null)
		{
			if (grid.equals(focus.grid))
				centerAt(focus.getCenter());
			else
				focus = null;
		}
		grid.advance();
		repaint();
	}

	/**	Starts or stops the mode to move entities.
	 * 
	 * @param start		to start or stop moving entities
	 */
	public void setMoveEntity (boolean start)
	{
		if (start)
		{
			isMovingEntities = true;
			findingEntities = true;
		}
		else
		{
			isMovingEntities = false;
			findingEntities = false;
		}
	}

	/** Centers this Viewport at the specified Spacetime coordinates.
	 * 
	 * @param gridCoords	the Spacetime coordinates to center at
	 */
	public void centerAt (Point2D gridCoords)
	{
		offset.x = (int)(-gridCoords.getX() * zoom + getWidth() / 2);
		offset.y = (int)(-gridCoords.getY() * zoom + getHeight() / 2);
	}

	/** Converts Spacetime coordinates into JPanel coordinates.
	 * 
	 * @param gridCoords		the Spacetime coordinates to be converted
	 * @return the coordinates of the point on this Viewport that is directly
	 * 		above the specified grid coordinates
	 */
	public Point convertPointGridToMouse (Point gridCoords)
	{
		Point result = new Point ();
		result.x = (int)(gridCoords.x * zoom + offset.x);
		result.y = (int)(gridCoords.x * zoom + offset.y);
		return result;
	}

	/** Converts JPanel coordinates into Spacetime coordinates.
	 * 
	 * @param p		the mouse coordinates
	 * @return the Spacetime grid coordinates of the point that is directly
	 * 		underneath the specified Viewport coordinates
	 */
	public Point convertPointMouseToGrid (Point p)
	{
		Point result = new Point ();
		result.x = (int)((p.x - offset.x) / zoom);
		result.y = (int)((p.y - offset.y) / zoom);		
		return result;
	}	

	/** Clears the list of selected Entities. 
	 */
	public void deselect ()
	{
		cursorEntity.clear();
	}

	/** Sets the Viewport to follow around the specified
	 * Entity until it is removed from the Spacetime
	 * that this Viewport is monitoring.
	 * 
	 * @param e		the Entity to follow
	 */
	public void followEntity (Entity e)
	{
		if (e != null)
			if (grid.equals(e.grid))
				focus = e;
	}

	public void moveEntities ()
	{
		if (findingEntities && dumped)
		{				
			clickSelection();

			if (!cursorEntity.isEmpty())
			{					
				findingEntities = false;
				for (int i = 0; i < cursorEntity.size(); i++)
					grid.requestRemoval(cursorEntity.get(i));

				dumped = false;
				repaint();
			}
		}
		else
		{
			dumpSelection();
		}
	}

	public void clickSelection ()
	{
		Rectangle cursorBox = new Rectangle (5,5);
		cursorBox.setLocation (convertPointMouseToGrid(click));
		cursorEntity = grid.getEntities(cursorBox);
	}

	public void dumpSelection ()
	{
		findingEntities = true;
		dumped = true;			
		for (int i = 0; i < cursorEntity.size(); i++)
		{
			cursorEntity.get(i).setLocation(convertPointMouseToGrid(click).x, convertPointMouseToGrid(click).y);
			grid.addEntity (cursorEntity.get(i));
			grid.ensureNoCollision (cursorEntity.get(i));
		}			
		repaint();
	}
	
	public void removeSelection ()
	{
		for (Entity e : cursorEntity)
			grid.requestRemoval(e);
	}

	/** Draws the current view of the Spacetime grid. 
	 */
	@Override
	public void paintComponent (Graphics g)
	{		
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;		
		grid.draw(this, g2);	

		if (isSelecting)
		{
			g2.setColor (new Color (0, 255, 0, 100));
			g2.fill (selection);
			g2.setColor (new Color (0, 192, 0));
			g2.draw (selection);
		}
	}	

	public void removeMouseListeners()
	{
		removeMouseListener (this);
		removeMouseMotionListener (this);
		removeMouseWheelListener (this);
	}

	/** Sets the zoom to the specified value. Centers the
	 * zooming action at the specified point, which is in
	 * JPanel coordinates.
	 * 
	 * @param newZoom		the new zoom value
	 * @param zoomCenter	the anchor point of this zoom
	 */
	public void setZoom (double newZoom, Point zoomCenter)
	{	
		// Adjust Viewport offset

		double scalar = newZoom * 1.0 / zoom;		
		int dx = (int) ((zoomCenter.x - zoomCenter.x) - scalar * (zoomCenter.x - offset.x));
		int dy = (int) ((zoomCenter.y - zoomCenter.y) - scalar * (zoomCenter.y - offset.y));
		offset.translate(dx, dy);

		// Set new zoom

		zoom = newZoom;
	}			


	/** Called by the MouseListener. Shifts the Viewport offset by
	 * the amount that the mouse moved since this method was last called.
	 * This method uses the <code>click</code> variable to store the
	 * previous location of the mouse.
	 * 
	 * @param now	the current location of the mouse
	 */
	private void updateMove (Point now)
	{
		if (isNavigating)
		{
			int dx = (now.x - click.x);
			int dy = (now.y - click.y);

			click = now;
			offset.translate(dx, dy);			
		}
	}

	/** Called by the MouseWheelListener. Updates the zoom. 
	 * Adjusts the Viewport offset so that the point underneath
	 * the mouse remains centered.
	 * 
	 * @param wheelRotation 	the mouse wheel increment
	 */
	private void updateZoom (int wheelRotation)
	{	
		// Increase or decrease zoom value, as necessary

		double newZoom = Math.pow(2, -wheelRotation * 0.5) * zoom;	

		if (newZoom >= minZoom && newZoom <= maxZoom)
		{
			// Adjust Viewport offset

			double scalar = newZoom * 1.0 / zoom;		
			int dx = (int) ((mouse.x - offset.x) - scalar * (mouse.x - offset.x));
			int dy = (int) ((mouse.y - offset.y) - scalar * (mouse.y - offset.y));
			offset.translate(dx, dy);

			// Set new zoom

			zoom = newZoom;
		}		
	}

	@Override
	public void mouseClicked(MouseEvent e) 
	{
	}

	/** Calls the <code>updateMove</code> method to 
	 * effect click and drag navigation. 
	 */
	@Override
	public void mouseDragged(MouseEvent e) 
	{
		Point now = e.getPoint();

		if (isSelecting)
		{
			point2 = convertPointMouseToGrid(now);
			selection.setFrameFromDiagonal(point1, point2);
		}
		else if (isNavigating) // if in moving mode
			updateMove (now);		

		else if (isDeletingEntities)
		{
			if (isFirstPoint)
			{
				point1 = now;
			}
		}		

		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) 
	{
	}

	@Override
	public void mouseExited(MouseEvent e) 
	{
	}

	/** Updates the <code>mouse</code> variable with
	 * the current mouse location. 
	 */
	@Override
	public void mouseMoved(MouseEvent e) 
	{
		mouse = e.getPoint();
	}


	/** Updates the <code>click</code> variable with
	 * the location of the click. 
	 */
	@Override
	public void mousePressed(MouseEvent e) 
	{		
		click = e.getPoint();
		if (isSelecting)
		{
			point1 = convertPointMouseToGrid (click);
			selection.setFrameFromDiagonal(point1, point1);
		}
		else if (isMovingEntities)
		{
			moveEntities();
		}		
		else if (isAddingEntities)
		{
			addNewEntity();
		}	
		else if (isDeletingEntities)
		{
			clickSelection();
			for (Entity entity : cursorEntity)
				grid.requestRemoval(entity);
			cursorEntity.clear();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		click = e.getPoint();

		if (isSelecting) // finalize selection
		{
			point2 = convertPointMouseToGrid (click);
			selection.setFrameFromDiagonal(point1, point2);
			cursorEntity.clear();
			cursorEntity = grid.getEntities(selection);			
			selection.setRect(0, 0, 0, 0);
			if (!cursorEntity.isEmpty())
			{
				if (isMovingEntities)
				{
					removeSelection();
					dumped = false;
				}
				else if (isDeletingEntities)
				{
					removeSelection();
					cursorEntity.clear();
				}
			}
		}
	}	

	/** Calls the <code>updateZoom</code> method to
	 * effect zooming. 
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) 
	{
		updateZoom (e.getWheelRotation());
		repaint ();
	}

	public void addNewEntity ()
	{
		Entity add;

		if (type == 0)
			add = new Human (convertPointMouseToGrid(mouse).x, convertPointMouseToGrid(mouse).y);
		else if (type == 1)
			add = new Incubator (grid, convertPointMouseToGrid(mouse).x, convertPointMouseToGrid(mouse).y);
		else if (type == 2)
			add = new Puella (convertPointMouseToGrid(mouse).x, convertPointMouseToGrid(mouse).y);
		else
			add = new Witch (convertPointMouseToGrid(mouse).x, convertPointMouseToGrid(mouse).y);

		grid.addEntity (add);
		grid.ensureNoCollision(add);
	}

	public void setAddEntityType (int entityType)
	{
		type = entityType;
	}	

	public void setIsAddingEntities (boolean mode)
	{
		isAddingEntities = mode;
	}	

	public void setIsDeletingEntities (boolean mode)
	{
		isDeletingEntities = mode;
	}

	public void setIsSelectingEntities (boolean mode)
	{
		isSelecting = mode;
	}

	public void setIsNavigating (boolean mode)
	{
		isNavigating = mode;
	}
}

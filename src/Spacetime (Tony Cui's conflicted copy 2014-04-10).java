import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/** A class containing everything in the simulated world.
 * Essentially, the "grid".
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class Spacetime 
{
	/**
	 * The underneath layer.
	 */
	public static final int TERRAIN = 0;
	/**
	 * The main layer. 
	 */
	public static final int ENTITIES = 1;
	/**
	 * The above layer.
	 */
	public static final int FOLIAGE = 2;	
	
	private long tick = 0;
	private Dimension size;
	
	/**
	 * The Viewport is offset by a random value from 0 to this value
	 * every time the graphics are drawn, thus resulting in a cool shaking
	 * effect. Default value is 0.
	 */
	public int shake = 0;

	// TODO: optimize finding an entity based on coordinates (maybe use chunks?)
	private List<List<Entity>> layers;		

	/** Creates a new space-time plane of the specified
	 * dimensions. These dimensions are in terms of
	 * GrassTiles.
	 * 
	 * @param width		the width of the plane
	 * @param height	the height of the plane
	 */
	public Spacetime (int width, int height) 
	{							
		// Initialize layers

		layers = new ArrayList<List<Entity>> (3);
		for (int i = 0; i < 3; i++)
			layers.add(new ArrayList<Entity> (4));

		// Set size

		size = new Dimension (width, height);

		// Generate World

		WorldGenerator wg = new WorldGenerator ();
		wg.generate (this);		
	}

	/** Adds the specified Entity to the ENTITIES layer
	 * of this space-time plane.
	 * 
	 * @param e		the Entity to be added
	 * @return true if the entity was added; false otherwise
	 */
	public boolean addEntity (Entity e)
	{		
		return addEntity (e, ENTITIES);
	}

	/** Adds the specified Entity to the specified layer of this
	 * space-time plane. Does nothing if the specified Entity
	 * is already present in the specified layer.
	 * 
	 * @param e			the Entity to be added
	 * @param layer		the layer to which to add the entity
	 * @return true if the Entity was added; false otherwise
	 * @throws IndexOutOfBoundsException if the layer is out of bounds 
	 * 		(layer < 0 || layer >= size())
	 */
	public boolean addEntity (Entity e, int layer)
	{
		boolean added = false;

		List<Entity> list = layers.get(layer);

		if (list.indexOf(e) == -1) // if the entity is not already in this list
		{
			list.add (e);
			e.grid = this;
			added = true;
		}				

		return added;			
	}
	

	/** Removes the specified Entity from the ENTITIES layer
	 * of this space-time plane.
	 * 
	 * @param e		the Entity to be removed
	 * @return true if the layer contained the specified Entity
	 */
	public boolean removeEntity (Entity e)
	{		
		return removeEntity (e, ENTITIES);
	}

	/** Removes the specified Entity from the specified layer
	 * of this space-time plane. 
	 * 
	 * @param e 		the Entity to be removed
	 * @param layer		the layer from which to remove the Entity
	 * @return true if this layer contained the specified Entity
	 * @throws IndexOutOfBoundsException if the layer is out of bounds 
	 * 		(layer < 0 || layer >= size())
	 */
	public boolean removeEntity (Entity e, int layer)
	{		
		List<Entity> list = layers.get(layer);

		boolean removed = list.remove(e);
		if (removed)
			if (e.grid.equals (this))
				e.grid = null;			

		return removed;		
	}	

	/** Advances the simulation by one tick. Iterates through all of
	 * the Entities in the ENTITIES layer and calls the advance()
	 *  method of each.
	 */
	public void advance ()
	{		
		Entity [] allEntities = layers.get(ENTITIES).toArray (new Entity [0]);
		for (Entity e : allEntities)
			e.advance();
		tick++;
	}
	
	/** Attempts to rotate the specified Entity by the indicated
	 * amount. Proceeds with the rotation only if the Entity does not
	 * collide with any other Entities in its new orientation.
	 * 
	 * @param e				the Entity to rotate
	 * @param deltaTheta	the amount by which to rotate, in radians
	 * @return true of the Entity was successfully rotated; false otherwise
	 */
	public boolean rotateEntity (Entity e, double deltaTheta)
	{
		boolean rotated = false;
		e.theta += deltaTheta;
		if (isColliding(e, e.x, e.y))
		{
			e.theta -= deltaTheta;
			rotated = false;
			e.angularVelocity = 0;
		}
		return rotated;
	}

	/** Attempts to translate the specified Entity by the specified amount.
	 * Proceeds with the translation only if the Entity does not collide with
	 * any other Entities at its new location.
	 * 	
	 * @param e		the Entity to translate
	 * @param dx	the distance to move this Entity along the X axis
	 * @param dy	the distance to move this Entity along the Y axis
	 * @return true if the Entity was successfully moved; false otherwise
	 */
	public boolean translateEntity (Entity e, double dx, double dy)
	{
		boolean moved = true;		
		if (!isColliding (e, e.x + dx, e.y + dy))
			e.setLocation(e.x + dx, e.y + dy);
		else
			moved = false;
		return moved;		
	}
	
	/** Advances the location and angle of the specified Entity based on
	 * its velocity and rotational velocity. If the Entity collides with 
	 * any number of other Entities, the notifyCollision() method of each 
	 * of the colliding Entities are called.
	 * 
	 * @param e		the Entity to move
	 * @return true if the Entity was successfully moved; false otherwise
	 */
	public boolean moveEntity (Entity e)
	{
		boolean moved = true;

		e.vectorAdvance();		
		
		List<Entity> colliding = getCollidingEntities (e);
		if (colliding.size() != 0)
		{
			for (int i = 0; i < colliding.size(); i++)
				colliding.get(i).notifyCollision(e);						
			moved = false;
		}
				
		return moved;
	}	
	
	/** Draws everything on this space-time plane, in order from the
	 * lowest layer (TERRAIN) to the highest layer (FOLIAGE). Draws 
	 * an Entity only if its shape intersects the rectangle defined
	 * by the Viewport's current field of view. (Determined by using 
	 * the offset value, zoom, and screen dimensions.) This is to
	 * prevent the unnecessary drawing of Entities that wouldn't be 
	 * visible anyway.
	 * 
	 * @param viewport	the Viewport that is calling this method
	 * @param g			the Graphics context in which to paint	
	 */
	public void draw (Viewport viewport, Graphics2D g)
	{
		int zoom = viewport.zoom;
		Rectangle panel = new Rectangle (viewport.getWidth() / zoom, viewport.getHeight() / zoom);
		panel.setLocation (viewport.offset);
		panel.x /= -zoom;
		panel.y /= -zoom;	
		if (shake != 0)
		{
			double randomX = shake * Math.random();
			double randomY = shake * Math.random();			
			g.translate(randomX, randomY);
			panel.x -= (int)randomX;
			panel.y -= (int)randomY;
		}

		for (List<Entity> list : layers)
			for (Entity e : list)
				if (isCollidingCheap (e.getShape(), panel))
					e.draw(g);
	}
	
	/** Retrieves a list of all of the Entities that the specified Entity 
	 * is currently intersecting with. This is for the ENTITIES layer. 
	 * Returns an empty list if the Entity is not intersecting with anything.
	 * 
	 * @param e			the Entity to be checked
	 * @return a List containing all colliding Entities
	 */
	public List<Entity> getCollidingEntities (Entity e)
	{
		List<Entity> plane = layers.get (ENTITIES);
		List<Entity> colliding = new ArrayList<Entity> ();
		Shape thisShape = e.getShape();
		
		for (int i = 0; i < plane.size(); i++)
		{
			Entity other = plane.get(i);
			if (!other.equals(e))
				if (isColliding (thisShape, other.getShape()))
					colliding.add(other);				
		}
		
		return colliding;
	}
	
	/** Retrieves the first Entity at the specified coordinates,
	 * on the ENTITIES layer.
	 * Returns null if nothing is there.
	 * 
	 * @param x			the x coordinate to check
	 * @param y			the y coordinate to check	 
	 * @return the Entity at the location if it exists; null otherwise	 
	 */
	public Entity getEntity (double x, double y)
	{
		List<Entity> entities = layers.get(ENTITIES);
		Entity entity = null;
		Point2D.Double snooper = new Point2D.Double (x, y);

		int index = 0;		
		while (index < entities.size () && entity == null)
		{
			Entity temp = entities.get(index);
			if (temp.getShape().contains(snooper))
				entity = temp;
			index++;
		}
		return entity;
	}

	/** Retrieves a list of all of the Entities that intersect 
	 *  the given selection Shape, on the ENTITIES layer. 
	 * 
	 * @param selection		the Shape specifying the selection area	 
	 * @return the list of all selected Entities	
	 */
	public List<Entity> getEntities (Shape selection)
	{
		List<Entity> entities = layers.get(ENTITIES);
		List<Entity> results = new ArrayList<Entity> ();
		for (Entity e : entities)
			if (isColliding (e.getShape(), selection))
				results.add (e);

		return results;
	}

	/** Returns the width and height of this space-time plane. 
	 * This is defined as: number of grass tiles widthwise x number of grass tiles heightwise
	 * 
	 * @return the width and height as a Dimension object
	 */
	public Dimension getSize ()
	{
		return new Dimension (size);
	}
	
	/** Retrieves the time value of this space-time plane.
	 * 
	 * @return the number of ticks that have elapsed since this Spacetime started
	 */
	public long getTime ()
	{
		return tick;
	}
		
	/** Checks if the specified Entity would collide with any 
	 * other Entities if the specified Entity were at the 
	 * specified coordinates.
	 * 
	 * @param e		the Entity being checked
	 * @param x		the x-coordinate to check
	 * @param y		the y-coordinate to check
	 * @return true if there is a collision; false otherwise
	 */
	public boolean isColliding (Entity e, double x, double y)
	{		
		return isColliding (e, x, y, ENTITIES);
	}


	/** Checks if the specified Entity will collide with any
	 * other Entities if the specified Entity were at the indicated
	 * coordinates in the indicated layer of this space-time plane.
	 * 
	 * @param e			the Entity being checked
	 * @param x			the x-coordinate to check
	 * @param y			the y-coordinate to check
	 * @param layer		the layer at which to check
	 * @return true if there is a collision; false otherwise
	 * @throws IndexOutOfBoundsException if the layer is out of bounds 
	 * 		(layer < 0 || layer >= size())
	 */
	public boolean isColliding (Entity e, double x, double y, int layer)
	{
		List<Entity> list = layers.get (layer);

		double tempX = e.x;
		double tempY = e.y;
		int index = 0;		
		boolean willCollide = false;

		e.setLocation (x, y); // Temporally shifting entity to location for check

		while (index < list.size() && !willCollide)
		{			
			if (!list.get(index).equals(e)) // Do not check if it will collide with itself
				willCollide = isColliding (e.getShape(), list.get(index).getShape());				

			index++;			
		}		

		e.setLocation (tempX, tempY); // Reverting temporary location shift

		return willCollide;
	}	
	
	/** Checks if the two specified Shapes are intersecting.
	 * 
	 * @param a		the first shape to be tested
	 * @param b		the other shape to be tested
	 * @return true if there is an intersection; false otherwise
	 */
	public boolean isColliding (Shape a, Shape b)
	{
		boolean isColliding = false;				

		// Cheap check with rectangle bounds
		isColliding = isCollidingCheap (a, b);
		if (isColliding)
		{
			isColliding = isCollidingLessCheap (a, b);

			if (isColliding) // More in-depth check with areas
			{			
				Area areaA = new Area (a);			
				areaA.intersect(new Area (b));
				isColliding = !areaA.isEmpty();
			}
		}

		return isColliding;
	}

	/** Does a somewhat cheap inaccurate test to see if the two specified Shapes are
	 * intersecting by checking if the bounding rectangle of the smaller shape 
	 * is intersecting with the larger shape.
	 * 
	 * @param a		the first shape to be tested
	 * @param b		the other shape to be tested
	 * @return true if there is an intersection; false otherwise
	 */
	public boolean isCollidingLessCheap (Shape a, Shape b)
	{
		boolean isColliding;

		Rectangle aBound = a.getBounds();
		Rectangle bBound = b.getBounds();
		
		Area smaller;
		Area larger;

		int sizeA = aBound.width * aBound.height;
		int sizeB = bBound.width * bBound.height;

		if (sizeA < sizeB)
		{
			smaller = new Area (aBound);
			larger = new Area (b);
		}
		else
		{
			smaller = new Area (bBound);
			larger = new Area (a);
		}

		smaller.intersect(larger);
		isColliding = !smaller.isEmpty();				

		return isColliding;
	}

	/** Does a cheap inaccurate check to see if the two specified Shapes
	 * are intersecting by checking if the rectangle bounds of the two
	 * specified Shapes are intersecting.
	 * 
	 * @param a		the first shape to be tested
	 * @param b		the other shape to be tested
	 * @return true if there is an intersection ; false otherwise
	 */
	public boolean isCollidingCheap (Shape a, Shape b)
	{
		return a.getBounds().intersects(b.getBounds());
	}
}

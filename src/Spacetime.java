import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** A class containing everything in the simulated world.
 * Essentially, the "grid". 
 * <p>
 * This class stores all of the Entities in the simulated world
 * in three separate layers:
 * <ul>
 * <li> the TERRAIN layer, for tiles
 * <li> the ENTITIES layer, for the entities
 * <li> the FOLIAGE layer, for decorative shapes above the rest  
 * </ul><br> 
 * Collisions are only checked on the ENTITIES layer. 
 * <p>
 * All of the Entities in each layer are stored in a separate QuadTree,
 * which recursively divides itself as more Entities are added to minimize
 * the number of collision checks necessary. 
 *  
 * @see QuadTree
 * @see Entity
 * 
 * @author Jiayin Huang
 * @author Sally Hui
 * @author Tony Cui 
 */
public class Spacetime implements Serializable
{	
	private static final long serialVersionUID = 1L;	

	/** The underneath layer.
	 */
	public static final int TERRAIN = 0;

	/** The main layer. 
	 */
	public static final int ENTITIES = 1;

	/** The above layer.
	 */
	public static final int FOLIAGE = 2;

	/** The color of the explosion used in the <code>markAddition</code> method. 
	 */
	public static Color addColor = new Color (177, 244, 252);

	/** The color of the explosion used in the <code>markRemoval</code> method. 
	 */
	public static Color removeColor = new Color (255, 43, 43);

	/** The time stamp of this simulation
	 */
	private long tick = 0;

	/** The size of this Spacetime grid, in units of TerrainTile widths. 
	 */
	private Dimension size = new Dimension ();	

	/** The Graphics object in {@code draw(Graphics2D)} is offset by a random value ranging 
	 * from 0 to this value every time this Spacetime is drawn, thus resulting in 
	 * a cool shaking effect. Default value is 0.
	 */
	public double shake = 0;

	/** The decrease that the <code>shake</code> variable undergoes every tick. This only
	 * applies when <code>shake</code> is greater than 0.
	 */
	public double shakeDecrement = 0;

	/** The number of ticks it takes for a special effect to fade away. 
	 */
	public int fadeTicks = 60;

	/** The layers of this Spacetime. 
	 */
	private List<QuadTree> layers;	

	/** A list of Entities that are to be removed at the beginning of the next tick. 
	 */
	private List<ArrayList<Entity>> removalQueue;

	/** The incubator responsible for this Spacetime. 
	 */
	protected Incubator incubator;

	/** Creates a new Spacetime plane of the specified
	 * dimensions, which are in units of the specified
	 * chunk size. 
	 * 
	 * @param width			the width of the plane
	 * @param height		the height of the plane
	 * @param chunkSize		the desired width of one chunk
	 */
	public Spacetime (int width, int height, int chunkSize) 
	{							
		// Initialize layers

		double maxX = width * chunkSize;
		double maxY = height * chunkSize;		

		layers = new ArrayList<QuadTree> (3);
		removalQueue = new ArrayList<ArrayList<Entity>>();
		for (int i = 0; i < 3; i++)
		{
			layers.add(new QuadTree (0, 0, maxX, maxY));
			removalQueue.add(new ArrayList<Entity>());
		}

		// Set size

		size = new Dimension (width, height);	
	}

	/** Creates a new Spacetime plane that is populated by
	 * the specified WorldGenerator object.
	 *
	 * @param wg		the WorldGenerator to be used to populate this plane
	 */
	public Spacetime (WorldGenerator wg)
	{
		this (wg.getWidth(), wg.getHeight(), wg.getChunk());

		// Generate World
		if (wg != null)		
			wg.generate (this);		
	}	


	/** Adds the specified Entity to the ENTITIES layer
	 * of this space-time plane.
	 * 
	 * @param e		the Entity to be added
	 */
	public void addEntity (Entity e)
	{		
		addEntity (e, ENTITIES);
	}

	/** Adds the specified Entity to the specified layer of this
	 * space-time plane.
	 * 
	 * @param e			the Entity to be added
	 * @param layer		the layer to which to add the entity
	 * @throws IndexOutOfBoundsException if the layer is out of bounds 
	 * 		(layer < 0 || layer >= size())
	 */
	public void addEntity (Entity e, int layer)
	{	
		QuadTree plane = layers.get(layer);
		plane.add(e);
		e.grid = this;
	}		

	/** Advances the simulation by one tick. Iterates through all of
	 * the Entities in the ENTITIES layer and FOLIAGE layer and calls 
	 * the advance() method of each.
	 */
	public void advance ()
	{	
		// Handle incubator respawning, if necessary

		if (incubator != null)	
			if (incubator.grid == null)		
				incubator.respawn(this);					

		// Remove any Entities currently in the removal queue

		for (int layer = 0; layer < removalQueue.size(); layer++)
		{
			List<Entity> list = removalQueue.get(layer);

			for (int i = 0; i < list.size(); i++)
				removeEntity (list.get(i), layer);

			list.clear();
		}

		// Advance all Entities in ENTITIES and FOLIAGE layers

		List<Entity> entities = layers.get(ENTITIES).get();
		for (Entity e : entities)
			e.advance();

		List<Entity> effects = layers.get(FOLIAGE).get();
		for (Entity e : effects)
			e.advance();

		tick++;			
	}	


	/** Draws everything on this space-time plane in order from the
	 * lowest layer (TERRAIN) to the highest layer (FOLIAGE). Draws 
	 * an Entity only if the bounding rectangle of its shape intersects 
	 * the rectangle defined by the Viewport's current field of view.
	 * <p>
	 * This method uses the Viewport's offset, zoom, and screen dimensions
	 * for calculations.
	 * 
	 * @param viewport	the Viewport that is calling this method
	 * @param g			the Graphics context in which to paint	
	 */
	public void draw (Viewport viewport, Graphics2D g)
	{
		Point offset = viewport.offset;
		double zoom = viewport.zoom;		

		AffineTransform at = AffineTransform.getScaleInstance(zoom, zoom);		
		AffineTransform at2 = AffineTransform.getTranslateInstance(offset.x, offset.y);
		at.preConcatenate(at2);
		g.transform(at);

		Rectangle window = new Rectangle ((int)(viewport.getWidth() / zoom), (int)(viewport.getHeight() / zoom));
		window.setLocation (viewport.offset);		
		window.x /= -zoom;
		window.y /= -zoom;	

		if (shake != 0)
			shake (g, window);		

		for (QuadTree layer : layers)
		{
			List<Entity> list = layer.get(window);			
			for (Entity e : list)
				if (isCollidingCheap (e.getShape(), window))
					e.draw(g);
		}			
	}

	/** Offsets the given Graphics2D and Rectangle by a random amount,
	 * the maximum of which being defined the the shake field variable.
	 * 
	 * @param g			the Graphics2D object being used for drawing
	 * @param window	the Rectangle representing the Viewport's current field of view
	 */
	private void shake (Graphics2D g, Rectangle window)
	{	
		double randomX = shake * Math.random();
		double randomY = shake * Math.random();			
		g.translate(randomX, randomY);
		window.x -= (int)randomX;
		window.y -= (int)randomY;

		shake -= shakeDecrement;

		if (shake < 0)
			shake = 0;
	}

	/** Starts a shaking special effect. The shaking will fade
	 * away after <code>fadeTicks</code> ticks have elapsed.
	 * 
	 * @param shakeMagnitude	the amount by which to shake the screen
	 */
	public void startShake (double shakeMagnitude)
	{
		shake = Math.max(shake, shakeMagnitude);
		shakeDecrement = shake / fadeTicks;
	}

	/** Nudges the specified Entity so that is not colliding with anything 
	 * at its current location in the ENTITIES layer. If the Entity is 
	 * colliding with something, its coordinates are iteratively moved 
	 * outwards in a spiral pattern until it is not colliding with anything anymore.
	 * <p>
	 * In the current implementation, in each successive iteration:
	 * <ul>
	 * <li> the angle to the initial point increases by <code>2&#960/30</code> radians
	 * <li> the radial distance from the initial point increases by <code>10</code> units
	 * </ul>
	 * 
	 * @param e		the specified Entity
	 */
	public void ensureNoCollision (Entity e)
	{	
		final int angleStep = 30;
		final int radiusStep = 4;

		Rectangle2D bounds = layers.get(ENTITIES).getBounds();
		Shape shape = e.getShape();		

		double x0 = e.x;
		double y0 = e.y;

		double dx = 0;
		double dy = 0;

		int counter = 1;

		boolean colliding = isColliding (e);
		boolean isInBounds = isColliding(bounds, shape);

		while (colliding || !isInBounds)
		{
			double scalar = (double)counter / angleStep; 
			dx = radiusStep * counter * Math.cos(scalar * 2 * Math.PI);
			dy = radiusStep * counter * Math.sin(scalar * 2 * Math.PI);
			e.setLocation(x0 + dx, y0 + dy);
			colliding = isColliding (e);
			isInBounds = isColliding (bounds, e.getShape());
			counter++;	
		}			
	}

	/** Retrieves a list of all of the Entities with which the specified Entity 
	 * is currently colliding. This is for the ENTITIES layer. 
	 * Returns an empty list if the Entity is not intersecting with anything.
	 * 
	 * @param e			the Entity to be checked
	 * @return a List containing all colliding Entities
	 */
	public ArrayList<Entity> getCollidingEntities (Entity e)
	{
		ArrayList<Entity> plane = layers.get(ENTITIES).get(e);		
		ArrayList<Entity> colliding = new ArrayList<Entity>();
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


	/** Returns a list of all of the Entities that intersect 
	 *  the given selection Shape on the ENTITIES layer. 
	 * 
	 * @param selection		the Shape specifying the selection area	 
	 * @return the list of all selected Entities	
	 */
	public ArrayList<Entity> getEntities (Shape selection)
	{
		ArrayList<Entity> entities = layers.get(ENTITIES).get(selection.getBounds());
		ArrayList<Entity> results = new ArrayList<Entity>();
		for (Entity e : entities)
			if (isColliding (e.getShape(), selection))
				results.add (e);	

		return results;
	}	

	/** Returns all of the Entities in this Spacetime that match 
	 * the specified entityType. Use ClassName.class as the argument
	 * for this method. For example: getEntities(Human.class)
	 * 
	 * @param entityType	the desired Entity type
	 * @return a list containing all of the Entities matching the specified type
	 */
	public ArrayList<Entity> getEntities (Class<?> entityType)
	{
		ArrayList<Entity> all = layers.get(ENTITIES).get();
		ArrayList<Entity> list = new ArrayList<Entity>();

		for (Entity e : all)		
			if (entityType.isInstance(e))
				list.add(e);		

		return list;
	}

	/** Returns a list of all of the Entities that fall within 
	 * given radius of the specified Entity.
	 * <p>
	 * The list excludes the Entity itself.
	 * 
	 * @param e			the Entity around which to search
	 * @param radius	the radius of the search
	 * @return an ArrayList containing all of the Entities within the specified radius
	 */
	public ArrayList<Entity> getEntities (Entity e, double radius)
	{
		Rectangle2D bounds = new Rectangle2D.Double (e.x - radius, e.y - radius, 2 * radius, 2 * radius);
		ArrayList<Entity> entities = layers.get(ENTITIES).get(bounds);
		ArrayList<Entity> result = new ArrayList<Entity> (entities.size());

		double radiusSquared = radius * radius;

		for (Entity temp : entities) // Use distance formula to check Entity distance 					
			if (((e.x - temp.x)*(e.x - temp.x)) + ((e.y - temp.y)*(e.y - temp.y)) <= radiusSquared)
				result.add(temp);		

		result.remove(e); // Do not include the Entity itself

		return result;
	}



	/** Retrieves the first Entity at the specified coordinates,
	 * on the ENTITIES layer. Returns null if nothing is there.
	 * 
	 * @param x			the x coordinate to check
	 * @param y			the y coordinate to check	 
	 * @return the Entity at the location if it exists; null otherwise	 
	 */
	public Entity getEntity (double x, double y)
	{
		List<Entity> entities = layers.get(ENTITIES).get(x, y);
		Point2D.Double snooper = new Point2D.Double (x, y);
		Entity entity = null;		

		int index = 0;		
		while (index < entities.size() && entity == null)
		{
			Entity temp = entities.get(index);
			if (temp.getShape().contains(snooper))
				entity = temp;
			index++;
		}
		return entity;
	}


	/** Returns the width and height of this space-time plane as TerrainTile width units. 
	 * i.e. number of tiles widthwise x number of tiles heightwise
	 * 
	 * @return the width and height of this Spacetime in units of TerrainTiles
	 */
	public Dimension getSize ()
	{
		return new Dimension (size);
	}		

	/** Returns the time stamp of this space-time plane.
	 * 
	 * @return the number of ticks that have elapsed since this Spacetime first started
	 */
	public long getTime ()
	{
		return tick;
	}

	/** Checks if the specified Entity is colliding with anything
	 * at its current location.
	 * 
	 * @param e		the Entity being checked
	 * @return true if there is a collision; false otherwise
	 */
	public boolean isColliding (Entity e)
	{
		return isColliding (e, ENTITIES);
	}

	/** Checks if the specified Entity would collide with any 
	 * other Entities if the specified Entity were set at the 
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

	/** Checks if the specified Entity would collide with any
	 * other Entities if the specified Entity were set at the specified
	 * coordinates in the specified layer.
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
		boolean willCollide = false;
		double tempX = e.x;
		double tempY = e.y;	

		e.setLocation (x, y); // Temporally shifting entity to location for check		
		Shape shape = e.getShape();		
		List<Entity> list = layers.get(layer).get(shape.getBounds());	
		e.setLocation (tempX, tempY); // Reverting temporary location shift

		int index = 0;
		while (index < list.size() && !willCollide)
		{			
			if (!list.get(index).equals(e)) // Do not check if it will collide with itself
				willCollide = isColliding (shape, list.get(index).getShape());			
			index++;			
		}			

		return willCollide;
	}	

	/** Checks if the specified Entity is colliding with anything
	 * at its current location in the specified layer.
	 * 
	 * @param e			the Entity being checked
	 * @param layer		the layer at which to check
	 * @return true if there is a collision; false otherwise
	 * @throws IndexOutOfBoundsException if the layer is out of bounds 
	 * 		(layer < 0 || layer >= size())
	 */
	public boolean isColliding (Entity e, int layer)
	{
		Shape shape = e.getShape();
		List<Entity> list = layers.get(layer).get(e);			
		boolean willCollide = false;
		int index = 0;

		while (index < list.size() && !willCollide)
		{			
			if (!list.get(index).equals(e)) // Do not check if it will collide with itself
				willCollide = isColliding (shape, list.get(index).getShape());			
			index++;			
		}	
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

	/** Does a somewhat cheap test to see if the two specified Shapes are
	 * intersecting by checking if the bounding rectangle of the smaller
	 * shape is intersecting with the larger shape.
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

	/** Determines if the specified Entity is within the boundaries of 
	 * this Spacetime.
	 * 
	 * @param e		the Entity to check
	 * @return true if the Entity is in bounds; false otherwise
	 */
	public boolean isInBounds (Entity e)
	{
		Rectangle2D bounds = layers.get(ENTITIES).getBounds();
		Shape shape = e.getShape().getBounds();
		return isColliding(bounds, shape);
	}

	/** Creates a visible blue explosion effect at the Entity's current location
	 * to mark its addition. This will automatically fade with time and then remove
	 * itself.
	 * 
	 * @param e			the Entity to mark
	 * @param scalar	the number of times bigger the explosion should be, compared
	 * 					to the Entity
	 */
	public void markAddition (Entity e, double scalar)
	{
		Box bounds = new Box (e.getShape().getBounds()).scale(scalar, scalar);

		double width = bounds.maxX - bounds.minX;
		double height = bounds.maxY - bounds.minY;

		addEntity (new Explosion (e.x, e.y, width, height, fadeTicks, addColor), FOLIAGE);		
	}

	/** Creates a visible red explosion effect at the Entity's current location
	 * to mark its removal. This will automatically fade with time and then remove
	 * itself.
	 * 
	 * @param e			the Entity to mark
	 * @param scalar	the number of times bigger the explosion should be, compared
	 * 					to the Entity
	 */
	public void markRemoval (Entity e, double scalar)
	{
		Box bounds = new Box (e.getShape().getBounds()).scale(scalar, scalar);

		double width = bounds.maxX - bounds.minX;
		double height = bounds.maxY - bounds.minY;

		addEntity (new Explosion (e.x, e.y, width, height, fadeTicks, removeColor), FOLIAGE);		
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

	/** Removes the specified Entity from the specified layer. 
	 * 
	 * @param e 		the Entity to be removed
	 * @param layer		the layer from which to remove the Entity
	 * @return true if this layer contained the specified Entity
	 * @throws IndexOutOfBoundsException if the layer is out of bounds 
	 * 		(layer < 0 || layer >= size())
	 */
	public boolean removeEntity (Entity e, int layer)
	{		
		QuadTree plane = layers.get(layer);

		boolean removed = plane.remove(e);
		if (removed)
			if (e.grid.equals (this))				
				e.grid = null;			

		return removed;		
	}	

	/** Sets the specified Entity to be removed from the ENTITIES
	 * layer at the start of the next tick. This method should be used
	 * instead of {@code removeEntity} if an Entity is removing another
	 * Entity. Otherwise, a NullPointerException may occur when Spacetime
	 * attempts to advance an Entity that has already been removed.
	 * 
	 * @param e		the Entity to be removed 
	 */
	public void requestRemoval (Entity e)
	{
		removalQueue.get(ENTITIES).add(e);
	}

	/** Sets the specified Entity to be removed from the ENTITIES
	 * layer at the start of the next tick. This method should be used
	 * instead of {@code removeEntity} if an Entity is removing another
	 * Entity. Otherwise, a NullPointerException may occur when Spacetime
	 * attempts to advance an Entity that has already been removed.
	 * 
	 * @param e			the Entity to be removed 
	 * @param layer		the layer from which to remove the Entity
	 * @throws IndexOutOfBoundsException if the layer is out of bounds 
	 * 		(layer < 0 || layer >= size())
	 */
	public void requestRemoval (Entity e, int layer)
	{
		removalQueue.get(layer).add(e);		
	}

	/** Advances the location and angle of the specified Entity based on
	 * its velocity and rotational velocity. 
	 * <p>
	 * If a collision occurs at the Entity's new location, this method will call 
	 * the {@code resolveCollision} method in the specified Entity, giving it 
	 * a List containing all of the Entities that are colliding with
	 * the specified Entity, as well as parameters defining the movement of the
	 * Entity. These parameters include the &#916;x, &#916;y, and &#916;&#952; of
	 * the movement. It is up to the Entity itself to decide how to resolve this collision.
	 * 
	 * @see Entity#resolveCollision
	 * 
	 * @param e		the Entity to move
	 * @return true if the Entity was moved without any collisions; false otherwise
	 */
	public boolean moveEntity (Entity e)
	{
		boolean moved = true;		

		e.vectorAdvance();		

		List<Entity> colliding = getCollidingEntities (e);
		if (colliding.size() != 0)
		{			
			e.resolveCollision(colliding, e.velocity.x, e.velocity.y, e.angularVelocity);					
			moved = false;
		}

		layers.get(ENTITIES).revalidate(e);

		return moved;
	}	

	/** Rotates the specified Entity by the indicated amount. 
	 * <p>
	 * If a collision occurs at the Entity's new location, this method will call 
	 * the {@code resolveCollision} method in the specified Entity, giving it 
	 * a List containing all of the Entities that are colliding with
	 * the specified Entity, as well as parameters defining the movement of the
	 * Entity. These parameters include the &#916;x, &#916;y, and &#916;&#952; of
	 * the movement. It is up to the Entity itself to decide how to resolve this collision.
	 * 
	 * @see Entity#resolveCollision
	 * 
	 * @param e				the Entity to rotate
	 * @param deltaTheta	the amount by which to rotate, in radians
	 * @return true if the Entity was rotated without any collisions; false otherwise	 
	 */
	public boolean rotateEntity (Entity e, double deltaTheta)
	{
		boolean rotated = true;		
		e.theta += deltaTheta;

		List<Entity> colliding = getCollidingEntities (e);		
		if (colliding.size() != 0)
		{		
			e.resolveCollision(colliding, 0, 0, deltaTheta);
			rotated = false;
		}

		layers.get(ENTITIES).revalidate(e);		

		return rotated;
	}

	/** Translates the specified Entity by the specified amount.
	 * <p>
	 * If a collision occurs at the Entity's new location, this method will call 
	 * the {@code resolveCollision} method in the specified Entity, giving it 
	 * a List containing all of the Entities that are colliding with
	 * the specified Entity, as well as parameters defining the movement of the
	 * Entity. These parameters include the &#916;x, &#916;y, and &#916;&#952; of
	 * the movement. It is up to the Entity itself to decide how to resolve this collision.
	 * 
	 * @see Entity#resolveCollision
	 * 	
	 * @param e		the Entity to translate
	 * @param dx	the distance to move this Entity along the X axis
	 * @param dy	the distance to move this Entity along the Y axis
	 * @return true if the Entity was moved without any collisions; false otherwise 
	 */
	public boolean translateEntity (Entity e, double dx, double dy)
	{
		boolean moved = true;		

		e.x += dx;
		e.y += dy;

		List<Entity> colliding = getCollidingEntities (e);
		if (colliding.size() != 0)
		{		
			e.resolveCollision(colliding, dx, dy, 0);
			moved = false;					
		}

		layers.get(ENTITIES).revalidate(e);

		return moved;		
	}
}

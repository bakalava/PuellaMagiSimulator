import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;

/** The abstract class representing a movable object. It has a defined
 * shape, and an x and y coordinate. It has a velocity vector and an 
 * angular velocity variable that are by default used  by the advance()
 * method to determine the Entity's displacement on the next tick. The
 * advance() method is called every time the simulation is advanced, so
 * advance() should be overridden by a subclass if customized behavior
 * is desired.
 * <p>
 * Note that when extending this class, you can override:<br>
 * <ul>
 * <li> advance()
 * <li> draw(Graphics g)
 * <li> notifyCollision(Entity instigator)  
 * <li> resolveCollision(List&#60;Entity&#62; colliding, double dX, double dY, double dTheta)
 * </ul>
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public abstract class Entity implements Serializable
{		
	private static final long serialVersionUID = 1L;

	/** If a variable is closer to 0 than this value, then 
	 * that variable is rounded to 0.
	 */
	public static final double precision = 0.00001;

	/** The Shape object representing the appearance and collision box of this Entity.
	 */
	protected Shape shape;

	/** The Spacetime object to which this Entity belongs.
	 */
	protected Spacetime grid;

	/** An index used by the QuadTree in the Spacetime grid to quickly
	 * locate the quadrant in which this Entity is currently located. 
	 */
	protected QuadTreePath path; //TODO: contain QuadBranch pointer?

	/** The Entity's x-coordinate, representing the centre of its shape. 
	 */
	protected double x;

	/** The Entity's y-coordinate, representing the centre of its shape. 
	 */
	protected double y;

	/** The Entity's current angle, in radians.
	 * 0 rad is pointing right. 1/2 pi rad is pointing down (since y
	 * coordinates increase downwards). Pi rad is pointing left.
	 * 3/2 pi rad is pointing up. 
	 */
	protected double theta;

	/** The x-coordinate of the rotational center for this Entity.
	 * This coordinate is relative to the Entity's x-coordinate.
	 * By default, this value is set to 0.
	 */
	protected double anchorX = 0;

	/** The y-coordinate of the rotational center for this Entity. 
	 * This coordinate is relative to the Entity's y-coordinate. 
	 * By default, this value is set to 0.
	 */
	protected double anchorY = 0;

	/** The x-coordinate offset for the Shape of this Entity. This is relative
	 * to the Entity's x-coordinate.
	 */
	protected double offsetX = 0;

	/** The y-coordinate offset for the Shape of this Entity. This is relative
	 * to the Entity's y-coordinate.
	 */
	protected double offsetY = 0;

	/** Determines the velocity and angular velocity decrease that occurs at the
	 * end of every tick. 0 indicates no decrease (frictionless). 1 indicates
	 * immediate nullification of velocities (high friction). 
	 */
	protected double friction = 0.5;

	/** The current velocity of this Entity. The x-component represents the 
	 * change in x-coordinate per tick, and the y-component represents the 
	 * change in y-coordinate per tick.
	 */
	protected Vector2D velocity = new Vector2D ();

	/** The rate by which this Entity rotates every tick. For example,
	 * if angularVelocity were pi/4, this Entity would rotate by pi/4
	 * radians every tick.
	 */
	protected double angularVelocity = 0;
	
	/** Constructs and initializes an Entity at the given
	 * coordinates with the given shape. The shape is 
	 * used for collision calculations. Sets this Entity's 
	 * default configurations by calling the setOffsetToDefault()
	 * and setRotateAnchorToDefault() methods.
	 * 
	 * @param x			the x-coordinate of the Entity
	 * @param y			the y-coordinate of the Entity
	 * @param shape		the shape of the Entity
	 */
	public Entity (double x, double y, Shape shape)
	{
		setLocation (x, y);
		setShape (shape);
		setOffsetToDefault();
		setRotateAnchorToDefault();				
	}

	/** Accelerates the velocity vector in the specified relative direction, 
	 * by the specified amount. The direction is relative to the Entity's current 
	 * orientation. For example, if this Entity were currently at a angle of pi/2 rad,
	 * and the parameters (0, 1) were passed into this method, the Entity would move
	 * in the pi/2 rad direction. 
	 * 
	 * @param relativeTheta		the angular orientation relative to the Entity's current angle, in radians
	 * @param amount 			the magnitude of the acceleration
	 */
	public void accelerate (double relativeTheta, double amount)
	{
		Vector2D vector = Vector2D.getVectorFromDirection(amount, theta + relativeTheta);		
		velocity.add(vector);
	}

	/** Adds the given vector to the current velocity vector.	 
	 * 
	 * @param vector		the vector to be added to the current velocity
	 */
	public void accelerate (Vector2D vector)
	{
		velocity.add(vector);
	}

	/** Changes the rotation rate by the specified amount.
	 * Adds <code>deltaAngularVelocity</code> to
	 * <code>angularVelocity</code>.
	 * 
	 * @param deltaAngularVelocity	the change in angular velocity, in radians per tick
	 */
	public void accelerateSpin (double deltaAngularVelocity)
	{
		angularVelocity += deltaAngularVelocity;
	}

	/** Advances the simulation by one tick. Override this method for
	 * custom behavior. By default, if the Entity is in motion, this method
	 * translates and rotates the Entity by the amounts specified by the 
	 * velocity and the angular velocity variables. This method then
	 * applies the friction, decreasing the velocity and angular 
	 * velocity as specified by the applyFriction() method.
	 */
	public void advance ()
	{		
		if (!isStatic())
		{			
			grid.moveEntity(this);
			applyFriction();			
		}		
	}	

	/** Reduces the velocity and angular velocity as specified
	 * by the friction variable. Multiples the magnitude of each
	 * variable by <code>(1 - friction)</code>, and rounds them to
	 * 0 if their magnitude is smaller than the {@code precision} variable.
	 */
	public void applyFriction ()
	{
		velocity.scale (1 - friction);			
		angularVelocity *= (1 - friction);
		if (velocity.getMagnitude() < precision)
			velocity.setLocation(0, 0);
		if (Math.abs(angularVelocity) < precision)
			angularVelocity = 0;
	}	

	/** Removes this Entity from the Spacetime grid. With no
	 * remaining references pointing to this object, the Java
	 * garbage collector will quickly clean out the memory occupied
	 * by this object.
	 */
	protected void die()
	{
		if (grid != null)
		{
			grid.markRemoval(this, 2);
			grid.removeEntity(this);
		}
	}

	/** Draws the Entity. This method should be overridden to
	 * specify color and any other custom drawing procedures.
	 * 
	 * @param g			the Graphics context in which to paint	
	 */
	public void draw (Graphics2D g)
	{
		g.fill(getShape());
	}

	/** Returns the Point2D.Double object representing the coordinates 
	 * of the rotational center of this Entity at its current location. 
	 * 
	 * @return the Point2D.Double object representing the current center coordinates
	 */
	public Point2D.Double getCenter ()
	{		
		return new Point2D.Double (x + anchorX, y + anchorY);
	}

	/** Returns a velocity vector specified by the given relative angle 
	 * and magnitude, based on this Entity's current angle. 
	 * For example, if this Entity were at an orientation 
	 * of pi/2 radians, and the parameters (0, 1) were passed into this 
	 * method, the vector would be at angle of pi/2 radians.
	 * 
	 * @param relativeTheta		the relative angle of the vector, in radians
	 * @param distance			the magnitude of the vector
	 * @return the Velocity object with the specified vector
	 */
	public Vector2D getRelativeVector (double relativeTheta, double distance)
	{		
		return Vector2D.getVectorFromDirection(distance, theta + relativeTheta);
	}

	/** Gets the Shape representation of this Entity at its current
	 * location and angle. This method is used by the Spacetime 
	 * class to determine collisions.
	 * <p>
	 * If the offsetX and offsetY variables are equal to 0, then the current
	 * x and y coordinates represent the point at the upper-left corner of the
	 * Shape's bounding rectangle, before rotation. If the offsetX and offsetY 
	 * variables are set to be equal to half of the Shape's width and height,
	 * the current x and y coordinates would represent the point at the center 
	 * of the Shape. 
	 * 
	 * @return the shape of this Entity at its current angle and location
	 */
	public Shape getShape ()
	{
		Rectangle2D bound = shape.getBounds2D();
		double dx = x + offsetX - bound.getX();
		double dy = y + offsetY - bound.getY();
		AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
		at.preConcatenate(AffineTransform.getRotateInstance (theta, anchorX + x, anchorY + y));		
		return at.createTransformedShape(shape);		
	}	

	/** Returns the Vector2D object representing this Entity's current velocity.
	 * 
	 * @return the velocity vector of this Entity
	 */
	public Vector2D getVelocity ()
	{
		return velocity;		
	}

	/** Determines if this Entity is currently in motion by looking at 
	 * its velocity and angular velocity.
	 *  
	 * @return true if this Entity's velocity is zero and its spin rate is zero; false otherwise
	 */
	public boolean isStatic ()
	{		
		return velocity.x == 0 && velocity.y == 0 && angularVelocity == 0;
	}

	/** Invoked by the {@code resolveCollision} method of an Entity that has
	 * collided into this Entity as a result of translation or rotation. This method 
	 * can be overridden to perform custom operations upon collision, such as damage dealing.
	 * <p>
	 * The default procedure is to nullify the vectors of the instigator Entity. 
	 *  
	 * @see #resolveCollision
	 * 
	 * @param instigator	the Entity that caused the collision
	 */
	public void notifyCollision (Entity instigator)
	{		
		instigator.getVelocity().scale(0);
		instigator.angularVelocity *= 0;		
	}

	/** This method is invoked by the Spacetime class if a collision resulted
	 * from the translation or rotation of this Entity. It is up to this Entity to
	 * resolve this collision. This method can be overridden to implement customized procedures.
	 * <p>
	 * The default procedure is to invoke the {@code notifyCollision} method of all
	 * of the Entities that this Entity is currently colliding with. While iterating 
	 * through the list of colliding Entities, this method checks if any of them are 
	 * Portals. If none of them are Portals, then this method reverts this Entity's 
	 * movement by subtracting the specified dX, dY, and dTheta values.
	 * 
	 * @see #notifyCollision
	 * 
	 * @param colliding		the List of all of the Entities that this Entity is colliding.
	 * @param dX			the distance moved along the x-axis
	 * @param dY			the distance moved along the y-axis
	 * @param dTheta		the angle rotated
	 */
	public void resolveCollision (List<Entity> colliding, double dX, double dY, double dTheta)
	{
		boolean portalCollision = false;
		for (Entity e : colliding)
		{
			e.notifyCollision(this);
			if (e instanceof Portal)
				portalCollision = true;
		}

		if (!portalCollision)
		{
			x -= dX;
			y -= dY;
			theta -= dTheta;
		}
	}

	/** Sets angular velocity to 0. 
	 */
	public void nullifySpin ()
	{
		angularVelocity = 0;
	}

	/** Sets velocity to (0,0). 
	 */
	public void nullifyVelocity ()
	{
		velocity.setLocation (0, 0);
	}
	
	/** Sets the angle of this Entity to the specified value
	 * without checking for collisions.
	 * 
	 * @param theta		the new angle, in radians
	 */
	protected void setAngle (double theta)
	{
		this.theta = theta;
	}

	/** Sets the location of this Entity to the specified coordinates
	 * without checking for collisions.
	 * 
	 * @param x		the new x-coordinate
	 * @param y		the new y-coordinate
	 */
	protected void setLocation (double x, double y)
	{
		this.x = x;
		this.y = y;		
	}

	/** Sets the location of this Entity by its rotational anchor without checking
	 * for collisions. 
	 * <p>
	 * For example, if a square is at (25,25) and its rotational center is a
	 * t a relative displacement of (5,5), calling {@code setLocationByCenter(100,100)}
	 * would set the Entity's actual x and y coordinates to (95,95). There would be 
	 * no difference in function between {@code setLocationByCenter(double,double)}
	 * and {@code setLocation(double,double)} if the Entity's rotational center 
	 * were exactly equal to its x and y coordinates.	
	 * 
	 * @param centerX		the new x-coordinate for the Entity's center
	 * @param centerY		the new y-coordinate for the Entity's center
	 */
	protected void setLocationByCenter (double centerX, double centerY)
	{
		x = centerX - anchorX;
		y = centerY - anchorY;
	}
	

	/** Sets the offset for the Shape of this Entity. In the getShape() 
	 * method, the shape is translated by this offset before rotation.
	 * This method shifts the rotational anchor coordinates in proportion 
	 * to the change in the offset so that the rotational center remains the 
	 * same distance from the top left corner of the shape.
	 * <p>
	 * You could use call {@code setOffset(-width/2, -height/2)} to make it so 
	 * that the Entity's x-coordinate and y-coordinate are equal to the
	 * Entity's center. This might make calculations less troublesome. 
	 * 
	 * @param relativeX		the x-coordinate offset
	 * @param relativeY		the y-coordinate offset
	 */
	public void setOffset (double relativeX, double relativeY)
	{
		double dx = relativeX - offsetX;
		double dy = relativeY - offsetY;

		offsetX = relativeX;
		offsetY = relativeY;

		anchorX += dx;
		anchorY += dy;
	}
	
	/** Sets the default offset of (-width/2, -height/2). 
	 */
	public void setOffsetToDefault ()
	{
		Rectangle2D bounds = shape.getBounds2D();
		double relativeX = -bounds.getWidth() / 2;
		double relativeY = -bounds.getHeight() / 2;
		setOffset(relativeX, relativeY);
	}

	/** Sets the anchor point around which to rotate this shape, 
	 * relative to the upper-left corner of the bounding rectangle 
	 * of this Entity's shape before rotation.
	 * 
	 * @param relativeX		the relative x-coordinate of the anchor
	 * @param relativeY		the relative y-coordinate of the anchor
	 */
	public void setRotateAnchor (double relativeX, double relativeY)
	{
		anchorX = relativeX;
		anchorY = relativeY;
	}

	/** Resets the anchor point to its default value at the center 
	 * of the bounding rectangle of the Entity's shape before rotation.
	 * 
	 */
	public void setRotateAnchorToDefault ()
	{		
		setRotateAnchor (0, 0);
	}

	/** Sets the shape of this Entity. If the specified Shape
	 * is an instance of Area, this method converts it to a Path2D.Double
	 * since Area objects are not serializable. Otherwise, the Shape is 
	 * aliased, so be careful.
	 * 
	 * @param s		the new Shape for this Entity
	 */
	public void setShape (Shape s)
	{
		if (s instanceof Area) // convert Area to Path2D.Double, because Area is not serializable
			s = new AffineTransform().createTransformedShape(s);
		shape = s;
	}

	/** Attempts to rotate this Entity by the specified angle.
	 * Only proceeds with the rotation if no collision occurs at
	 * the Entity's new orientation.
	 * 
	 * @param deltaTheta	the angle change, in radians
	 * @return true if the Entity was successfully rotated; false otherwise
	 */
	public boolean rotate (double deltaTheta)
	{	
		return grid.rotateEntity(this, deltaTheta);
	}

	/** Attempts to translate this Entity by the specified amount.
	 * Only proceeds with the translation if no collision occurs at
	 * the Entity's new location.
	 * 
	 * @param dx	the distance to move this Entity along the x-axis
	 * @param dy	the distance to move this Entity along the y-axis
	 * @return true if the Entity was successfully moved; false otherwise
	 */
	public boolean translate (double dx, double dy)
	{
		return grid.translateEntity(this, dx, dy);
	}

	/** Attempts to translate this Entity by the specified distance, 
	 * at the specified relative angle. The direction of the translation
	 * is relative to the Entity's current angle. Only proceeds with the
	 * translation if no collision occurs at the Entity's new location.
	 * 
	 * @param distance			how far to move this Entity
	 * @param relativeTheta		the relative angle of the vector, in radians
	 * 
	 * @return true if the Entity was successfully moved; false otherwise
	 */
	public boolean translateAtAngle (double distance, double relativeTheta)
	{
		Vector2D vector = getRelativeVector (relativeTheta, distance);		
		return translate (vector.x, vector.y);
	}

	/** Advances this Entity's location and angle based on its velocity vector 
	 * and angular velocity. Does not check for collisions. 
	 */
	protected void vectorAdvance ()
	{
		x += velocity.x;
		y += velocity.y;
		theta += angularVelocity;
	}

	/** Reverts this Entity's location and angle based on its velocity vector 
	 * and angular velocity. i.e. applies the inverse of those vectors. 
	 * Does not check for collisions.
	 */
	protected void vectorRevert ()
	{
		x -= velocity.x;
		y -= velocity.y;
		theta -= angularVelocity;
	}
}

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class Entity 
{	
	/** If a variable is closer to 0 than this value, then 
	 * that variable is rounded to 0.
	 */
	public static double precision = 0.000001;
	
	/** The Shape object representing the shape of this Entity.
	 */
	protected Shape shape;
	
	/** The Spacetime object to which this Entity belongs.
	 */
	protected Spacetime grid;
	
	/** The Entity's x-coordinate. 
	 */
	protected double x;
	
	/** The Entity's y-coordinate. 
	 */
	protected double y;
	
	/** The Entity's current angle, in radians.
	 * 0 rad is pointing right. pi/2 rad is pointing down (since y
	 * coordinates increase downwards). Pi rad is pointing left.
	 * 3*pi/2 rad is pointing up. 
	 */
	protected double theta;
	
	/** The x-coordinate of the rotational center for this Entity.
	 * This coordinate is relative to the Entity's x-coordinate.
	 */
	protected double anchorX = 0;
	
	/** The y-coordinate of the rotational center for this Entity. 
	 * This coordinate is relative to the Entity's y-coordinate.
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
	 * coordinates, with the given shape. The shape is 
	 * used for collision calculations. Sets this Entity's 
	 * rotational anchor to its default value by calling the
	 * setRotateAnchorToDefault() method.
	 * 
	 * @param x			the x-coordinate of the Entity
	 * @param y			the y-coordinate of the Entity
	 * @param shape		the shape of the Entity
	 */
	public Entity (double x, double y, Shape shape)
	{
		setLocation (x, y);
		this.shape = shape;
		setRotateAnchorToDefault();
	}

	/** Accelerates in the specified relative direction, by the specified amount.
	 * The direction is relative to the Entity's current orientation. For
	 * example, if this Entity were currently at a rotation of pi/2 rad, and
	 * the parameters (0, 1) were passed into this method, the Entity would move
	 * in the pi/2 rad direction. 
	 * 
	 * @param relativeTheta		the angular orientation relative to the Entity's current angle, in radians
	 * @param amount 			the magnitude of the acceleration
	 */
	public void accelerate (double relativeTheta, double amount)
	{
		Vector2D vector = new Vector2D (amount, 0);
		vector.rotate(theta + relativeTheta);
		velocity.add(vector);
	}
	
	/** Changes velocity as specified by the given vector.
	 * The new velocity is determined by adding the current
	 * velocity vector to the given vector.
	 * 
	 * @param vector		the vector to be added to the current velocity
	 */
	public void accelerate (Vector2D vector)
	{
		velocity.add(vector);
	}
	
	/** Changes the rotation rate by the specified amount.
	 * 
	 * @param deltaAngularVelocity	the change in angular velocity, in radians per tick
	 */
	public void accelerateSpin (double deltaAngularVelocity)
	{
		angularVelocity += deltaAngularVelocity;
	}
	
	/** Reduces the velocity and angular velocity as specified
	 * by the friction variable. 
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

	/**
	 * Advances the simulation by one tick.
	 */
	public void advance ()
	{
		if (!isStatic())
		{
			grid.moveEntity(this);
			applyFriction();	
		}		
	}		
	
	/** Draws the Entity.
	 * 
	 * @param g			the Graphics context in which to paint	
	 */
	public void draw (Graphics2D g)
	{
		g.fill(getShape());
	}
	
	/** Determines the rotational center of this Entity at
	 * its current location. 
	 * 
	 * @return a Point2D.Double object containing the center coordinates
	 */
	public Point2D.Double getCenterLocation ()
	{
		return new Point2D.Double (x + anchorX, y + anchorY);
	}
	
	/** Returns a velocity vector specified by the given relative angle 
	 * and magnitude. This vector's angle is relative to the Entity's 
	 * current angle. For example, if this Entity were at an orientation 
	 * of pi/2 radians, and the parameters (0, 1) were passed into this 
	 * method, the vector would be at angle of pi/2 radians.
	 * 
	 * @param relativeTheta		the relative angle of the vector, in radians
	 * @param distance			the magnitude of the vector
	 * @return the Velocity object with the specified vector
	 */
	public Vector2D getRelativeVector (double relativeTheta, double distance)
	{
		Vector2D vector = new Vector2D (distance, 0);
		vector.rotate(theta + relativeTheta);
		return vector;
	}

	/** Gets the Shape representation of this Entity at its current
	 * location and orientation. This method is used by the Spacetime 
	 * class to determine collisions.
	 * <br>
	 * <br>
	 * If the offsetX and offsetY variables are equal to 0, then the current
	 * x and y coordinates represent the point at the upper-left corner of the
	 * Shape's bounding rectangle, before rotation. Another option is setting 
	 * offsetX and offsetY to be equal to half of the Shape's width and height.
	 * In this case, the current x and y coordinates would represent the point
	 * at the center of the Shape. 
	 * 
	 * @return the shape of this Entity
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
	
	/** Gets the Vector2D object representing this Entity's velocity.
	 * 
	 * @return the velocity vector of this Entity
	 */
	public Vector2D getVector ()
	{
		return velocity;		
	}

	/** Determines whether this Entity is currently in motion. 
	 * This is determined by analyzing the velocity and angular velocity.
	 *  
	 * @return true if this Entity's velocity is zero and its spin rate is zero; false otherwise
	 */
	public boolean isStatic ()
	{		
		return velocity.x == 0 && velocity.y == 0 && angularVelocity == 0;
	}
	
	/** Called by the moveEntity() method in Spacetime if a collision
	 * occurs. Default action is revert the instigator's movement,
	 * and then nullify its vectors. This method can be overridden 
	 * to do whatever.
	 * 
	 * @param instigator	the Entity that caused the collision
	 */
	public void notifyCollision (Entity instigator)
	{	
		instigator.vectorRevert();
		instigator.getVector().scale(0);
		instigator.angularVelocity *= 0;
	}
	
	/** Sets the angular velocity to 0. 
	 */
	public void nullifySpin ()
	{
		angularVelocity = 0;
	}
	
	/** Sets the velocity to 0. 
	 */
	public void nullifyVelocity ()
	{
		velocity.setLocation (0, 0);
	}

	/** Sets the location of this Entity to the specified coordinates.
	 * This method does not check for collisions. 
	 * 
	 * @param x		the new x-coordinate
	 * @param y		the new y-coordinate
	 */
	protected void setLocation (double x, double y)
	{
		this.x = x;
		this.y = y;		
	}
	
	/** Sets the location of this Entity based on the specified rotational
	 * center coordinates. This method does not check for collisions.
	 * 
	 * @param centerX		the new x-coordinate for the Entity's center
	 * @param centerY		the new y-coordinate for the Entity's center
	 */
	protected void setLocationByCenter (double centerX, double centerY)
	{
		x = centerX - anchorX;
		y = centerY - anchorY;
	}

	/** Sets the angle of this Entity to the specified value.
	 * This method does not check for collisions.
	 * 
	 * @param theta		the new angle, in radians
	 */
	protected void setAngle (double theta)
	{
		this.theta = theta;
	}

	/** Sets the offset for the Shape of this Entity. In the getShape() 
	 * method, the shape is translated by this offset before rotation.
	 * Shifts the rotational anchor coordinates with the offset.
	 * <br>
	 * <br>
	 * You could use parameters of (-width/2, -height/2) to make it so 
	 * that the Entity's x-coordinate and y-coordinate are equal to the
	 * Entity's center. This might make calculations less troublesome.
	 * I don't know. 
	 * 
	 * @param relativeX		the x-coordinate offset
	 * @param relativeY		the y-coordinate offset
	 */
	public void setOffset (double relativeX, double relativeY)
	{
		offsetX = relativeX;
		offsetY = relativeY;
		anchorX += offsetX;
		anchorY += offsetY;
	}

	/** Sets the anchor point around which to rotate this shape.
	 * These coordinates are relative to the upper-left corner of
	 * the bounding rectangle of this Entity's shape at the Entity's
	 * current location, before rotation.
	 * 
	 * @param relativeX		the relative x-coordinate of the anchor
	 * @param relativeY		the relative y-coordinate of the anchor
	 */
	public void setRotateAnchor (double relativeX, double relativeY)
	{
		anchorX = relativeX;
		anchorY = relativeY;
	}

	/** Resets the anchor point around which to rotate this shape.
	 * The anchor point is set to its default value, which is equal to
	 * the center of the bounding rectangle of the Entity's shape before
	 * rotation. 
	 */
	public void setRotateAnchorToDefault ()
	{
		Rectangle2D bounds = shape.getBounds2D();
		double relativeX = bounds.getWidth() / 2;
		double relativeY = bounds.getHeight() / 2;
		setRotateAnchor (relativeX, relativeY);
	}

	/** Attempts to rotates this Entity by the specified angle.
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
	 * and angular velocity. Basically applies the inverse of those vectors. 
	 * Does not check for collisions.
	 */
	protected void vectorRevert ()
	{
		x -= velocity.x;
		y -= velocity.y;
		theta -= angularVelocity;
	}
}

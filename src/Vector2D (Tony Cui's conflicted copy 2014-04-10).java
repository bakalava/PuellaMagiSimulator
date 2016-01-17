import java.awt.geom.Point2D;

/** A class representing a position vector.
 * The x variable represents the change in x that this vector would effect.
 * The y variable represents the change in y that this vector would effect.
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class Vector2D extends Point2D.Double 
{		
	/** Creates a new velocity vector with x and y components of (0,0).
	 */
	public Vector2D ()
	{
		super ();
	}

	/** Creates a new velocity vector with the specified
	 * x component and y component.
	 * 
	 * @param dx	x component of vector
	 * @param dy	y component of vector
	 */
	public Vector2D(double dx, double dy) 
	{		
		super(dx, dy);		
	}

	/** Creates a new Vector identical to the specified vector.
	 * 
	 * @param v		the Vector2D to copy off of
	 */
	public Vector2D (Vector2D v)
	{
		super (v.x, v.y);
	}

	/** Returns a new Vector2D defined by the indicated magnitude,
	 * and x and y components. The x and y components are just used
	 * a ratio; the direction of the vector. Thus, x and y components
	 * of 2,3 and 4,6 would be equivalent. 
	 * 
	 * @param magnitude		the length of the vector
	 * @param x				the x component of the vector
	 * @param y				the y component of the vector
	 * @return the new Vector2D
	 */
	public static Vector2D getVectorFromDirection (double magnitude, double x, double y)
	{
		Vector2D v = new Vector2D (x, y);
		v.setMagnitude(magnitude);
		return v;
	}
	
	/** Returns a new Vector2D defined by the indicated magnitude and angle. 
	 * 
	 * @param magnitude		the length of the vector
	 * @param theta			the direction of the vector, in radians
	 * @return the new Vector2D
	 */
	public static Vector2D getVectorFromDirection (double magnitude, double theta)
	{
		Vector2D v = new Vector2D (magnitude, 0);
		v.rotate (theta);
		return v;
	}

	/** Returns a new Vector2D defined by the indicated x component
	 * and y component.
	 * 
	 * @param dx	the x component
	 * @param dy	the y component
	 * @return the new Vector2D
	 */
	public static Vector2D getVectorFromPosition (double dx, double dy)
	{
		return new Vector2D (dx, dy);
	}

	/** Adds the specified vector to this vector.
	 * 
	 * @param v		the vector to be added
	 */
	public void add (Vector2D v)
	{
		x += v.x;
		y += v.y;
	}

	/** Determines the direction of this vector.
	 * 
	 * @return the direction as an angle, in radians
	 */
	public double getAngle ()
	{
		return Math.atan2(y, x);
	}

	/** Uses the Pythagorean theorem on the x and y components to 
	 * determine the length of this vector.
	 * 
	 * @return the length of this vector
	 */
	public double getMagnitude ()
	{
		return Math.sqrt (x * x + y * y);
	}

	/** Rotates this vector about the origin by the
	 * specified angle.
	 * 
	 * @param deltaTheta	the angle to rotate, in radians
	 */
	public void rotate (double deltaTheta)
	{
		double x1 = x;
		double y1 = y;
		x = x1 * Math.cos(deltaTheta) - y1 * Math.sin(deltaTheta);
		y = x1 * Math.sin(deltaTheta) + y1 * Math.cos(deltaTheta);		
	}

	/** Multiplies this vector by the specified scalar.
	 * If the scalar > 1, the vector will get longer.
	 * If the scalar < 1, the vector will get shorter.
	 * 
	 * @param scalar	the scalar by which the x and y values will be multiplied
	 */
	public void scale (double scalar)
	{
		x *= scalar;
		y *= scalar;		
	}

	/** Sets the length of this vector while maintaining its
	 * current direction.
	 * 
	 * @param magnitude		the new vector length
	 */
	public void setMagnitude (double magnitude)
	{					
		double angle = Math.atan2 (y, x);
		x = magnitude * Math.cos(angle);
		y = magnitude * Math.sin(angle);		
	}
}

import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/* *********************************************************************** *
 * Box.java                                                                *
 * *********************************************************************** *
 * date created    : August, 2012                                          *
 * email           : info@kirstywilliams.co.uk                             *
 * author          : Kirsty Williams                                       *
 * version         : 1.0                                                   *
 * *********************************************************************** */

/** A box that encompasses all x-coordinates such that minX <= x < maxX,
 * and all y-coordinates such that minY <= y < maxY.  
 * <p>
 * This class was copied directly from a QuadTree example from the Internet
 * at: http://kirstywilliams.co.uk/blog/2012/08/quadtrees-java-implementation/
 */
public class Box implements Serializable 
{	
	private static final long serialVersionUID = 1L;

	public final double minX;
	public final double minY;
	public final double maxX;
	public final double maxY;
	public final double centreX;
	public final double centreY;

	/** Creates a new Box with the specified corner points.
	 * 
	 * @param minX		the smallest x-coordinate of the Box.
	 * @param minY		the smallest y-coordinate of the Box.
	 * @param maxX 		the largest x-coordinate of the Box.
	 * @param maxY		the largest y-coordinate of the Box.
	 */
	public Box(double minX, double minY, double maxX, double maxY) 
	{
		this.minX = Math.min(minX, maxX);
		this.minY = Math.min(minY, maxY);
		this.maxX = Math.max(minX, maxX);
		this.maxY = Math.max(minY, maxY);
		this.centreX = (minX + maxX) / 2;
		this.centreY = (minY + maxY) / 2;
	}

	/** Creates a new Box with the same dimensions and 
	 * coordinates as the specified Rectangle2D.
	 * 
	 * @param rect		the specified Rectangle2D
	 */
	public Box(Rectangle2D rect)
	{
		minX = rect.getMinX();
		minY = rect.getMinY();
		maxX = rect.getMaxX();
		maxY = rect.getMaxY();
		centreX = rect.getCenterX();
		centreY = rect.getCenterY();
	}

	/** Determines if the specified point is contained within this box.
	 * 
	 * @param x		the x-coordinate of the point
	 * @param y		the y-coordinate of the point
	 * @return true if the point is inside of this box; false otherwise
	 */
	public boolean contains(double x, double y) 
	{
		return (x >= this.minX &&
				y >= this.minY &&
				x < this.maxX &&
				y < this.maxY);
	}
	

	/** Determines if the specified Box is contained within this box.
	 * 
	 * @param box	the Box to check for containment
	 * @return true if the specified Box is entirely enclosed by this Box,
	 * 		of if the specified Box is exactly equal to this Box; false otherwise
	 */
	public boolean contains(Box box) 
	{
		return (box.minX >= this.minX &&
				box.minY >= this.minY &&
				box.maxX < this.maxX && //TODO: inclusive or exclusive?
				box.maxY < this.maxY);
	}
	
	/** Returns a Rectangle2D.Double object with the same location and dimensions
	 * as this box.
	 * 
	 * @return the Rectangle2D.Double equivalent of this box
	 */
	public Rectangle2D.Double getBounds2D()
	{
		return new Rectangle2D.Double (minX, minY, maxX - minX, maxY - minY);
	}
	

	/** Calculates the intersection of the specified Box with this one.
	 * Returns null if the Boxes do not overlap.
	 * 
	 * @param r		the Box to intersect with
	 * @return the largest Box contained in both the specified Box and in this Box; 
	 * 	or if the rectangles do not intersect, null.
	 */
	public Box intersection(Box r) 
	{
		Box newBox;

		double tempX1 = this.minX;
		double tempY1 = this.minY;
		double tempX2 = this.maxX;
		double tempY2 = this.maxY;

		if (this.minX < r.minX) 
			tempX1 = r.minX;
		if (this.minY < r.minY) 
			tempY1 = r.minY;
		if (tempX2 > r.maxX) 
			tempX2 = r.maxX;
		if (tempY2 > r.maxY) 
			tempY2 = r.maxY;

		if(tempX2-tempX1 <=0.f || tempY2-tempY1 <= 0.f)
			newBox = null;
		else
			newBox = new Box (tempX1, tempY1, tempX2, tempY2);

		return newBox;
	}	
	
	
	/** Determines if this Box intersects with the specified Box.
	 * 
	 * @param other		the Box with which to check intersection
	 * @return true if there is an intersection; false otherwise
	 */
	public boolean intersects(Box other) 
	{
		boolean intersecting;
		if ((this.maxX - this.minX) <= 0 || (this.maxY - this.minY) <= 0) 
		{
			intersecting = false;
		}
		else
		{
			intersecting = other.maxX > this.minX &&
					other.maxY > this.minY &&
					other.minX < this.maxX &&
					other.minY < this.maxY;
		}
		return intersecting;
	}
	

	/** Computes and returns the union of this Box and the specified Box.
	 * 
	 * @param b		the specified Box
	 * @return the smallest Box containing both the specified Box and this Box.
	 */
	public Box union(Box b) 
	{
		return new Box( Math.min(this.minX, b.minX),
				Math.min(this.minY, b.minY),
				Math.max(this.maxX, b.maxX),
				Math.max(this.maxY, b.maxY));
	}

	/** Calculates the distance from the indicated coordinates to this Box.	
	 *  
	 * @param x		the x-coordinate
	 * @param y		the y-coordinate
	 * @return the smallest distance from the coordinates to this Box; 0.0 if
	 * 		the coordinates are inside of this Box
	 */
	public double calcDist(double x, double y) 
	{
		double distanceX;
		double distanceY;

		if (this.minX <= x && x <= this.maxX) 		
			distanceX = 0;		 
		else 		
			distanceX = Math.min(Math.abs(this.minX - x), Math.abs(this.maxX - x));
		
		if (this.minY <= y && y <= this.maxY) 		
			distanceY = 0;		
		else 		
			distanceY = Math.min(Math.abs(this.minY - y), Math.abs(this.maxY - y));
		
		return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
	}

	/** Computes and returns a copy of this Box that is scaled as specified by the given scalars.
	 * The Box is scaled with respect to its centre.
	 * 
	 * @param scaleX	the scalar by which to stretch the Box widthwise
	 * @param scaleY	the scalar by which to stretch the Box heightwise
	 * @return the scaled version of this Box
	 */
	public Box scale(double scaleX, double scaleY) 
	{
		scaleY *= this.centreY - this.minY;
		scaleX *= this.centreX - this.minX;
		return new Box(this.minX - scaleX, this.minY-scaleY, this.maxX + scaleX, this.maxY + scaleY);
	}
	
	/** Returns a Rectangle2D.Double object with the same dimensions and
	 * coordinates as this Box.
	 *  
	 * @return a Rectangle2D.Double object identical to this Box.
	 */
	public Rectangle2D toRectangle()
	{
		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}

	/** Returns the String representation of this Box. 
	 * 
	 * @return a String representing this Box
	 */
	@Override
	public String toString() {
		return "upperLeft: (" + minX + ", " + minY + ") lowerRight: (" + maxX + ", " + maxY + ")";
	}

}
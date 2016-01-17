import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/** A class representing a Portal. It is visually represented as a 
 * blue one-dimensional line of thickness 1. The length of the Portal 
 * can be specified. 
 * <p>
 * Though Entities can enter the Portal from either direction, it will 
 * only deposit exiting Entities in the forward facing direction. For example, 
 * if the Portal is at a rotation of 0 radians, it looks like a vertical line, 
 * and it deposits Entities towards the right.
 * <p>
 * Portals preserve relative angles. If two Portals are at an angle to each other,
 * then Entities entering one Portal will emerge rotated from the other Portal 
 * such that the relative position of either Portal remains the same.
 * <p>
 * Needless to say, two Portals are necessary for desired functionality. Call
 * the {@code setTarget(Portal)} method of both Portals to link them together.
 * Note that it is possible to have one-way Portals.  
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class Portal extends Entity 
{	
	private static final long serialVersionUID = 1L;

	private Portal target;		

	/** Constructs a new portal at the specified coordinates,
	 * of the specified length.
	 * 
	 * @param x			the x-coordinate of this Portal
	 * @param y			the y-coordinate of this Portal
	 * @param length	the length of this Portal
	 */
	public Portal(double x, double y, int length) 
	{
		super(x, y, new Rectangle (1, length));		
	}

	@Override
	public void draw (Graphics2D g) 
	{	
		g.setColor(Color.blue);		
		super.draw(g);		
	}		

	/** Teleports the instigator Entity over to the target portal,
	 * if one is set. Invokes the {@code ensureNoCollision(Entity)} method
	 * in Spacetime to ensure that the Entity does not materialize on
	 * top of another Entity. Ah, the bane of teleportation.
	 */
	@Override
	public void notifyCollision (Entity instigator)
	{
		if (target != null)
		{					
			// Rotate instigator about the portal center by the relative difference	
			double deltaTheta = target.theta - theta;		

			// Additional rotation adjustment based on Entity approach direction
			if (Math.abs(instigator.getVelocity().getAngle() - theta) % (2 * Math.PI) > Math.PI / 2)
				deltaTheta += Math.PI;			

			Point2D.Double point = instigator.getCenter();			

			AffineTransform at2 = AffineTransform.getRotateInstance(deltaTheta, x, y);
			AffineTransform at3 = AffineTransform.getTranslateInstance(target.x - x, target.y - y);				

			at2.transform(point, point);
			at3.transform(point, point);

			Vector2D entityVector = instigator.getVelocity();

			// Set instigator at new location

			instigator.setLocationByCenter (point.x, point.y);
			instigator.setAngle (instigator.theta + deltaTheta);
			entityVector.rotate(deltaTheta);

			// Nudge instigator so that it won't be in contact with the portal when it appears						

			Vector2D away = Vector2D.getVectorFromDirection(1, target.theta);

			// Remove instigator from this grid and add it to the target grid
			instigator.grid.removeEntity(instigator);
			target.grid.addEntity(instigator);	

			do
			{							
				instigator.setLocation (instigator.x + away.x, instigator.y + away.y);
			}
			while (instigator.grid.isColliding(instigator.getShape(), target.getShape()));

			instigator.grid.ensureNoCollision(instigator);			
		}
	}

	/** Sets the target Portal for this Portal. Note that it 
	 * is possible to have a one-way Portal. If a two-way Portal
	 * is desired, you must add this Portal to the other Portal and
	 * vice versa. It is possible to have the two Portals in separate
	 * Spacetime objects.
	 * 
	 * @param portal	the Portal that this Portal will lead to
	 */
	public void setTarget (Portal portal)
	{
		target = portal;		
	}
}

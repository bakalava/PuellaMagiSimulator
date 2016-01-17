import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


/** The class representing the strongest Witch of all, 
 * the WALPURGISNACHT. Otherwise known as the giant gear.
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class Walpurgisnacht extends Gear 
{	
	private static final long serialVersionUID = 1L;

	/** How fast the Walpurgisnacht can move.
	 */
	private int maxSpeed = 3;
	
	/** Current target of the Walpurgisnacht.
	 */
	private Entity currentTarget;
	
	/** Counter variable for the number of entities the
	 * Walpurgisnacht has killed.
	 */
	private int killCounter = 0;
	
	/** How far the Walpurgisnacht can search for more prey.
	 */
	private int searchRadius = 1000;

	/** Constructor for a new Walpurgisnacht using a given location.
	 * @param x			the x-coordinate of the Walpurgisnacht
	 * @param y			the y-coordinate f the Walpurgisnacht
	 */
	public Walpurgisnacht (double x, double y) 
	{
		super(x, y, 200, 14, 15);		
		accelerateSpin (Math.PI / 800);		
		friction = 0;				
	}	

	/** Displays the Walpurgisnacht by drawing it onto a
	 * Graphics object.
	 */
	public void draw (Graphics2D g)
	{
		super.draw(g);
	}

	@Override
	public void advance ()
	{					
		if (currentTarget == null )
		{
			if (!determineNewTarget())			
			{
				if (searchRadius < 100000)
					searchRadius += 100;
				else				
					searchRadius -= 100;				
			}
		}
		else if (currentTarget.grid != null)
		{
			Point2D.Double myCenter = getCenter();
			Point2D.Double itsCenter = currentTarget.getCenter();

			double dx = itsCenter.x - myCenter.x;
			double dy = itsCenter.y - myCenter.y;			

			if (velocity.getMagnitude() <= maxSpeed)
			{	
				Vector2D add = Vector2D.getVectorFromDirection(0.1, dx, dy);
				accelerate(add);
			}
			else
			{
				velocity.setAngle(dx, dy);
			}
		}	
		else
		{
			currentTarget = null;
		}
		
		super.advance();
	}
	
	/** Getter method for the number of entities the 
	 * Walpurgisnacht has killed.
	 * @return		the number of deaths the Walpurgisnacht has caused
	 */
	public int getKillCount()
	{
		return killCounter;
	}
	
	/** Getter method for the search radius of the Walpurgisnacht.
	 * @return		the search radius of the Walpurgisnacht
	 */
	public int getSearchRadius()
	{
		return searchRadius;
	}

	@Override
	public void resolveCollision (List<Entity> colliding, double dx, double dy, double dTheta)
	{
		boolean wallCollision = false;
		for (Entity e : colliding)
		{
			if (e instanceof Wall)			
				wallCollision = true;			
			else
				kill (e);			
		}

		if (wallCollision)
		{
			nullifyVelocity();
			x -= dx;
			y -= dy;
		}
	}
	
	/** Kills the specified Entity in a spectacular fashion.
	 * 
	 * @param target
	 */
	private void kill (Entity target)
	{
		grid.markRemoval(target, 4);
		grid.requestRemoval(target);		
		killCounter++;		
		currentTarget = null;
	}

	/** Finds the nearest Returns true if a new target was acquired; false otherwise
	 * 
	 * @return true if a new target was acquired; false otherwise
	 */
	private boolean determineNewTarget()
	{
		// Initialization of Variables

		List<Entity> temp = grid.getEntities(this, searchRadius);
		List<Entity> targets = new ArrayList<Entity>(temp.size());
		boolean targetAcquired = false;		

		// Removing Walls from list because they can't be killed

		for (Entity e : temp)
			if (!(e instanceof Wall))
				targets.add(e);		

		// Find closest Entity

		if (targets.size() != 0)	
		{
			targetAcquired = true;

			Entity punyTarget = targets.get(0);

			Point2D.Double myCenter = getCenter();
			Point2D.Double itsCenter = punyTarget.getCenter();

			double shortestDistance = getDistanceSquared(myCenter, itsCenter);

			for (int i = 1; i < targets.size(); i++)
			{
				Entity e = targets.get(i);
				itsCenter = e.getCenter();
				double thisDistance = getDistanceSquared(myCenter, itsCenter);

				if (thisDistance < shortestDistance)
				{
					punyTarget = e;
					shortestDistance = thisDistance;
				}
			}
			currentTarget = punyTarget;			
		}

		return targetAcquired;
	}

	/** Calculates the square of the distance between two points. This
	 * is used for a comparison of two distances, so comparing the squares
	 * of the two distances are just as valid as the two distances. Computing
	 * two unnecessary square roots would just be a waste of time. 
	 * 
	 * @param a 	the first point
	 * @param b		the second point
	 * @return the square of the distance between the two points
	 */
	private double getDistanceSquared (Point2D.Double a, Point2D.Double b)
	{
		return ((b.x - a.x) * (b.x - a.x)) + ((b.y - a.y) * (b.y - a.y));
	}
}

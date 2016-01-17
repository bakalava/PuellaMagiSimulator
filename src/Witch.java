import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.*;


/** The class used to represent a Witch.
 * 
 * @author Tony Cui
 * @author Jiayin Huang
 * @author Sally Hui
 */
public class Witch extends Entity 
{
	private static final long serialVersionUID = 1L;

	/** Boolean representation of whether the witch is born.
	 */
	private boolean birth;
	
	/** The counter used for the Witch.
	 */
	private int counter;

	/** Constructor for a new Witch.
	 * 
	 * @param x		the x-coordinate of the witch
	 * @param y		the y-coordinate of the witch
	 */
	public Witch (double x, double y)
	{
		super(x, y,  new Ellipse2D.Double(0, 0, 7, 7)); // method in Entity class
		birth = true;
		counter = 100;
	}

	/** Constructor for a new Witch that was created
	 * as the result of a magical girl transformation.
	 * 
	 * @param p		the Puella transformed
	 */
	public Witch (Puella p)
	{
		super(p.x, p.y, new Ellipse2D.Double(0, 0, 7, 7));
		birth = true;
		counter = 100;
	}

	/** Advances the witch in the simulation by one tick.
	 * Overrides the advance() method in its parent class.
	 */
	public void advance ()
	{
		super.advance(); // translates, rotates, and applies friction
		if(counter > 0)
			counter--;
		else			
			move(grid.getEntities(Human.class));
	}

	/** Deals with collisions.
	 */
	public void resolveCollision (List<Entity> colliding, double dx, double dy, double dTheta)
	{
		super.resolveCollision(colliding, dx, dy, dTheta); // use parent class's collision method
		
		for (Entity e : colliding) // goes through the list of entities that are colliding
		{
			if (e instanceof Human) // the witch meets a human
			{
				((Human) e).WitchKiss();
			}
		}
	}
	
	/** Generates the next move of the witch, based on
	 * the shortest distance from it to its prey. This allows
	 * the Witch to move in 8 directions.
	 * 
	 * @param prey		the entity that the witch is focussing on
	 * @return			the point in array form of the next move
	 */
	public int[] move (ArrayList<Entity> prey)
	{
		int[] direct = new int[2];
		
		if(prey.size() != 0)
		{			
			double[] distance = new double[prey.size()];
			int closest = 0;
			double a, b;

			for(int i = 0; i < prey.size(); i ++)
			{
				distance[i] = Math.sqrt((x - prey.get(i).x) * (x - prey.get(i).x) + (y - prey.get(i).y) * (y - prey.get(i).y));
			}

			for(int i = 1; i < distance.length; i++)
			{
				Entity temp = prey.get(i);
				if(distance[i] < distance[closest])
				{
					if (temp instanceof Human)
					{
						Human human = (Human) temp;

						if (human.kissed != true)
						{
							closest = i;
						}
					}
				}
			}

			a = prey.get(closest).x;
			b = prey.get(closest).y;
			
			if(x - a > 0 && y - b > 0)
			{
				direct[0] = -1;
				direct[1] = -1;
			}
			else if(x - a == 0 && y - b > 0)
			{
				direct[0] = 0;
				direct[1] = -1;
			}
			else if(x - a < 0 && y - b > 0)
			{
				direct[0] = 1;
				direct[1] = -1;
			}
			else if(x - a > 0 && y - b == 0)
			{
				direct[0] = -1;
				direct[1] = 0;
			}
			else if(x - a == 0 && y - b == 0)
			{
				direct[0] = 0;
				direct[1] = 0;
			}
			else if(x - a < 0 && y - b == 0)
			{
				direct[0] = 1;
				direct[1] = 0;
			}
			else if(x - a > 0 && y - b < 0)
			{
				direct[0] = -1;
				direct[1] = 1;
			}
			else if(x - a == 0 && y - b < 0)
			{
				direct[0] = 0;
				direct[1] = 1;
			}
			else if(x - a < 0 && y - b < 0)
			{
				direct[0] = 1;
				direct[1] = 1;
			}

			translate(direct[0], direct[1]);

			return direct;
		}
		else
		{		
			direct[0] = random(-1, 1);
			direct[1] = random(-1, 1);

			translate(direct[0], direct[1]);	
		}
		return direct;
	}

	/** Generates a random movement.
	 * 
	 * @param min		the lower limit of the movement distance
	 * @param max		the upper limit of the movement distance
	 * @return			the random integer
	 */
	public int random (int min, int max)
	{
		double step1 = (Math.max(min, max) - Math.min(min, max));
		double step2 = (Math.random() * step1);
		double step3 = Math.min(min, max) + step2;

		return (int) Math.round(step3);
	}

	/** Displays the witch by drawing it onto a Graphics 
	 * object.
	 */
	public void draw (Graphics2D g)
	{
		g.setColor(new Color(0, 0, 64));
		g.fill(getShape());
	}
	
	/** Represents a witch's death by turning it into a 
	 * Grief Seed.
	 */
	public void die()
	{
		if (grid != null)
		{
			grid.markRemoval(this, 2); 
			grid.addEntity(new GriefSeed(x, y)); // transform into grief seed
			grid.requestRemoval(this); // remove
		}
	}
}
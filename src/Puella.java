import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/** The class representing a Puella - a magical girl.
 * 
 * @author Tony Cui
 * @author Sally Hui
 * @author Jiayin Huang
 */
public class Puella extends Entity
{
	private static final long serialVersionUID = 1L;

	/** The maximum grief a Puella can sustain before turning into a Witch.
	 */
	public final int MAXGRIEF = 3000;

	/** The current grief a Puella holds.
	 */
	private int grief;

	static final Shape shape = new Rectangle2D.Double(0, 0, 6, 6);

	/** Constructs a new Puella at a location.
	 * @param x		the x-coordinate of the Puella
	 * @param y		the y-coordinate of the Puella
	 */
	public Puella(double x, double y)
	{
		super(x, y, shape); // constructor from Entity class

		grief = 0; // initialize grief to 0
	}

	/** Constructs a new Puella from a human (presumably
	 * transforming).
	 * @param h		the human to be transformed
	 */
	public Puella(Human h)
	{
		super(h.x, h.y, shape); // constructor from Entity class

		grief = 0;
	}


	@Override
	public void advance()
	{
		grief++;

		super.advance();


		move(grid.getEntities(Witch.class));

		if(grief == MAXGRIEF)
			if (grid != null)
				transform();
	}

	@Override
	public void resolveCollision (List<Entity> colliding, double dx, double dy, double dTheta)
	{
		super.resolveCollision(colliding, dx, dy, dTheta);

		for (Entity e : colliding)
		{
			if (e instanceof Witch)
			{
				double num = Math.random();

				if(num < 0.333)
				{
					kill(this);
				}
				else
				{
					kill(e);
					grief = 0;
				}
			}
			else if (e instanceof GriefSeed)
			{
				x += dx;
				y += dy;
			}
		}
	}

	/** Allows a Puella to kill an entity.
	 * @param target	the entity the Puella is killing	
	 */
	private void kill (Entity target)
	{
		grid.markRemoval(target, 4);
		target.die();
	}

	/** Generates the next movement of a Puella, based on
	 * the location of witches.
	 * @param witches			the witches around it 
	 * @return		the point in array form of the next movement
	 */
	public int[] move (ArrayList<Entity> witches)
	{
		int[] direct = new int[2];

		if(witches.size() != 0)
		{			
			double[] distance = new double[witches.size()];
			int closest = 0;
			double a, b;

			for(int i = 0; i < witches.size(); i ++)
			{
				distance[i] = Math.sqrt((x - witches.get(i).x) * (x - witches.get(i).x) + (y - witches.get(i).y) * (y - witches.get(i).y));
			}

			for(int i = 1; i < distance.length; i++)
			{
				if(distance[i] < distance[closest])
				{
					closest = i;
				}
			}

			a = witches.get(closest).x;
			b = witches.get(closest).y;

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
		}
		else
		{						
			direct[0] = random(-1, 1);
			direct[1] = random(-1, 1);

			translate(direct[0], direct[1]);			
		}

		return direct;
	}

	/** Generates a random integer between a given range.
	 * @param min		the lower limit of the range
	 * @param max		the upper limit of the range
	 * @return			the random integer
	 */
	public int random (int min, int max)
	{
		double step1 = (Math.max(min, max) - Math.min(min, max));
		double step2 = (Math.random() * step1);
		double step3 = Math.min(min, max) + step2;

		return (int) Math.round(step3);
	}

	/** The transformation of a Puella into its Witch form.
	 */
	public void transform()
	{		
		grid.addEntity(new Witch (x, y)); // create a new witch in its place
		grid.addEntity(new Explosion (x, y, 400, 400, 100), Spacetime.FOLIAGE); // explosion
		grid.startShake(30); // shake the screen
		grid.removeEntity(this); // remove the puella
	}

	/** Displays a Puella by drawing it onto a graphics object.
	 */
	public void draw (Graphics2D g)
	{
		g.setColor(new Color(255, 0, 128)); // set color
		g.fill(getShape());
	}

	/** Getter method for grief.
	 * @return		the current grief of the puella
	 */
	public int getGrief ()
	{
		return grief;
	}
}
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/** A class representing Kyubey, or the Incubator. It contracts
 * female Humans into Magical Girls (Puella Magi).
 * 
 * @author Tony Cui
 * @author Jiayin Huang 
 * @author Sally Hui
 */
public class Incubator extends Entity 
{
	private static final long serialVersionUID = 1L;
	
	/** The statistics object used to gather information for the incubator.
	 */
	private Statistics data;	
	
	/** The radius of the incubator's respawn area.
	 */
	public int respawnX = 1200;
	public int respawnY = 1200;

	/** The number of grief seeds the incubator has consumed.
	 */
	public int griefSeeds;
	
	/** Constructs a new incubator at a location on a Spacetime object.
	 * @param grid		the spacetime the incubator is located in
	 * @param x			the x-coordinate of the incubator
	 * @param y			the y-coordinate of the incubator
	 */
	public Incubator(Spacetime grid, double x, double y) 
	{
		super(x, y, new Ellipse2D.Double (x, y, 5, 5));		
		grid.incubator = this;
		data = new Statistics(grid);
		
		griefSeeds = 0;
	}
	
	@Override
	public void advance ()
	{
		super.advance();
		
		if(grid != null && grid.incubator != null)
			move(grid.getEntities(Human.class), grid.getEntities(GriefSeed.class));
	}

	@Override
	public void draw(Graphics2D g) 
	{	
		Shape shape = getShape();
		g.setColor(Color.white);
		g.fill(shape);
		g.setColor(Color.black);
		g.draw(shape);
	}	
	
	@Override
	public void resolveCollision (List<Entity> colliding, double dx, double dy, double dTheta)
	{
		super.resolveCollision(colliding, dx, dy, dTheta);
		
		for (Entity e : colliding)
		{
			if (e instanceof Human)
			{
				if(Math.random() < 0.2 && !((Human) e).gender && !((Human) e).contractor)
				{
					grid.requestRemoval(e);
					
					grid.addEntity (new Puella((Human) e));
				}
				else
					((Human) e).refuseContract();
			}
			
			if (e instanceof Witch)
			{
				grid.removeEntity(this);				
			}
			
			if (e instanceof GriefSeed)
			{
				grid.requestRemoval(e);
				griefSeeds++;
			}
		}
	}
	
	/** Respawns the incubator, as it has infinite lives.
	 * @param requester			the spacetime to be respawned in
	 */
	public void respawn (Spacetime requester)
	{
		x += respawnX * Math.random() - (respawnX / 2);
		y += respawnY * Math.random() - (respawnY / 2);
		requester.addEntity (this);
		requester.ensureNoCollision(this);
		requester.markAddition(this, 18);		
	}
	
	/** Generates the next movement of the incubator based on the 
	 * nearby female humans and grief seeds.
	 * @param potential			the nearby female humans
	 * @param seeds				the nearby grief seeds
	 * @return		the point in array form of the next movement
	 */
	public int[] move (ArrayList<Entity> potential, ArrayList<Entity> seeds)
	{
		int[] direct = new int[2];
		
		data.update();
		if (data.puellaNum >= data.witchNum && seeds.size() != 0 || data.humanNum < data.witchNum)
			potential = seeds;

		if(potential.size() != 0 && (grid.getEntities(Witch.class).size() != 0 || potential.equals(seeds)))
		{
			double[] distance = new double[potential.size()];
			int closest = 0;
			double a, b;

			for(int i = 0; i < potential.size(); i ++)
			{
				distance[i] = Math.sqrt((x - potential.get(i).x) * (x - potential.get(i).x) + (y - potential.get(i).y) * (y - potential.get(i).y));
			}

			for(int i = 1; i < distance.length; i++)
			{
				Entity temp = potential.get(i);
				if(distance[i] < distance[closest])
				{
					if (temp instanceof Human)
					{
						Human human = (Human) temp;

						if (human.gender == false && human.contractor == false)
						{
							closest = i;
						}
					}
					else if (temp instanceof GriefSeed)
					{
						closest = i;
					}
				}
			}

			a = potential.get(closest).x;
			b = potential.get(closest).y;

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
	
	/** Generates a random number in a given range.
	 * @param min		the lower limit of the range
	 * @param max		the upper limit of the range
	 * @return			the random integer
	 */
	public int random (int min, int max)
	{
		double step1 = (Math.max(min, max) - Math.min(min, max));
		double step2 = (Math.random() * step1);
		double step3 = Math.min(min, max) + step2;

		//System.out.println ("Steps: " + step1 + " " + step2 + " " + step3);
		return (int) Math.round(step3);
	}
}

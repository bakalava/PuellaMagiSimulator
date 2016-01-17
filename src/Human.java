import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;

/** A class used to represent a human entity.
 * 
 * Handles interactions, transformation, movement and death of humans.
 * 
 * @author Tony Cui
 * @author Jiayin Huang 
 * @author Sally Hui
 */
public class Human extends Entity
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * The amount of time a Human has to live before dying or interacting with other entities.
	 * These parameters can be changed by interactions.
	 */
	int age, timeLeft, reproduce;
	final int MAXAGE = 3000;

	boolean gender, contractor;

	/**
	 * The human is a small 4x4 pixel square.
	 */
	static final Shape shape = new Rectangle2D.Double(0, 0, 4, 4);

	/** A boolean variable that represents whether or not the human 
	 * has been kissed by a witch.
	 */
	boolean kissed;

	/** Constructs a new Human at a specified point.
	 * 
	 * @param x			the x-coordinate of the human
	 * @param y			the y-coordinate of the human
	 */
	public Human(double x, double y)
	{
		super(x, y, shape); // accesses Entity constructor 
		age = 0;
		timeLeft = MAXAGE - age;
		kissed = false;

		// determine gender
		if(Math.random() < 0.225)
			gender = false;
		else
			gender = true;

		// contractors
		if(gender)
			contractor = true;

		// reproduction
		if(!gender)
			reproduce = 2;
	}

	/** Constructs a new Human based on its location, age, time left,
	 * and whether or not it was kissed.
	 * 
	 * @param x			the x-coordinate of the Human
	 * @param y			the y-coordinate of the Human
	 * @param a			the age of the human
	 * @param b			the time left for the human
	 * @param k			if the human was kissed
	 */
	public Human(double x, double y, int a, int b, boolean k)
	{
		super(x, y, shape); // access Entity constructor
		age = a; // set age
		timeLeft = b; // set time left
		kissed = k; // set kissed status

		// Generate gender
		if(Math.random() < 0.3)
			gender = false;
		else
			gender = true;

		// contractor
		if(gender)
			contractor = true;

		// reproduction
		if(!gender)
			reproduce = 2;
	}

	@Override
	public void advance ()
	{
		super.advance();
		move();

		age++;
		timeLeft--;

		if(age == MAXAGE || timeLeft == 0)
			die();
	}

	@Override
	public void resolveCollision (List<Entity> colliding, double dx, double dy, double dTheta)
	{
		super.resolveCollision(colliding, dx, dy, dTheta);

		for (Entity e : colliding)
		{
			if (e instanceof Human)
			{
				if(((Human) e).gender != gender && gender == false && reproduce >= 0)
				{
					int birthx, birthy;

					birthx = random((int)x - 20, (int)x + 20);
					birthy = random((int)y - 20, (int)y + 20);

					Human kid = new Human(birthx, birthy); 

					grid.addEntity (kid);
					grid.ensureNoCollision(kid);
					grid.markAddition(kid, 5);

					reproduce--;
				}
			}
		}
	}

	/** Generates the next movement of the human.
	 * 
	 * @return		the point in array form of the next movement
	 */
	public int[] move()
	{
		int[] direct = new int[2];

		direct[0] = random(-1, 1);
		direct[1] = random(-1, 1);

		translate(direct[0], direct[1]);

		return direct;
	}

	/** Generates a random number in a given range.
	 * 
	 * @param min		the lower limit of the range
	 * @param max		the upper limit of the range
	 * @return			the random integer
	 */
	public int random (int min, int max)
	{
		double step1 = (Math.max(min, max) - Math.min(min, max));
		double step2 = (Math.random() * step1);
		double step3 = Math.min(min, max) + step2;
		
		return (int) Math.round(step3); // the random number
	}

	/** Deals with a witch kiss.
	 */
	public void WitchKiss()
	{
		kissed = true;
		timeLeft *= 0.1;
	}

	/** Transforms the human into a Puella (magical girl).
	 */
	public void transform()
	{
		grid.addEntity(new Puella (x, y));
		grid.removeEntity(this); // remove old human
	}

	/** Deals with when the human refuses a contract
	 * with an incubator.
	 */
	public void refuseContract()
	{
		contractor = true;
	}

	/** Displays the human by drawing it onto a Graphics
	 * object.
	 */
	public void draw (Graphics2D g)
	{
		if(gender)
			g.setColor(new Color(128, 64, 0)); // male
		
		else if (!gender)
			g.setColor(Color.RED); // female
		
		g.fill(getShape());
	}
}

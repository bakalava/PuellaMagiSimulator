import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/** This is meant for debugging purposes only.
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class Ant extends Entity 
{	
	private static final long serialVersionUID = 1L;

	/** Constructs a new Ant at the specified coordinates.
	 * 
	 * @param x		the x-coordinate
	 * @param y		the y-coordinate
	 */
	public Ant(double x, double y) 
	{
		super(x, y, new Ellipse2D.Double (x, y, 20, 10));		
	}

	@Override
	public void draw(Graphics2D g) 
	{		
		g.setColor(Color.red);	
		super.draw(g);	
	}	
}

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/** The class representing a wall.
 * 
 * @author Jiayin Huang
 * @author Sally Hui
 * @author Tony Cui
 *
 */
public class Wall extends Entity
{		
	private static final long serialVersionUID = 1L;

	/** Constructs a new wall.
	 * @param x			the beginning x-coordinate of the new wall
	 * @param y			the beginning y-coordinate of the new wall
	 * @param width		the width of the wall
	 * @param height	the height of the wall
	 * @param theta		the angle of the wall
	 */
	public Wall(double x, double y, double width, double height, double theta) 
	{
		super(x, y, new Rectangle2D.Double (x, y, width, height));
		super.theta = theta;
	}		

	@Override
	public void draw(Graphics2D g) 
	{
		g.setColor(Color.black);
		super.draw (g);	
	}	
}

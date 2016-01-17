import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/** This Entity is used by the {@code markRemoval(Entity)} method
 * in Spacetime to mark the removal of an Entity with a fading
 * "explosion" graphic. Instances of Explosion should be added to the
 * FOLIAGE layer instead of the default ENTITIES layer to ensure
 * that the image appears on top of everything else.
 * 
 * @author Jiayin Huang
 * @author Sally Hui
 * @author Tony Cui 
 *
 */
public class Explosion extends Entity
{		
	private static final long serialVersionUID = 1L;	
	private int fadeInterval;
	private int timeLeft;
	public Color color;
	
	/** Creates a new Explosion.
	 * 
	 * @param x				x-coordinate of center
	 * @param y				y-coordinate of center
	 * @param width			width of blast
	 * @param height		height of blast
	 * @param fadeTicks		the number of ticks that will elapse before this explosion fades away completely
	 */
	public Explosion (double x, double y, double width, double height, int fadeTicks) 
	{
		this (x, y, width, height, fadeTicks, Color.white);
	}
	
	/** Creates a new Explosion.
	 * 
	 * @param x				x-coordinate of center
	 * @param y				y-coordinate of center
	 * @param width			width of blast
	 * @param height		height of blast
	 * @param fadeTicks		the number of ticks that will elapse before this explosion fades away completely
	 * @param color			the color of this explosion
	 */	
	public Explosion (double x, double y, double width, double height, int fadeTicks, Color color) 
	{
		super(x, y, new Ellipse2D.Double(x, y, width, height));
		fadeInterval = fadeTicks;
		timeLeft = fadeTicks;
		this.color = color;		
	}
	
	@Override
	public void advance ()
	{
		timeLeft--;
		
		if (timeLeft < 0)
			if (grid != null)
				grid.removeEntity(this, Spacetime.FOLIAGE);
	}
	
	@Override
	public void draw (Graphics2D g)
	{		
		int transparency = (int) (255 * (double)timeLeft / fadeInterval);
		g.setColor (new Color (color.getRed(), color.getGreen(), color.getBlue(), transparency));
		g.fill (getShape());
		g.setColor (Color.black);
	}
}

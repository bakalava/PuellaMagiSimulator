import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

/** A class representing the Grief Seed dropped by witches
 * when they die.
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class GriefSeed extends Entity {	
	
	private static final long serialVersionUID = 1L;
	private static final int colorNumber = 10;	
	public static final Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
	
	private ArrayList<Color> colors;
	private int colorIndex = 0;	
	
	/** Sets a new Grief Seed at the specified coordinates.
	 * 
	 * @param x		the x-coordinate
	 * @param y		the y-coordinate
	 */
	public GriefSeed(double x, double y)
	{
		super(x, y, shape);
		initRandomColors();
	}
	
	public void advance()
	{
		super.advance();
		
		colorIndex++;
		if (colorIndex >= colors.size())
			colorIndex = 0;
	}
	
	public void draw (Graphics2D g)
	{
		g.setColor(colors.get(colorIndex));
		g.fill(getShape());
	}
	
	/** Initializes the array of randomized colors that are
	 * used in sequence as time passes to draw this Grief Seed. 
	 */
	public void initRandomColors ()
	{
		colors = new ArrayList<Color>(colorNumber);
		for (int i = 0; i < colorNumber; i++)
		{
			int red = (int)(80 * Math.random());
			int green = (int)(20 * Math.random());
			int blue = (int)(200 * Math.random() + 55);	
			colors.add(new Color(red, green, blue));
		}
	}
}

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class Gear extends Entity 
{		
	private static final long serialVersionUID = 1L;

	/** Creates a new gear.
	 * 
	 * @param x						the x-coordinate of the center
	 * @param y						the y-coordinate of the center
	 * @param radius				the radius of the gear
	 * @param spokeNumber			the number of spokes that this gear has
	 * @param spokeExtensionLength	how far each spoke extends from the main gear body
	 */
	public Gear(double x, double y, int radius, int spokeNumber, int spokeExtensionLength) 
	{
		super(x, y, getInitShape (radius, spokeNumber, spokeExtensionLength));			
	}		
		
	/** Initializes a gear shape by adding Area objects together. 
	 *
	 * @return an Area object that represents the shape of this gear
	 */
	public static Shape getInitShape (int radius, int spokes, int spokeExtensionLength)
	{			
		double angle = 2 * Math.PI / spokes;
		double spokeRadius = radius + spokeExtensionLength;
		double spokeHeight = radius * (angle / 2); // yay, radians are the same as arc length
		double arcWidth = 1.2/5.0 * spokeHeight;		
		
		Shape nthSpoke = new RoundRectangle2D.Double (0, -spokeHeight / 2, spokeRadius, spokeHeight, arcWidth, arcWidth);			
		AffineTransform at = AffineTransform.getRotateInstance(angle);
		Area gear = new Area ();
		
		for (int i = 0; i < spokes; i++)
		{
			gear.add(new Area (nthSpoke));
			nthSpoke = at.createTransformedShape(nthSpoke);
		}
		
		Shape gearBody = new Ellipse2D.Double (-radius, -radius, 2 * radius, 2 * radius);
		gear.add(new Area (gearBody));							
		
		return gear;
	}	
	
	@Override
	public void draw (Graphics2D g)
	{
		g.setColor (Color.black);
		super.draw (g);		
	}
}

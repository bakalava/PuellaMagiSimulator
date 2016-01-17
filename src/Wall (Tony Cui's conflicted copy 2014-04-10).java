import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class Wall extends Entity
{	
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

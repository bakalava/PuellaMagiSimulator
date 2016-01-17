import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class Ant extends Entity {

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

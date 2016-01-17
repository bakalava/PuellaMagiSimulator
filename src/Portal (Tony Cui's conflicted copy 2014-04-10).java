import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;

public class Portal extends Entity 
{
	private Portal target;		

	public Portal(double x, double y, int length) 
	{
		super(x, y, new Rectangle (1, length));
		setOffset (-length / 2, -1 / 2);
	}

	@Override
	public void draw (Graphics2D g) 
	{	
		g.setColor(Color.blue);
		super.draw(g);		
	}		

	@Override
	public void notifyCollision (Entity instigator)
	{
		if (target != null)
		{
			// Remove instigator from this grid and add it to the target grid
			instigator.grid.removeEntity(instigator);
			target.grid.addEntity(instigator);			

			// Rotate instigator about the portal center by the relative difference	
			double deltaTheta = target.theta - theta;		
			
			System.out.println (instigator.theta+ " " + theta);
			// Additional rotation adjustment based on Entity approach direction
			if (Math.abs(instigator.theta - theta) % (2 * Math.PI) > Math.PI / 4)
				deltaTheta += Math.PI;			

			Point2D.Double point = instigator.getCenterLocation();
			
			AffineTransform at = AffineTransform.getRotateInstance(deltaTheta, x, y);
			AffineTransform at2 = AffineTransform.getTranslateInstance(target.x - x, target.y - y);	
			//at.preConcatenate (at2);	
			at.transform(point, point);				
			at2.transform(point, point);			

			Vector2D entityVector = instigator.getVector();
			
			// Set instigator at new location
			
			instigator.setLocationByCenter (point.x, point.y);
			instigator.setAngle (instigator.theta + deltaTheta);
			entityVector.rotate(deltaTheta);

			// Nudge instigator so that it won't be in contact with the portal when it appears						
						
			Vector2D away = Vector2D.getVectorFromDirection(1, target.theta);	
			int counter = 0;			
			
			while (instigator.grid.isColliding(instigator.getShape(), target.getShape()))
			{
				counter++;
				System.out.println (counter);
				instigator.setLocation (instigator.x + away.x, instigator.y + away.y);
			}
			
			// TODO: better portals
		}
	}

	public void setTarget (Portal portal)
	{
		target = portal;		
	}

}

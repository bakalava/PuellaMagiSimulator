import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/** A class representing an energy beam, like in all the manga and anime.
 * 
 * @author Jiayin Huang
 * @author Sally Hui
 * @author Tony Cui 
 */
public class EnergyBeam extends Entity // Ringo Mogire Beam!
{
	private static final long serialVersionUID = 1L;
	
	/** The number of randomized colors that this beam should cycle through.
	 */
	private static final int colorNumber = 10;
	
	/** The list of random colors. 
	 */
	private List<Color> colors;
	
	/** The index of the current color in the list of random colors. 
	 */
	private int colorIndex = 0;
	
	/** The entity that shot the energy beam; prevents the entity from hitting itself.
	 */
	private Entity shooter;
	
	/** The dimensions and the power of this energy beam.
	 */
	private double size;
	
	/** Constructs a new energy beam using the entity that fired the beam, 
	 * angle, speed, and size. 
	 * 
	 * @param shooter			the originator of the energy beam
	 * @param theta				the angle of the energy beam
	 * @param speed				how fast the energy beam is
	 * @param size				how powerful the energy beam is
	 */
	public EnergyBeam(Entity shooter, double theta, double speed, double size)
	{
		this (shooter, Vector2D.getVectorFromDirection(speed, theta), size);
	}
	
	/** Constructs a new energy beam using the entity that fired the beam, velocity 
	 * vector, and size.
	 * @param shooter			the origin of the energy beam
	 * @param velocity			the speed and direction of the energy beam
	 * @param size				how powerful the energy beam is
	 */
	public EnergyBeam(Entity shooter, Vector2D velocity, double size) 
	{
		super(shooter.getCenter().x, shooter.getCenter().y, new Ellipse2D.Double(0, 0, 2 * size, 2 * size));		
		this.velocity = velocity;
		this.shooter = shooter;
		this.size = size;
		setOffset (-size, -size);		
		friction = 0;
		generateRandomColors();
	}	
	
	@Override
	public void advance()
	{
		super.advance();
		if (grid.getTime() % 30 == 0)
			if (!grid.isInBounds(this))
				detonate(null);
		
		colorIndex++;
		if (colorIndex >= colors.size())
			colorIndex = 0;
	}
	
	@Override
	public void notifyCollision (Entity instigator)
	{
		if (!instigator.equals(shooter))
			if (!(instigator instanceof EnergyBeam))
				detonate (instigator);			
	}
	
	@Override
	public void resolveCollision (List<Entity> colliding, double dx, double dy, double dTheta)
	{
		for (Entity e : colliding)
			if (!e.equals(shooter))
				if(!(e instanceof EnergyBeam))
					detonate(e);		
	}
	
	/** Detonates (destroys) an entity.
	 * 
	 * @param instigator		the entity to be destroyed
	 */
	public void detonate (Entity instigator)
	{
		Point2D.Double center = getCenter();
		grid.addEntity(new Explosion(center.x, center.y, size * 4, size * 4, 100, Color.pink), Spacetime.FOLIAGE);
		grid.requestRemoval(this);		
	}
	
	@Override
	public void draw (Graphics2D g)
	{
		Color temp = colors.get(colorIndex);		
		
		g.setColor(temp);
		g.fill(new Ellipse2D.Double (x - size, y - size, 2 * size, 2 * size));
		
		g.setColor(Color.white);	
		g.fill(new Ellipse2D.Double(x - size/2, y - size/2, size, size));	
	}	
	
	/** Generates a list of random colours by systematically randomizing 
	 * R, G, and B values.
	 */
	private void generateRandomColors()
	{
		colors = new ArrayList<Color>(colorNumber);
		for (int i = 0; i < colorNumber; i++)
		{
			int red = (int)(200 * Math.random() + 55);
			int green = (int)(200 * Math.random() + 55);
			int blue = (int)(200 * Math.random() + 55);	
			colors.add(new Color(red, green, blue));
		}
	}
}

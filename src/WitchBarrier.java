import java.io.Serializable;

/** A currently unimplemented class that makes use of a Spacetime object
 * and two Portals to represent a witch labyrinth.
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class WitchBarrier implements Serializable
{	
	private static final long serialVersionUID = 1L;
	
	/** The Spacetime object representing the normal outside world. 
	 */
	protected Spacetime outside;
	
	/** The Spacetime object representing the witch's labyrinth. This should
	 * be separate from the normal outside world. 
	 */
	protected Spacetime labyrinth;
	
	/** The Portal Entity that leads into the witch's labyrinth. 
	 */
	protected Portal entrance;
	
	/** The Portal Entity that leads out of the witch's labyrinth. 
	 */
	protected Portal exit;

	/** Dimensions are in units of TerrainTiles.
	 * 
	 * @param outside				the Spacetime object representing the outside world	 *
	 * @param width					the width of the witch's labyrinth
	 * @param height				the height of the witch's labyrinth
	 */
	public WitchBarrier(Spacetime outside, int width, int height) 
	{
		this.outside = outside;		
		
		WorldGenerator labyrinthGenerator = new WorldGenerator ();		
		labyrinthGenerator.setChunk(16);
		//labyrinthGenerator.isWitchBarrier = true;
		labyrinthGenerator.setWidth(width);
		labyrinthGenerator.setHeight(height);
		
		labyrinth = labyrinthGenerator.generate();
		
		entrance = new Portal (0, 0, 50);
		exit = new Portal (0, 0, 50);		
		entrance.setTarget(exit);
		exit.setTarget(entrance);
	}
	
	/** Sets the location of the entrance portal that leads into this WitchBarrier. 
	 * This portal should be set in the normal outside world.  
	 *
	 * @param x			the desired x-coordinate of the entrance portal
	 * @param y			the desired y-coordinate of the entrance portal
	 * @param theta		the desired angle of the entrance portal
	 */
	public void setEntrance (double x, double y, double theta)
	{
		outside.addEntity(entrance);
		entrance.setAngle(theta);
		entrance.setLocation(x, y);		
	}
	
	/** Sets the location of the exit portal that leads out to the outside world.
	 * This portal is set inside of the witch labyrinth.
	 * 
	 * @param x			the desired x-coordinate of the exit portal
	 * @param y			the desired y-coordinate of the exit portal
	 * @param theta		the desired angle of the exit portal
	 */
	public void setExit (double x, double y, double theta)
	{
		labyrinth.addEntity(exit);
		exit.setAngle (theta);
		exit.setLocation(x, y);		
	}
}

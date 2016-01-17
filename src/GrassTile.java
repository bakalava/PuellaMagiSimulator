import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

/** A rectangular Entity representing a grass tile.
 * Currently used for debugging purposes, hence the 
 * developer texture with the x and y coordinates.
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class GrassTile extends TerrainTile 
{
	private static final long serialVersionUID = 1L;

	/** Creates a new GrassTile.
	 * 
	 * @param x			the x coordinate of the GrassTile
	 * @param y			the y coordinate of the GrassTile
	 * @param width		the width of the GrassTile
	 * @param height	the height of the GrassTile
	 */
	public GrassTile(int x, int y, int width, int height) 
	{
		super(x, y, width, height);			
	}	

	/** Draws the GrassTile shape, as well as printing the
	 * GrassTile's x and y coordinates. 
	 */
	@Override
	public void draw(Graphics2D g) 
	{
		Shape grass = getShape ();
		g.setColor(new Color (150, 255, 0));
		g.fill (grass);
		g.setColor (new Color (100, 192, 0));
		g.draw (grass);
		g.drawString((int)x + "," + (int)y, (int) x + 2, (int) y + 20);
	}	
}

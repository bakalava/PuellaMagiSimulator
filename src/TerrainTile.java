import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

/** A class representing a tile that is mean to be added
 * to the Spacetime.TERRAIN layer of the Spacetime class.
 * These are shaped as simple integer rectangles in order
 * to save on processing power because of how numerous they are.
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class TerrainTile extends Entity 
{	
	private static final long serialVersionUID = 1L;
	
	public Color color = new Color (128, 128, 128);
	
	/** Creates a new sTile.
	 * 
	 * @param x			the x coordinate of the Tile
	 * @param y			the y coordinate of the Tile
	 * @param width		the width of the Tile
	 * @param height	the height of the Tile
	 */
	public TerrainTile(int x, int y, int width, int height) 
	{
		super(x, y, new Rectangle (x, y, width, height));
		setOffset(0, 0);
	}
	
	/** Draws the Tile shape.
	 * 
	 * @param g		the Graphics context in which to paint
	 */
	@Override
	public void draw(Graphics2D g) 
	{
		Shape tile = getShape ();
		g.setColor(color);
		g.fill (tile);		
	}
	
	/** Gets the shape of this Tile, which is a simple Rectangle. 
	 */
	@Override	
	public Shape getShape() 
	{
		Rectangle tile = (Rectangle)shape;
		tile.x = (int) x;
		tile.y = (int) y;
		return tile;
	}	
}

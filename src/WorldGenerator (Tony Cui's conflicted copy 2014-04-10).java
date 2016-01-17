import java.awt.Dimension;
import java.awt.Rectangle;

public class WorldGenerator 
{
	/** The size of the TERRAIN grass tiles. 
	 */
	public int chunk = 128;

	public void generate (Spacetime grid)
	{
		Dimension size = grid.getSize();
		for (int x = 0; x < size.width; x++) // TODO: terrain generation method
		{
			for (int y = 0; y < size.height; y++)
			{
				GrassTile temp = new GrassTile (x * chunk, y * chunk, chunk, chunk);
				grid.addEntity (temp, Spacetime.TERRAIN);				
			}
		}

		// Test test testing testers etc

		Portal portal = new Portal (30, 50, 20);	
		portal.theta = 0;		
		Portal portal2 = new Portal (90, 300, 20);
		portal2.theta = 9 * Math.PI / 16;
		portal.setTarget(portal2);
		portal2.setTarget(portal);

		grid.addEntity (portal);
		grid.addEntity (portal2);
		grid.addEntity (new Gear (200, 200, 400, 20, 25));
	}

}

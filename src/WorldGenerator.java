import java.util.Random;

/** A class used to generate a new Spacetime object.
 * Prior to generation, field variables can be altered
 * to customize the initial conditions.
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class WorldGenerator 
{
	/** The size of the TERRAIN tiles. 
	 */
	private int chunk = 128;
	private int width = 50;
	private int height = 50;	
	
	/** If the generated Spacetime has a Walpurgisnacht
	 */
	private boolean hasWalpurgisnacht = false;
	
	/** If the generated Spacetime has an Incubator
	 */
	private boolean hasIncubator = true;
	
	/** Population numbers of humans, puella, and witches
	 * in the new Spacetime object.
	 */
	private int humanPopulation = 200;
	private int puellaPopulation = 7;
	private int witchPopulation = 20;
	
	/** The minimum and maximum age range for a human.
	 */
	private int humanMinAge = 0;
	private int humanMaxAge = 1000;
	
	/** The area a human can be spawned into's percentage
	 * of the entire Spacetime object.
	 */
	private double humanSpawnMinX = 0.078125; //% of total width
	private double humanSpawnMaxX = 0.234375; //% of total height
	private double humanSpawnMinY = 0.078125;	
	private double humanSpawnMaxY = 0.234375;
	
	/** The area a Puella can be spawned into's percentage
	 * of the entire Spacetime object.
	 */
	private double puellaSpawnMinX = 0.078125;
	private double puellaSpawnMaxX = 0.234375;
	private double puellaSpawnMinY = 0.078125;
	private double puellaSpawnMaxY = 0.234375;
	
	/** The area a witch can be spawned into's percentage
	 * of the entire Spacetime object.
	 */
	private double witchSpawnMinX = 0.078125;
	private double witchSpawnMaxX = 0.234375;
	private double witchSpawnMinY = 0.078125;		
	private double witchSpawnMaxY = 0.234375;
	
	/** The random number generator used for generating random events. 
	 */
	public Random randomNumberGenerator; 

	/** Constructs a new WorldGenerator object using
	 * a random number.
	 */
	public WorldGenerator ()
	{
		this (new Random().nextLong());
	}

	/** Constructs a new WorldGenerator object using a seed.
	 * @param seed		the random number used to generate more random numbers
	 */
	public WorldGenerator (long seed)
	{
		randomNumberGenerator = new Random (seed);
	}
	
	/** Generates a new Spacetime object.
	 * 
	 * @return		the generated Spacetime
	 */
	public Spacetime generate ()
	{
		return new Spacetime (this);		
	}

	/** Populates the specified Spacetime object.
	 * 
	 * @param grid		the specified Spacetime
	 */
	public void generate (Spacetime grid)
	{				
		// generate entities and terrain
		generateWalls (grid);
		generateTiles (grid);
		generateHumans (grid);
		generatePuella (grid);
		generateWitches (grid);
		
		if (hasWalpurgisnacht) // user enabled walpurgisnacht
		{
			Walpurgisnacht wal = new Walpurgisnacht (width * chunk / 3, height * chunk / 3);
			grid.addEntity (wal);
			grid.ensureNoCollision (wal);
		}
		
		if (hasIncubator)
		{
			Incubator kyu = new Incubator(grid, 0.3 * width * chunk, 0.3 * height * chunk);
			grid.addEntity(kyu);
			grid.ensureNoCollision(kyu);
		}
	}
	
	/** Generates the grass tiles of the new Spacetime grid.
	 * @param grid			the Spacetime object created 
	 */
	private void generateTiles (Spacetime grid)
	{
		for (int x = 0; x < width; x++) 
		{
			for (int y = 0; y < height; y++)
			{
				TerrainTile tile = new GrassTile (x * chunk, y * chunk, chunk, chunk);				
				grid.addEntity (tile, Spacetime.TERRAIN);
			}				
		}
	}
	
	/** Generates humans within the specified area
	 * 
	 * @param grid		the Spacetime object to be populated with humans
	 */
	private void generateHumans (Spacetime grid)	
	{
		// calculate area
		double xRange = width * chunk * Math.abs(humanSpawnMaxX - humanSpawnMinX);
		double yRange = height * chunk * Math.abs(humanSpawnMaxY - humanSpawnMinY);
		double xOffset = humanSpawnMinX * width * chunk;
		double yOffset = humanSpawnMinY * width * chunk;
		
		// calculate age allowance for humans
		int ageRange = humanMaxAge - humanMinAge;
		
		for (int i = 0; i < humanPopulation; i++)
		{
			double x = xRange * Math.random() + xOffset;
			double y = yRange * Math.random() + yOffset;
			int age = (int)(ageRange * Math.random() + humanMinAge);
			Human human = new Human (x, y);
			human.age = age;
			grid.addEntity(human);
			grid.ensureNoCollision(human);
		}	
	}
	
	/** Generates magical girls within the specified area.
	 * 
	 * @param grid		the Spacetime object to be populated with puella
	 */
	private void generatePuella (Spacetime grid)
	{
		// calculate the area to be populated
		double xRange = width * chunk * Math.abs(puellaSpawnMaxX - puellaSpawnMinX);
		double yRange = height * chunk * Math.abs(puellaSpawnMaxY - puellaSpawnMinY);
		double xOffset = puellaSpawnMinX * width * chunk;
		double yOffset = puellaSpawnMinY * width * chunk;		
		
		for(int i = 0; i < puellaPopulation; i++)
		{
			double x = xRange * Math.random() + xOffset;
			double y = yRange * Math.random() + yOffset;
			Puella hero = new Puella (x, y);
			grid.addEntity(hero);
			grid.ensureNoCollision(hero);
		}
	}
	
	/** Generates witches within the specified area.
	 * 
	 * @param grid		the Spacetime object to be populated with witches
	 */
	private void generateWitches (Spacetime grid)
	{
		// calculate the area to be populated
		double xRange = width * chunk * Math.abs(witchSpawnMaxX - witchSpawnMinX);
		double yRange = height * chunk * Math.abs(witchSpawnMaxY - witchSpawnMinY);
		double xOffset = witchSpawnMinX * width * chunk;
		double yOffset = witchSpawnMinY * width * chunk;		
		
		for(int i = 0; i < witchPopulation; i++)
		{
			double x = xRange * Math.random() + xOffset;
			double y = yRange * Math.random() + yOffset;
			Witch witch = new Witch (x, y);
			grid.addEntity(witch);
			grid.ensureNoCollision(witch);
		}
	}

	/** Generate walls around the entire Spacetime grid.
	 * 
	 * @param grid		the Spacetime object to be walled in
	 */
	private void generateWalls (Spacetime grid)
	{
		// Boundaries
		Wall top = new Wall (1, 1, chunk * width - 2, 1, 0);
		Wall bottom = new Wall (1, chunk * height - 2, chunk * width - 2, 1, 0);
		Wall left = new Wall (1, 1, 1, chunk * height - 3, 0);
		Wall right = new Wall (chunk * width - 2, 1, 1, chunk * height - 3, 0);

		// add walls
		grid.addEntity(top);
		grid.addEntity(bottom);
		grid.addEntity(left);
		grid.addEntity(right);			
	}	
	
	/** Setter method for chunk
	 * @param newChunk			the new chunk value
	 */
	public void setChunk (int newChunk)
	{
		chunk = newChunk;
	}
	
	/** Seter method for width
	 * 
	 * @param newWidth		the new width value
	 */
	public void setWidth (int newWidth)
	{
		width = newWidth;
	}
	
	/** Setter method for height
	 * 
	 * @param newHeight		the new height value
	 */
	public void setHeight (int newHeight)
	{
		height = newHeight;
	}	
	
	/** Setter method for human spawn minimum width
	 * 
	 * @param newX		the new width percent value
	 */
	public void setHumanSpawnMinX (double newX)
	{
		humanSpawnMinX = newX;
	}
	
	/** Setter method for human spawn minimum height
	 * 
	 * @param newY		the new height percent value
	 */
	public void setHumanSpawnMinY (double newY)
	{
		humanSpawnMinY = newY;
	}
	
	/** Setter method for human spawn maximum width
	 * 
	 * @param newX		the new width percent value
	 */
	public void setHumanSpawnMaxX (double newX)
	{
		humanSpawnMaxX = newX;
	}
	
	/** Setter method for human spawn maximum height
	 * 
	 * @param newY 		the new height percent value
	 */
	public void setHumanSpawnMaxY (double newY)
	{
		humanSpawnMaxY = newY;
	}
	
	/** Setter method for witch spawn minimum width
	 * 
	 * @param newX		the new width percent value
	 */
	public void setWitchSpawnMinX (double newX)
	{
		witchSpawnMinX = newX;
	}
	
	/** Setter method for witch spawn minimum height
	 * 
	 * @param newY		the new height percent value
	 */
	public void setWitchSpawnMinY (double newY)
	{
		witchSpawnMinY = newY;
	}
	
	/** Setter method for witch spawn minimum width
	 * 
	 * @param newX		the new width percent value
	 */
	public void setWitchSpawnMaxX (double newX)
	{
		witchSpawnMaxX = newX;
	}
	
	/** Setter method for witch spawn maximum height
	 * 
	 * @param newY		the new height percent value
	 */
	public void setWitchSpawnMaxY (double newY)
	{
		witchSpawnMaxY = newY;
	}
	
	/** Setter method for puella spawn minimum width
	 * 
	 * @param newX		the new width percent value
	 */
	public void setPuellaSpawnMinX (double newX)
	{
		puellaSpawnMinX = newX;
	}
	
	/** Setter method for puella spawn minimum height
	 * 
	 * @param newY		the new height percent value
	 */
	public void setPuellaSpawnMinY (double newY)
	{
		puellaSpawnMinY = newY;
	}
	/** Setter method for puella spawn minimum width
	 * 
	 * @param newX		the new width percent value
	 */
	public void setPuellaSpawnMaxX (double newX)
	{
		puellaSpawnMaxX = newX;
	}
	
	/** Setter method for witch spawn maximum height
	 * 
	 * @param newY		the new height percent value
	 */
	public void setPuellaSpawnMaxY (double newY)
	{
		puellaSpawnMaxY = newY;
	}
	
	/** Setter method for human population
	 * 
	 * @param newPop		the new human population value
	 */
	public void setHumanPopulation (int newPop)
	{
		humanPopulation = newPop;
	}
	
	/**	Setter method for existence of Walpurgisnacht
	 * 
	 * @param hasWP		if there is a Walpurgisnacht
	 */
	public void setHasWalpurgisnacht (boolean hasWP)
	{
		hasWalpurgisnacht = hasWP;
	}
	
	/** Setter method for witch population
	 * 
	 * @param newPop		new witch population value
	 */
	public void setWitchPopulation (int newPop)
	{
		witchPopulation = newPop;
	}
	
	/**	Setter method for puella population
	 * 
	 * @param newPop		new puella population value
	 */
	public void setPuellaPopulation (int newPop)
	{
		puellaPopulation = newPop;
	}
	
	/**	Setter method for human minimum age
	 * 
	 * @param newAge		the new minimum age
	 */
	public void setHumanMinAge (int newAge)
	{
		humanMinAge = newAge;
	}
	
	/**	Setter method for human maximum age
	 * 
	 * @param newAge		the new maximum age
	 */
	public void setHumanMaxAge (int newAge)
	{
		humanMaxAge = newAge;
	}
	
	/** Getter method for chunk
	 * 
	 * @return		the chunk size
	 */
	public int getChunk ()
	{
		return chunk;
	}
	
	/** Getter method for height
	 * 
	 * @return		height
	 */
	public int getHeight ()
	{
		return height;
	}
	
	/**	Getter method for width
	 * 
	 * @return		width
	 */
	public int getWidth()
	{
		return width;
	}
}

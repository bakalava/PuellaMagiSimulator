import java.io.Serializable;
import java.util.List;


/** A class for gathering data from a Spacetime object.
 * 
 * @author Jiayin Huang
 * @author Tony Cui
 * @author Sally Hui
 */
public class Statistics implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/** the Spacetime object being analysed
	 */
	private Spacetime grid;
	
	/** The number of each type of entity living in the 
	 * Spacetime object.
	 */
	protected int livingNum, 
					humanNum,
					maleNum,
					femNum,
					puellaNum,
					witchNum;
	
	/** Constructs a new Statistics object.
	 * @param grid			the Spacetime object being analysed
	 */
	public Statistics (Spacetime grid)
	{
		this.grid = grid;
	}
	
	/** Update the statistics object with new data from the 
	 * Spacetime object.
	 */
	public void update()
	{			
		// initialize to 0
		humanNum = 0;
		puellaNum = 0;
		witchNum = 0;
		maleNum = 0;
		femNum = 0;
		
		List<Entity> all = grid.getEntities(Entity.class);
		
		for (Entity e : all) // goes through the Spacetime object's entities
		{
			if (e instanceof Human) // human information
			{
				Human human = (Human)e;
				if (human.gender == true)
					maleNum++;
				else
					femNum++;
				humanNum++;
			}
			else if (e instanceof Puella) // puella information
				puellaNum++;
			else if (e instanceof Witch) // witch information
				witchNum++;			
		}
		
		livingNum = humanNum + puellaNum + witchNum;		
	}
	
	/** Creates a text representation of the gathered statistics.
	 */
	public String toString()
	{
		String str = grid.toString() + "\n";
		str += "t = " + grid.getTime() + "\n";
		str += "Total Population: " + livingNum + "\n";
		str += "Humans: " + humanNum + "\n";
		str += "      Male: " + maleNum + "\n";
		str += "      Female: " + femNum + "\n";
		str += "Puella Magi: " + puellaNum + "\n";
		str += "Witches: " + witchNum + "\n";
		return str;
	}

}

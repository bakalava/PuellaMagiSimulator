import java.io.Serializable;

/** The class used to produce statistics in a text 
 * representation for an entity. <br> This class is 
 * used for the Statistics glass panel tool of the
 * Simulation Screen class.
 * 
 * @author Jiayin Huang
 * @author Sally Hui
 * @author Tony Cui
 */
public class EntityStalker implements Serializable
{       
	private static final long serialVersionUID = 1L;
	
	/** The entity to be analysed.
	 */
	private Entity target;	

	/** Constructs an EntityStalker object for the specified entity.
	 * 
	 * @param e             the entity to be analysed
	 */
	public EntityStalker (Entity e) 
	{
		target = e;             
	}       
	
	/** Creates a text representation of the entity's 
	 * statistics. 
	 */
	public String toString()
	{
		String str = target.toString() + ":\n";
		
		// position and velocity statistics (applicable to all entities)
		if(target.grid == null)
			str += "GONE\n";
		str += "x: " + target.x + "\n";
		str += "y: " + target.y + "\n";
		str += "\u03B8: " + target.theta + "\n";
		str += "dx: " + target.velocity.x + "\n";
		str += "dy: " + target.velocity.y + "\n";
		str += "d\u03B8:" + target.angularVelocity + "\n";
		
		// entity is a human
		if (target instanceof Human)
		{
			Human human = (Human)target;
			str += "sex: " + (human.gender ? "F" : "M") + "\n";
			str += "age: " + human.age + "\n";
			str += "max age: " + human.MAXAGE + "\n";
			str += "kissed: " + human.kissed + "\n";
		}
		
		// entity is a magical girl
		else if (target instanceof Puella)
		{
			Puella puella = (Puella)target;
			str += "sex: F\n";
			str += "grief: " + puella.getGrief() + "\n";
			str += "max grief: " + puella.MAXGRIEF + "\n";
		}
		
		// entity is a witch
		else if (target instanceof Witch)
		{
			// no useful variables right now
		}
		
		// entity is a Walpurgisnacht
		else if (target instanceof Walpurgisnacht)
		{
			Walpurgisnacht gears = (Walpurgisnacht)target;
			str += "search radius: " + gears.getSearchRadius() + "\n";
			str += "kills: " + gears.getKillCount() + "\n";                 
		}
		
		// entity is an Incubator
		else if (target instanceof Incubator)
		{
			Incubator kyu = (Incubator)target;
			str += "grief seeds: " + kyu.griefSeeds;
		}                
		
		return str;
	}
}

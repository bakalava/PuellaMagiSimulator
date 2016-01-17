import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** A class that stores Entities in recursive quadrant branches.
 * This is to make retrieval and collision checking more efficient.
 * 
 * A QuadTree object contains one root QuadBranch and one list of outlier
 * Entities that are not within the bounds of the root QuadBranch. 
 * 
 * @see QuadBranch
 * 
 * @author Jiayin Huang
 * @author Sally Hui
 * @author Tony Cui 
 */
public class QuadTree implements Externalizable
{	
	private static final long serialVersionUID = 1L;	

	/** The path to this QuadTree is represented with {-2}. Specifically,
	 * that path refers to the list of outlier Entities that are stored at 
	 * this level.
	 */
	public QuadTreePath path = new QuadTreePath(false);

	/** The root QuadBranch for this QuadTree. 
	 */
	private QuadBranch root;

	/** The list of all outlier Entities that do not belong to the root QuadBranch.  
	 */
	private List<Entity> outliers;	

	/** The number of Entities in this QuadTree. 
	 */
	private int size = 0;	

	/** Creates an empty QuadTree implementation with no defined QuadBranches. 
	 */
	public QuadTree()
	{
		outliers = new ArrayList<Entity>();
	}

	/** Creates a new QuadTree with a root QuadBranch of the specified
	 * bounds.
	 * 
	 * @param minX		the smallest x-coordinate of the QuadTree.
	 * @param minY		the smallest y-coordinate of the QuadTree.
	 * @param maxX 		the largest x-coordinate of the QuadTree.
	 * @param maxY		the largest y-coordinate of the QuadTree.
	 */
	public QuadTree(double minX, double minY, double maxX, double maxY) 
	{
		outliers = new ArrayList<Entity>();
		root = new QuadBranch (null, QuadTreePath.ROOT, new Box(minX, minY, maxX, maxY));		
	}	

	/** Adds the specified Entity to this QuadTree. If the bounding rectangle
	 * of the Entity's shape is entirely contained by the root QuadBranch, 
	 * the task of adding the Entity is passed down to that QuadBranch. Otherwise,
	 * the Entity is added to the outliers list here in this QuadTree instance.
	 * 
	 * @param e		the Entity to be added
	 */
	public void add(Entity e)
	{		
		Box entityBounds = new Box(e.getShape().getBounds());

		// Add the Entity to the correct location
		if (root.contains(entityBounds))
		{
			root.add(e, entityBounds);			
		}
		else
		{			
			outliers.add(e);
			e.path = this.path;
		}
		size++;
	}

	/** Attempts to remove the specified Entity from
	 * the QuadTree. Returns false if the specified 
	 * Entity could not be found.
	 * 
	 * @param e		the Entity to be removed
	 * @return true if this QuadTree was changed as a result
	 * 		of this removal; false otherwise
	 */
	public boolean remove(Entity e)
	{
		boolean removed = root.remove(e); // attempts to remove Entity from root
		if (!removed)
			removed = outliers.remove(e); // attempts to remove Entity from outliers
		if (removed)
		{
			e.path = null;
			size--;
		}
		return removed;
	}	

	/** Clears this QuadTree and sets a new QuadBranch with the
	 * specified bounds.
	 * 
	 * @param minX		the smallest x-coordinate of the QuadTree.
	 * @param minY		the smallest y-coordinate of the QuadTree.
	 * @param maxX 		the largest x-coordinate of the QuadTree.
	 * @param maxY		the largest y-coordinate of the QuadTree.
	 */
	public void reset (double minX, double minY, double maxX, double maxY)
	{
		outliers.clear();
		if (root != null)
			root.clear();
		root = new QuadBranch (null, QuadTreePath.ROOT, new Box(minX, minY, maxX, maxY));
	}

	/** Returns an array containing all of the Entities in this QuadTree.
	 * 
	 * @return an array containing all of the Entities in this QuadTree
	 */
	public ArrayList<Entity> get()
	{
		HashSet<Entity> all = root.get();
		all.addAll(outliers);
		return new ArrayList<Entity> (all);
	}

	/** Returns an array containing all of the Entities in the deepest QuadBranch 
	 * that contains the specified point. This includes all of the Entities are entirely 
	 * within that QuadBranch, as well as all of the noncommittal Entities that are 
	 * only partially in that QuadBranch.
	 * 
	 * @param x		the x-coordinate of the point
	 * @param y		the y-coordinate of the point
	 * @return an array containing all of the Entities in the QuadBranch as
	 * 		the specified point
	 */
	public ArrayList<Entity> get(double x, double y)
	{
		HashSet<Entity> list;
		if (root.contains(x, y))
			list = root.get(x, y);
		else
			list = new HashSet<Entity>(outliers);
		return new ArrayList<Entity> (list);		
	}

	/** Returns an array containing all of the Entities in
	 * the same QuadBranch as the specified Entity, including itself.
	 * If the Entity is a noncommittal, i.e. it is in more than one QuadBranch, 
	 * this method retrieves all of the Entities and noncommittal Entities from
	 * all of the QuadBranches that contain a noncommittal copy of the specified
	 * Entity. This method ensures that there are no duplicate Entities during 
	 * collection by using a HashSet instead of an ArrayList.
	 * 
	 * @param e		the specified Entity
	 * @return an array containing all of the Entities in same QuadBranch or QuadBranches 
	 * 		as the bounding rectangle of the shape of the specified Entity
	 */
	public ArrayList<Entity> get(Entity e)
	{
		HashSet<Entity> list;	
		Box entityBounds = new Box (e.getShape().getBounds());
		QuadBranch branch = getBranch(e.path);		
		if (branch == null)
		{
			list = new HashSet<Entity>(outliers);
			list.addAll(root.get(entityBounds));			
		}
		else
		{			
			list = branch.get(entityBounds);	
		}					
		return new ArrayList<Entity> (list);
	}

	/** Returns an array containing all of the Entities in the deepest
	 * QuadBranch that contains the specified Rectangle. This includes all of
	 * the Entities are entirely within that QuadBranch, as well as all
	 * of the noncommittal Entities that are only partially in that QuadBranch.
	 * If the specified rectangle occupies more than one QuadBranch, this method 
	 * retrieves all of the Entities and noncommittal Entities from all of the 
	 * QuadBranches that intersect the specified Rectangle. 
	 * 
	 * @param bounds	the specified Rectangle
	 * @return an array containing all of the Entities that occupy the same QuadBranch
	 *		or QuadBranches	the specified Rectangle
	 */
	public ArrayList<Entity> get(Rectangle2D bounds)
	{
		HashSet<Entity> list = new HashSet<Entity> (outliers);		
		list.addAll(root.get(new Box(bounds)));
		return new ArrayList<Entity> (list);
	}
	
	/** Returns a Rectangle2D.Double representing the boundaries of this QuadTree.
	 * 
	 * @return the Rectangle2D.Double object representing this QuadTree's bounds
	 */
	public Rectangle2D getBounds()
	{
		return root.getBounds().toRectangle();
	}

	/** Retrieves the QuadBranch specified by the given QuadTreePath.
	 * Returns the deepest possible QuadBranch if the chain of branches 
	 * ends before the QuadTreePath does.
	 * <p>
	 * As specified by the constant field variables in the QuadTreePath class:<br>
	 * {-2} -> the outliers list in the master QuadTree<br>
	 * {-1} -> the root QuadBranch<br>
	 * {...,0} -> north-east quadrant<br>
	 * {...,1} -> north-west quadrant<br>
	 * {...,2} -> south-west quadrant<br>
	 * {...,3} -> south-east quadrant<br><br>
	 * 
	 * Note that cardinal directions are relative to the Cartesian plane,
	 * where y-values increase from bottom to top. 
	 * 
	 * @param path		the QuadTree path to the desired QuadBranch
	 * @return the QuadBranch, or null if either the path was null or if
	 * 		the path specifies the master QuadTree
	 */
	protected QuadBranch getBranch(QuadTreePath path)
	{
		QuadBranch branch = null;		
		if (path != null)
		{
			if (path.get(0) != QuadTreePath.TREE)
			{
				branch = root;				
				for (int i = 1; i < path.length && branch.hasChildren(); i++)				
					branch = branch.getChild(path.get(i));				
			}
		}
		return branch;
	}	

	/** Returns an array containing all of the Entities that are 
	 * contained within the root QuadBranch.
	 * 
	 * @return all containing the Entities within the root QuadBranch
	 */
	public ArrayList<Entity> getInliers()
	{
		HashSet<Entity> list = root.get();
		return new ArrayList<Entity> (list);
	}		

	/** Returns an array containing all of the Entities that are not
	 * contained within the root QuadBranch.
	 * 
	 * @return all containing the outlier Entities
	 */
	public ArrayList<Entity> getOutliers()
	{		
		return new ArrayList<Entity> (outliers);
	}	

	/** Returns the number of Entities in this QuadTree.
	 * 
	 * @return the number of Entities in this QuadTree
	 */
	public int getSize()
	{
		return size;
	}		

	/** Rechecks the specified Entity's current placement in the QuadTree,
	 * and repositions it if necessary. If the Entity is in more than one QuadBranch, 
	 * each of the noncommittal copies of the Entity in all of the child QuadBranches 
	 * are checked accordingly.
	 * 
	 * @param e		the Entity to revalidate
	 */
	public void revalidate (Entity e) //TODO: outlier revalidate
	{		
		Box entityBounds = new Box (e.getShape().getBounds());
		QuadBranch branch = getBranch(e.path);

		boolean valid;

		if (branch == null)
			valid = !root.contains(entityBounds);
		else
		{			
			valid = branch.contains(entityBounds);
			if (valid)
				if (branch.hasChildren())
					branch.revalidateNoncommittals(e, entityBounds);
		}

		if (!valid)			// TODO: down-up re-adding 
			if (remove(e))
				add(e);
	}

	/** Attempts to find the specified Entity in this QuadTree structure.
	 * Returns the QuadTreePath of the Entity if found, or null if not found.
	 * 
	 * @param e		the Entity to be search for
	 * @return the QuadTreePath of the Entity if found; null otherwise
	 */
	public QuadTreePath searchDownwards(Entity e)
	{
		QuadTreePath newPath;		
		if (outliers.indexOf(e) != -1)			
			newPath = this.path;		
		else		
			newPath = root.searchDownwards(e);
		return newPath;
	}

	/** Attempts to find the specified Entity in this QuadTree structure.
	 * Starts the search at the QuadBranch specified by the Entity's 
	 * QuadTreePath variable. First searches down from that location. If not
	 * found, goes up a level, and searches down in all of the child QuadBranches
	 * of that QuadBranch. Stops at the root QuadBranch. Does not search outliers.
	 * 
	 * @param e		the Entity to be search for
	 * @return the QuadTreePath of the Entity if found; null otherwise
	 */
	public QuadTreePath searchUpwards(Entity e)
	{	
		QuadTreePath newPath = null;
		QuadBranch branch = null;

		if (e.path != null) // if the path is valid
		{
			if (e.path.get(0) != QuadTreePath.TREE) // if the path points to a QuadBranch
			{
				branch = getBranch (e.path); // retrieve the QuadBranch
				newPath = branch.searchUpwards(e); // Search upwards from selected QuadBranch
			}
		}			

		return newPath;				
	}	


	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException 
	{
		// Read and set root bounds		

		double minX = in.readDouble();
		double minY = in.readDouble();
		double maxX = in.readDouble();
		double maxY = in.readDouble();

		reset(minX, minY, maxX, maxY);

		// Read in Entities

		int size = in.readInt();
		Entity[] all = new Entity[size];
		
		for (int i = 0; i < all.length; i++)
			all[i] = (Entity)in.readObject();

		// Re-add all Entities

		for (Entity e : all)		
			if (e != null)		
				add(e);						
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException 
	{
		Box rootBounds = root.getBounds();
		double minX = rootBounds.minX;
		double minY = rootBounds.minY;
		double maxX = rootBounds.maxX;
		double maxY = rootBounds.maxY;

		// Write root bounds

		out.writeDouble(minX);
		out.writeDouble(minY);
		out.writeDouble(maxX);
		out.writeDouble(maxY);

		// Write all Entities

		ArrayList<Entity> all = get();
		out.writeInt(all.size());

		for (Entity e : all)
			out.writeObject(e);
	}
	
	/** Used for debugging
	 * 
	 * @param g
	 */
	public void draw (Graphics2D g)
	{		
		root.draw(g);
	}
}

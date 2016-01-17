import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** A class that stores Entities. If the number of Entities in a branch
 * exceeds the value specified by {@code maxSize}, the branch will recursively
 * divide its Entities amongst 4 smaller QuadBranches. This is to make 
 * retrieval and collision checking more efficient.
 * <p>
 * If an Entity occupies more than one of the new QuadBranches, then that 
 * Entity remains stored in the original QuadBranch.
 * <p>
 * North-east QuadBranch -> larger x; larger y;<br>
 * North-west QuadBranch -> smaller x; larger y;<br>
 * South-west QuadBranch -> smaller x; smaller y;<br>
 * South-east QuadBranch -> larger x; smaller y;
 * <p>
 * Note that unlike here, y-coordinates are inverted in Graphics.
 * <p>
 * Currently does not implement any mechanism for merging children QuadBranches.
 *  
 * @author Jiayin Huang
 * @author Sally Hui
 * @author Tony Cui 
 */
public class QuadBranch implements Serializable
{	
	private static final long serialVersionUID = 1L;

	/** The ID number for the north-east quadrant. 
	 */
	public static final int NE = QuadTreePath.NE;
	
	/** The ID number for the north-west quadrant. 
	 */
	public static final int NW = QuadTreePath.NW;
	
	/** The ID number for the south-west quadrant. 
	 */
	public static final int SW = QuadTreePath.SW;
	
	/** The ID number for the south-east quadrant. 
	 */
	public static final int SE = QuadTreePath.SE;
	
	/** A list of all of the Entities that are entirely enclosed
	 * by this QuadBranch. 
	 */
	private List<Entity> leaves;

	/** A list of all of the Entities that are only partially in 
	 * this QuadBranch. 
	 */
	private List<Entity> noncommittals;
	
	
	/** The parent QuadBranch of this QuadBranch, or null if there
	 * is no parent. 
	 */
	private QuadBranch parent;
	
	/** The four children QuadBranches of this QuadBranch, or null
	 * if there are no children. 
	 */	
	private QuadBranch[] children;

	/** The maximum number of Entities that can be held by this QuadBranch
	 * before it divides. This value includes both Entities that are entirely
	 * enclosed by this QuadBranch as well as noncommittal Entities that are
	 * only partially in this QuadBranch. 
	 */
	public int maxSize = 8;
	
	/** The maximum depth that this QuadBranch structure will go before ceasing
	 * to divide any further. 
	 */
	public final int maxDepth = 8;
	
	/** The current depth of this QuadBranch. A QuadBranch will have a depth 
	 * value 1 greater than that of its parent. For example, a root QuadBranch
	 * will always start with <code>depth = 0</code>, so all of its children
	 * QuadBranches have <code>depth = 1</code>. 
	 */
	public final int depth;
	
	/** The number of Entities and noncommittal Entities held by this QuadBranch.
	 * This does not include any Entities in any children QuadBranches. 
	 */
	private int size = 0;	

	/** The Box representing the boundaries of this QuadBranch. 
	 */
	private Box bounds;		

	/** The QuadTreePath to this QuadBranch. 
	 */
	public QuadTreePath path;
	
	/** Is true if this QuadBranch has children QuadBranches; false otherwise  
	 */
	private boolean hasChildren = false;

	/** Creates a new QuadBranch with the specified parent and bounds. The
	 * <code>quadrant</code> parameter specifies which quadrant this QuadBranch
	 * occupies in its parent QuadBranch.
	 * 
	 * @param parent	the parent if this QuadBranch; null is this is the root
	 * @param quadrant	this QuadBranch's quadrant number in the parent QuadBranch
	 * @param bounds	the Box detailing the boundaries of this QuadBranch
	 */
	protected QuadBranch(QuadBranch parent, int quadrant, Box bounds) 
	{
		// Initializing variables
		
		this.bounds = bounds;
		this.parent = parent;		
		leaves = new ArrayList<Entity>();
		noncommittals = new ArrayList<Entity>();

		if (parent == null) // This is the root branch
		{
			depth = 0;
			path = new QuadTreePath(true);
		}
		else // This is the child of some other branch
		{
			path = parent.path.getBranchPath(quadrant);
			depth = parent.depth + 1;
			if (depth > maxDepth)
				maxSize = Integer.MAX_VALUE; // thereby preventing any further division
		}			
	}	

	/** Adds the specified Entity to this QuadBranch. Assumes that the
	 * invoker of this method has already correctly determined that the
	 * Entity is contained at least partially by this QuadBranch. If the
	 * Entity fits in a deeper QuadBranch, this method pushes the task
	 * to that branch.
	 * 
	 * @param e					the Entity to be added
	 * @param entityBounds		the Box representing the bounds of the Entity's shape
	 */
	public void add(Entity e, Box entityBounds)
	{	
		if (hasChildren) // check where to add Entity
		{			
			List<Integer> branchIDs = determineBranch(entityBounds);

			if (branchIDs.size() > 1) // then this Entity is in more than one quadrant
			{
				// Add the Entity to the leaf of this Branch

				leaves.add(e);
				e.path = path;
				size++;

				// Add noncommittal copies of the Entity to deeper Branches

				for (int i = 0; i < branchIDs.size(); i++)
					children[branchIDs.get(i)].addNoncommittal(e, entityBounds);			
			}
			else // delegate the adding of the Entity to the correct child Branch
			{
				children[branchIDs.get(0)].add(e, entityBounds);
			}
		}
		else // add Entity to the leaves of this Branch and determine if division is necessary
		{
			leaves.add(e);
			e.path = path;			
			size++;
			
			if (size > maxSize)			
				divide();			
		}
	}	

	/** Adds to this QuadBranch a noncommittal copy of an Entity that occupies
	 * more than one QuadBranch.
	 * 
	 * @param e					the Entity to be added
	 * @param entityBounds		the Box representing the bounds of the Entity's shape
	 */
	private void addNoncommittal(Entity e, Box entityBounds)
	{
		if (hasChildren) // check where to add Entity
		{			
			List<Integer> branchIDs = determineBranch(entityBounds);

			if (branchIDs.size() > 1)										
				for (int i = 0; i < branchIDs.size(); i++)
					children[branchIDs.get(i)].addNoncommittal(e, entityBounds);

			else // delegate the adding of the Entity to a deeper Branch			
				children[branchIDs.get(0)].addNoncommittal(e, entityBounds);			
		}
		else // add Entity to the noncommittal list of this Branch and determine if division is necessary
		{
			noncommittals.add(e);		
			size++;			
			if (size > maxSize)							
				divide();		
		}
	}

	/** Removes all of the elements in this QuadBranch. Calls the
	 * <code>clear()</code> method on any children QuadBranches, 
	 * then sets them to null. Sets <code>hasChildren</code> to false.
	 * Clears the <code>leaves</code> and <code>noncommittals</code> 
	 * Lists, and sets them to null. Sets size to 0. 
	 */
	public void clear()
	{
		if (hasChildren) 
		{
			for (int i = 0; i < children.length; i++)
				children[i].clear();
			children = null;
			hasChildren = false;
		} 
		else 
		{
			leaves.clear();
			leaves = null;
			noncommittals.clear();
			noncommittals = null;
		}		
		size = 0;
	}
	
	/** Determines if this QuadBranch encompasses the specified Box entirely.
	 * 
	 * @param box	the Box to be checked
	 * @return true if the Box is contained entirely within the bounds
	 * 		of this QuadBranch; false otherwise 
	 */
	public boolean contains(Box box)
	{
		return bounds.contains(box);
	}
	
	
	/** Determines if this QuadBranch contains the specified point.
	 * 
	 * @param x		the x-coordinate of the point
	 * @param y		the y-coordinate of the point
	 * @return true if the point is inside of the bounds of this 
	 * 		QuadBranch; false otherwise
	 */
	public boolean contains(double x, double y)
	{
		return bounds.contains(x, y);
	}	
	
	
	/** Determines the quadrants in which the specified Box
	 * lies, assuming that this QuadBranch already has 
	 * children. Returns an array containing the quadrant IDs of
	 * all of the quadrants that this box falls under. It is
	 * impossible for the box to fall under less than one quadrant.
	 * 
	 * @param box	the specified Box
	 * @return an array containing the IDs of all of the quadrants
	 * 		that this box falls under
	 */
	protected List<Integer> determineBranch(Box box)
	{			
		List<Integer> results = new ArrayList<Integer>(4);

		if (box.minX < bounds.centreX) // box is left of center
		{
			if (box.minY < bounds.centreY) // box is below centre 			
				results.add(SW);	

			if (box.maxY >= bounds.centreY) // box is above centre, inclusive			
				results.add(NW);	
		}

		if (box.maxX >= bounds.centreX) // box is right of centre, inclusive  
		{
			if (box.minY < bounds.centreY) // box is below centre 			
				results.add(SE);

			if (box.maxY >= bounds.centreY) // box is above centre, inclusive			
				results.add(NE);		
		}		

		return results;
	}	
	

	/** Determines the quadrant in which the specified point
	 * lies, assuming that this QuadBranch already has children.
	 * 
	 * @param x		the x-coordinate of the point
	 * @param y		the y-coordinate of the point
	 * @return the ID of the correct quadrant
	 */
	protected int determineBranch(double x, double y) 
	{
		int result; 

		if (x < bounds.centreX) 
		{
			if (y > bounds.centreY)
				result = NW;
			else
				result = SW;
		}
		else
		{
			if (y > bounds.centreY)
				result = NE;
			else
				result = SE;				
		}

		return result;
	}

	
	/** Splits this QuadBranch into four child QuadBranches, one for
	 * each quadrant. Removes all of the Entities from the <code>leaves</code>
	 * and <code>noncommittals</code> Lists, then re-adds them using the 
	 * <code>add()</code> method. This splits the Entities amongst the new 
	 * children QuadBranches accordingly. 
	 */
	public void divide()
	{
		if (!hasChildren)
		{
			// Initialize bounds of new QuadBranches

			double x1 = bounds.minX;
			double x2 = bounds.centreX;
			double x3 = bounds.maxX;

			double y1 = bounds.minY;
			double y2 = bounds.centreY;
			double y3 = bounds.maxY;

			hasChildren = true;
			children = new QuadBranch [4];
			
			children[NE] = new QuadBranch (this, NE, new Box (x2, y3, x3, y2)); // top right rectangle
			children[NW] = new QuadBranch (this, NW, new Box (x1, y3, x2, y2)); // top left rectangle
			children[SW] = new QuadBranch (this, SW, new Box (x1, y2, x2, y1)); // bottom left rectangle
			children[SE] = new QuadBranch (this, SE, new Box (x2, y2, x3, y1)); // bottom right rectangle

			// Move leaf Entities into new branches
			
			size = 0;
			Entity[] tempLeaves = leaves.toArray(new Entity[leaves.size()]);
			Entity[] tempNoncommittals = noncommittals.toArray(new Entity[noncommittals.size()]);
			
			leaves.clear();
			noncommittals.clear();
			
			for (Entity e : tempLeaves)
				add(e, new Box(e.getShape().getBounds()));			
			for (Entity e : tempNoncommittals)
				addNoncommittal(e, new Box(e.getShape().getBounds()));
		}			
	}	
	
	/** Returns a list containing all of the Entities stored within this QuadBranch.
	 * This includes all of the Entities in the leaves of this QuadBranch,
	 * and all of the Entities in all of the leaves of all child QuadBranches.
	 * This does not include noncommittal Entities that are stored in the leaf
	 * of a QuadBranch higher than this one.
	 * 
	 * @return a list containing all of the Entities stored within this QuadBranch
	 */
	public HashSet<Entity> get()
	{
		HashSet<Entity> values = new HashSet<Entity> (leaves);		

		if (hasChildren)		
			for (int i = 0; i < children.length; i++)			
				values.addAll(children[i].get());

		return values;
	}
	
	/** Returns a list containing all of the Entities stored in the deepest
	 * QuadBranch or QuadBranches that intersect the specified Box.
	 * 
	 * @param bounds	the specified Box
	 * @return a list containing all of the Entities in the QuadBranch specified
	 * 		by the given Box
	 */
	public HashSet<Entity> get(Box bounds)
	{				
		HashSet<Entity> list = new HashSet<Entity>();

		if (hasChildren)
		{
			for (QuadBranch branch : children)
				if (branch.intersects(bounds))
					list.addAll(branch.get(bounds));
		}
		else
		{			
			list.addAll(leaves);
			list.addAll(noncommittals);
		}

		return list;
	}	

	/** Returns a list containing all of the Entities in the deepest QuadBranch 
	 * that contains the specified point. This also includes all of the
	 * noncommittal Entities that are only partially in that QuadBranch.
	 * 
	 * @param x 	the x-coordinate of the point
	 * @param y		the y-coordinate of the point
	 * @return a list containing all of the Entities in the QuadBranch specified by
	 * 		the given coordinates
	 */
	public HashSet<Entity> get(double x, double y)
	{
		HashSet<Entity> list = new HashSet<Entity>();
		if (hasChildren)
		{
			QuadBranch subBranch = children[determineBranch(x, y)];
			list.addAll(subBranch.get(x, y));
		}
		else
		{
			list.addAll(leaves);
			list.addAll(noncommittals);
		}
		return list;
	}	
	
	/** Returns the Box representing the boundaries of this QuadBranch.
	 * 
	 * @return the Box representing the boundaries of this QuadBranch
	 */
	public Box getBounds()
	{
		return bounds;
	}
	
	/** Gets the child of this QuadBranch in the specified quadrant.
	 * Returns null if the specified quadrant ID is invalid or if this
	 * QuadBranch does not have any children.
	 * 
	 * @param quadrant		the quadrant ID of the requested child branch
	 * @return the child QuadBranch in the specified quadrant
	 */
	public QuadBranch getChild(int quadrant)
	{
		QuadBranch branch = null;
		if (hasChildren && quadrant >= 0 && quadrant <= 3)
			branch = children[quadrant];
		return branch;
	}

	/** Returns the array containing the children QuadBranches of this
	 * QuadBranch, or null if there are no children.
	 * 
	 * @return the array containing this QuadBranch's children, if any;
	 * 		null otherwise
	 */
	public QuadBranch[] getChildren()
	{
		return children;
	}
	
	/** Returns a list of all of the Entities contained in the
	 * leaves of this QuadBranch. This does not include any 
	 * children QuadBranches.
	 * 
	 * @return the leaf Entities contained in this QuadBranch
	 */
	public HashSet<Entity> getLeaves()
	{
		return new HashSet<Entity>(leaves);
	}
	
	/** Returns a list of all of the noncommittal Entities
	 * that are within the bounds of this QuadBranch. This method
	 * goes all the way down to the deepest QuadBranches contained
	 * within this QuadBranch and retrieves all of their noncommittals.
	 * A HashSet is used to avoid duplicated. 
	 * 
	 * @return a list of all of the noncommittal Entities stored in
	 * 		this QuadBranch and in any children QuadBranches
	 */
	public HashSet<Entity> getNoncommittals()
	{
		HashSet<Entity> list = new HashSet<Entity>();
		if (hasChildren)
			for (QuadBranch b : children)
				list.addAll(b.getNoncommittals());
		else
			list.addAll(noncommittals);

		return list;
	}

	/** Returns the parent of this QuadBranch, or null if there
	 * is no parent. 
	 * 
	 * @return this QuadBranch's parent, if it exists; null otherwise
	 */
	public QuadBranch getParent()
	{
		return parent;
	}	

	/** Returns the number of Entities and noncommittal Entities held 
	 * by this QuadBranch. This does not include any Entities in any 
	 * children QuadBranches. 
	 * 
	 * @return the number of Entities in this QuadBranch
	 */
	public int getSize()
	{
		return size;
	}	
	
	/** Returns true if this QuadBranch has children.
	 * 
	 * @return true if this QuadBranch has children
	 */
	public boolean hasChildren()
	{
		return hasChildren;
	}		

	/** Determines if this QuadBranch is storing the specified Entity
	 * itself. Returns true if this Entity is present in the list of 
	 * <code>leaves</code>. Does not return true if the Entity is found 
	 * in a child QuadBranch of this QuadBranch, or if the Entity is found 
	 * in the list of <code>noncommittals</code>. 
	 * 
	 * @param e		the Entity to search for
	 * @return true if this branch itself contains the specified Entity 
	 */
	public boolean hasEntity(Entity e)
	{
		return leaves.indexOf(e) != -1;
	}
	
	public boolean hasParent()
	{
		return parent != null;
	}
	
	/** Determines if this QuadBranch intersects the
	 * specified Box.
	 * 
	 * @param box	the Box to be checked
	 * @return true if the Box intersects the bounds of this
	 * 		QuadBranch; false otherwise
	 */
	public boolean intersects(Box box)
	{
		return bounds.intersects(box);
	}		

	/** Attempts to remove the specified Entity from this QuadBranch.
	 * If the Entity was a noncommittal this method removes all of the
	 * noncommittal copies as well. Returns false if the specified Entity 
	 * could not be found in this QuadBranch or any of its children QuadBranches.
	 * 
	 * @param e		the Entity to be removed
	 * @return true if the Entity was removed; false otherwise
	 */
	public boolean remove(Entity e)
	{
		boolean removed = leaves.remove(e);
		if (removed)
			size--;

		if (hasChildren)	
		{
			if (removed) // This means that the Entity was in multiple QuadBranches			
				removeNoncommittals(e);

			else // This means that the Entity was entirely contained within a deeper QuadBranch			
				for (int i = 0; i < children.length && !removed; i++)
					removed = children[i].remove(e);			
		}

		return removed;
	}

	/** Removes all of the noncommittal instances of the
	 * specified Entity.
	 * 
	 * @param e		the specified Entity
	 */
	private void removeNoncommittals(Entity e)
	{
		if (noncommittals.remove(e))
			size--;	
		else
			if (hasChildren)
				for (QuadBranch b : children)
					b.removeNoncommittals(e);
	}

	/** Revalidates all of the noncommittal copies of the specified Entity.
	 * 
	 * @param e				the Entity to verify
	 * @param entityBounds	the Box representing the bounds of the Entity's shape
	 */
	public void revalidateNoncommittals(Entity e, Box entityBounds)
	{
		if (hasChildren)
		{
			List<Integer> branchIDs = determineBranch(entityBounds);
			if (branchIDs.size() == 1)	
			{
				if (e.path.equals(this.path))
				{
					remove(e);
					children[branchIDs.get(0)].add(e, entityBounds);
				}
			}
			else
			{
				for (QuadBranch branch : children)
					branch.revalidateNoncommittals(e, entityBounds);
			}
		}
		else
		{
			boolean intersects = intersects(entityBounds);
			if (noncommittals.indexOf(e) != -1)
			{
				if (!intersects)
					removeNoncommittals(e);
			}
			else
			{
				if (intersects)
					addNoncommittal(e, entityBounds);
			}
		}							
	}

	/** Attempts to find the specified Entity in the QuadBranch structure from
	 * here downwards, including this QuadBranch itself. Returns the QuadTreePath 
	 * of the Entity if found, or null if not found.
	 * 
	 * @param e		the Entity to search for
	 * @return the QuadTreePath of the Entity if found; null otherwise
	 */
	public QuadTreePath searchDownwards(Entity e)
	{
		QuadTreePath newPath = null;
		if (leaves.indexOf(e) != -1)		
			newPath = this.path;		
		else		
			if (hasChildren)			
				for (int i = 0; i < children.length && newPath == null; i++)
					newPath = children[i].searchDownwards(e);

		return newPath;		
	}

	/** Attempts to find the specified Entity in an upwards pattern.
	 * First checks if the Entity is in this branch or in any 
	 * children branches. If not, then this method goes to the parent
	 * branch and searches the three other quadrants. If still not found,
	 * this method does up another level. The search stops when the current
	 * branch does not have a parent.
	 * 
	 * @param e		the Entity to search for
	 * @return the QuadTreePath of the Entity if found; null otherwise
	 */
	public QuadTreePath searchUpwards(Entity e)
	{
		QuadBranch previousBranch = this;
		QuadBranch branch = this;
		QuadTreePath newPath = null;
		newPath = searchDownwards(e);
		if (newPath == null)
		{
			while (branch.parent != null && newPath == null)
			{
				previousBranch = branch;
				branch = branch.parent;

				for (int i = 0; i < branch.children.length && newPath == null; i++)				
					if (!branch.children[i].path.equals(previousBranch.path))
						newPath = branch.children[i].searchDownwards(e);				
			}
		}
		return newPath;
	}
	
	public void draw (Graphics2D g)
	{
		g.draw(bounds.toRectangle());
		if (hasChildren)
			for (QuadBranch branch : children)
				branch.draw(g);
	}
}

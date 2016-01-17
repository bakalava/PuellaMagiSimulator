import java.io.Serializable;

/** A class representing the path to an Entity in a QuadTree.
 * Note that when talking about cardinal directions, we're talking about
 * with respect to the Cartesian plane, unlike on a Graphics object, 
 * where the y axis is reversed.
 * <p>
 * 0 = North-east<br>
 * 1 = North-west<br>
 * 2 = South-west<br>
 * 3 = South-east<p>
 * Some examples of valid QuadTree paths:
 * <br>
 * {-2} -> QuadTree outlier<br>
 * {-1} -> root QuadBranch<br>
 * {-1, 0} -> North-east quadrant of root QuadBranch<br>
 * {-1, 0, 3, 2, 2} -> Go NE, SE, SW, SW from root<br>
 * 
 * @see QuadTree
 * 
 * @author Jiayin Huang
 * @author Sally Hui
 * @author Tony Cui 
 */
public class QuadTreePath implements Serializable //TODO: validate path
{	
	private static final long serialVersionUID = 1L;

	/** The ID number for the master QuadTree. 
	 */
	public static final int TREE = -2;

	/** The ID number for the root QuadBranch 
	 */
	public static final int ROOT = -1;

	/** The ID number for the north-east quadrant. 
	 */
	public static final int NE = 0;	

	/** The ID number for the north-west quadrant. 
	 */
	public static final int NW = 1;	

	/** The ID number for the south-west quadrant. 
	 */
	public static final int SW = 2;	

	/** The ID number for the south-east quadrant. 
	 */
	public static final int SE = 3;

	/** The number of nodes in this QuadTreePath. 
	 */
	public final int length;
	
	private int[] path;

	/** If isBranch is true, this QuadTreePath points towards
	 * the root QuadBranch. If isBranch is false, this QuadTreePath
	 * points towards the outliers list in the QuadTree.
	 * 
	 * @param isBranch	if this path should point to the root QuadBranch or not
	 */
	public QuadTreePath(boolean isBranch) 
	{
		path = new int[1];		
		length = 1;
		if (isBranch)
			path[0] = ROOT;
		else
			path[0] = TREE;		
	}

	/** Creates a new QuadTreePath with the specified nodes.
	 * 
	 * @param nodes		the specified quadrant ID sequence
	 */
	private QuadTreePath(int[] nodes)
	{
		path = new int[nodes.length];
		for (int i = 0; i < path.length; i++)
			path[i] = nodes[i];
		length = nodes.length;		
	}

	/** Retrieves the quadrant ID node at the specified index
	 * in the QuadTreePath.
	 * 
	 * @param index		the index in the QuadTreePath
	 * @return the ID number for the node at the specified index
	 * @throws IndexOutOfBoundsException if the index is out of bounds 
	 * 		(index < 0 || index >= size())
	 */
	public int get(int index)
	{		
		return path[index];	
	}

	/** Returns an int array containing the full list of quadrant ID
	 * nodes in this QuadTreePath in the correct order.
	 * 
	 * @return an int array containing all of the nodes in this QuadTreePath.
	 */
	public int[] getPath()
	{
		int[] temp = new int[path.length];
		for (int i = 0; i < temp.length; i++)
			temp[i] = path[i];
		return temp;
	}

	/** Returns a new QuadTreePath instance identical to
	 * this one, but with the specified quadrant ID node 
	 * concatenated to the end.
	 * 
	 * @param quadrant	the quadrant ID to append to the path
	 * @return a new QuadTreePath with the added quadrant ID node
	 */
	public QuadTreePath getBranchPath(int quadrant)
	{
		QuadTreePath branch = null;
		if (quadrant >= 0 && quadrant <= 3)
		{
			int[] temp = new int[path.length + 1];
			for (int i = 0; i < path.length; i++)
				temp[i] = path[i];
			temp[path.length] = quadrant;
			branch = new QuadTreePath(temp);
		}
		else
		{
			System.out.println("Warning: Invalid Quadrant " + quadrant);
		}
		return branch;
	}

	/** Returns a new QuadTreePath instance identical
	 * to this one, except one node shorter. The last quadrant ID
	 * node is truncated. Returns null if this QuadTreePath is
	 * already length 0.
	 * 
	 * @return a new QuadTreePath that's one node shorter
	 */
	public QuadTreePath getParentPath()
	{
		QuadTreePath parent = null;
		if (path.length > 0)
		{
			int[] temp = new int[path.length - 1];
			for (int i = 0; i < temp.length; i++)
				temp[i] = path[i];
			parent = new QuadTreePath(temp);
		}

		return parent;
	}

	/** Returns a string representation of this QuadTreePath as
	 * a list of QuadBranch quadrants, separated by commas.
	 * For example: <code>QuadTreePath@1dc5237{-1,3,0,0,3,0,3,3}</code> 
	 */
	@Override
	public String toString()
	{
		String str = super.toString() + "{";
		for (int i = 0; i < path.length; i++)
		{
			str += path[i];
			if (i != path.length - 1)
				str += ",";
		}
		str += "}";
		return str;
	}

}

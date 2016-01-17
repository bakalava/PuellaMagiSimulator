
public class WitchBarrier 
{
	protected Spacetime labyrinth;
	protected Portal entrance;
	protected Portal exit;

	public WitchBarrier(int width, int height) 
	{
		labyrinth = new Spacetime (width, height);
		entrance = new Portal (0, 0, 50);
		exit = new Portal (0, 0, 50);
		entrance.setTarget(exit);
		exit.setTarget(entrance);
	}
	
	public void setEntrance (Spacetime grid, double x, double y, double theta)
	{
		grid.addEntity(entrance);
		entrance.setAngle(theta);
		entrance.setLocation(x, y);		
	}
	
	public void setExit (double x, double y, double theta)
	{
		labyrinth.addEntity(exit);
		exit.setAngle (theta);
		exit.setLocation(x, y);
		
	}

}

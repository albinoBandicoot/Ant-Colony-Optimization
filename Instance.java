import java.util.*;
public abstract class Instance {

	/* Abstract class representing an instance of a problem that might be solved
	 * with ant colony optimization. The only current implementation is TSPInstance,
	 * for the traveling salesman problem. */

	public ArrayList<State> states;
	public State initial;

	public abstract ArrayList<State> getViableNeighbors (Ant a) ;
	public abstract void reset (AntColony ac);

}


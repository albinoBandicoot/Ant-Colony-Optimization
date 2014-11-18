import java.util.*;
public class TSPState extends State {

	/* These represent the cities */

	public double x,y;	// position of city
	public double[] pheromones;
	public TSPInstance inst;

	public TSPState (int id, double x, double y, int ncities, TSPInstance tspi) {
		super (id);
		this.x = x;
		this.y = y;
		pheromones = new double[ncities];
		Arrays.fill (pheromones, 10.0);
		inst = tspi;
	}

	public double distanceTo (State other) {
		TSPState s = (TSPState) other;
		return Math.sqrt ( (x-s.x) * (x-s.x) + (y-s.y) * (y-s.y) );
	}

	public double getPheromones (State other) {
		return pheromones[ ((TSPState) other).id ];
	}

	public void setPheromones (State other, double amt) {
		pheromones[ ((TSPState) other).id ] = amt;
	}

	public ArrayList<TSPState> getNeighbors () {
		return inst.cities;	// every node has everything as a neighbor - so the neighbors list is the list of 
							// cities. No sense in copying it over to every node.
	}
}

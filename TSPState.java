import java.util.*;
public class TSPState extends State {

	/* These represent the cities */

	public double x,y;	// position of city
	public double[] pheromones;	// amount of pheromones along edge to each other city
	public double[] probabilities;	// precomputed probabilities (not really probabilities, but proportional to the probabilities - these are
									// probably better termed 'desirabilities') for each other city. These are the pheromones^ALPHA + (1/distance)^BETA.
									// Storing these rather than computing them each time is a performance optimization.

	public double[] distances;	// this allows for arbitrary edge weights that don't have to come from Euclidean distances in the plane
	public TSPInstance inst;

	public TSPState (int id, double x, double y, int ncities, TSPInstance tspi) {
		super (id);
		this.x = x;
		this.y = y;
		pheromones = new double[ncities];
		probabilities = new double[ncities];
		distances = new double[ncities];
		inst = tspi;
	}

	/* Compute the distances to each other city */
	public void initDistances () {
		for (int i=0; i < inst.states.size(); i++) {
			TSPState t = (TSPState) inst.states.get(i);
			distances[i] = Math.sqrt ( (x-t.x)*(x-t.x) + (y-t.y)*(y-t.y) );
		}
	}

	public void initProbabilities (double alpha, double beta) {
		for (int i=0; i < inst.states.size(); i++) {
			setPheromones (inst.states.get(i), 10.0, alpha, beta);
		}
	}

	public double distanceTo (State other) {
		return distances [ ((TSPState) other).id ];
	}

	public double getPheromones (State other) {
		return pheromones[ ((TSPState) other).id ];
	}

	public void setPheromones (State other, double amt, double alpha, double beta) {
		TSPState t = (TSPState) other;
		pheromones[t.id] = amt;
		probabilities[t.id] =  Math.pow (getPheromones(t), alpha) * Math.pow (desirability (t), beta);
	}

	public void reset () {
		Arrays.fill (pheromones, 10.0);
	}

	public double getProbability (State other) {
		return probabilities[((TSPState) other).id];
	}

	public ArrayList<State> getNeighbors () {
		return inst.states;	// every node has everything as a neighbor - so the neighbors list is the list of 
							// cities. No sense in copying it over to every node.
	}

	public String toString () {
		return "(" + x + ", " + y + ")";
	}
}

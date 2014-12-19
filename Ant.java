import java.util.*;
public class Ant {

	/* Represents a single ant. */

	public int id = 0;
	public ArrayList<State> path;		// the path the ant has followed
	public HashSet <State>  pathset;	// stores the same states as the path, but in a HashSet for 
										// greater efficiency in determining the viable neighbors.
	public double length;	// stores the length of the current path, for efficiency.
	public boolean done;	// whether the path has terminated (looped back on itself)
	public Instance inst;	// pointer to the parent instance
	public AntColony ac;	// pointer to the ant colony that's using this ant

	private static int NUM = 0;

	/* Create a new ant with just the initial state of the instance in its path */
	public Ant (AntColony ac, Instance inst) {
		this.inst = inst;
		this.id = NUM++;
		this.ac = ac;
		path = new ArrayList<State>();
		pathset = new HashSet <State>();
		path.add (inst.initial);
		pathset.add (inst.initial);
		length = 0;
		done = false;
	}

	/* Adds a state to the path, and updates the running count of the path length */
	public void add (State s) {
		if (!path.isEmpty()) {
			length += path.get (path.size()-1).distanceTo (s);
		}
		path.add (s);
		pathset.add (s);
	}

	/* Put down pheromones according to Q/length along the edges of this ant's path */
	public void depositPheromones (double multiplier) {
		double amt = ac.Q / length;
		for (int i=0; i < path.size()-1; i++) {
			State next = path.get(i+1);
			path.get(i).setPheromones (next, path.get(i).getPheromones(next) + amt, ac.ALPHA, ac.BETA);
		}
	}

	/* Based on distances and current pheromone levels, decide where to go next and add it  */
	public boolean extendPath () {
		if (done) return true;
		ArrayList<State> viable_neighbors = inst.getViableNeighbors (this);
		if (viable_neighbors.isEmpty ()) {	// we're done when there's nowhere left to go.
			done = true;
			return true;
		}
		/* Now compute the probabilities of going to each neighbor, and select one at random
		 * according to those probabilities */
		State curr = path.get (path.size() - 1);
		double psum = 0;
		for (State s : viable_neighbors) {
			psum += curr.getProbability (s);
		}
		double rand = Math.random() * psum;
		double accum = 0;
		for (State s : viable_neighbors) {
			accum += curr.getProbability (s);
			if (rand <= accum) {
				add (s);
				return false;
			}
		}
		// should never be here
		System.out.println("Uh oh you should not be reading this.");
		return false;
	}
}

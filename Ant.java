import java.util.*;
public class Ant {

	public ArrayList<State> path;
	public double length;	// stores the length of the current path, for efficiency.
	public boolean done;
	public Instance inst;

	public Ant (Instance inst) {
		this.inst = inst;
		path = new ArrayList<State>();
		path.add (inst.initial);
		length = 0;
		done = false;
	}

	public void add (State s) {
		if (!path.isEmpty()) {
			length += path.get (path.size()-1).distanceTo (s);
		}
		path.add (s);
	}

	public void depositPheromones (double multiplier) {
		double amt = AntColony.Q / length;
		for (int i=0; i < path.size()-1; i++) {
			State next = path.get(i+1);
			path.get(i).setPheromones (next, path.get(i).getPheromones(next) + amt);
		}
	}

	/* Based on distances and current pheromone levels, decide where to go next and add it 
	 */
	public boolean extendPath () {
		if (done) return true;
		ArrayList<State> viable_neighbors = inst.getViableNeighbors (this);
		if (viable_neighbors.isEmpty ()) {
			done = true;
			return true;
		}
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

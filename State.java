import java.util.*;
public abstract class State {

	public int id;		// unique ID number for each state; can be used as an index
	public double psum;	// = sum over all neighbors of (pheromones^alpha)*(desirability^beta). 

	public State (int id) {
		this.id = id;
		psum = 0;
	}

	public abstract double distanceTo (State other);			// get the distance (cost) of an edge to state 'other'
	public abstract double getPheromones (State other);
	public abstract void setPheromones (State other, double amt);
	public abstract ArrayList<? extends State> getNeighbors ();

	public double desirability (State other) {
		return 1 / distanceTo (other);
	}

	public final double getProbability (State other) {
		if (other == this) return 0;	// don't choose self
		return Math.pow (getPheromones(other), AntColony.ALPHA) * Math.pow (desirability (other), AntColony.BETA);
	}

	public final double normalizedProbability (State other) {
		return getProbability (other) / psum;
	}

	public final void evaporatePheromones () {
		for (State n : getNeighbors()) {
			setPheromones (n, getPheromones(n) * (1 - AntColony.RHO));
		}
	}

	public final void updatePsum () {
		psum = 0;
		for (State n : getNeighbors()) {
			 psum += getProbability (n);
		}
	}

}

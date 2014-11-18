import java.util.*;
public abstract class State {

	public int id;		// unique ID number for each state; can be used as an index

	public State (int id) {
		this.id = id;
	}

	public abstract double distanceTo (State other);			// get the distance (cost) of an edge to state 'other'
	public abstract double getPheromones (State other);
	public abstract void setPheromones (State other, double amt);
	public abstract ArrayList<? extends State> getNeighbors ();

	public double desirability (State other) {
		return 1 / distanceTo (other);
	}

	public final double getProbability (State other) {
		return Math.pow (getPheromones(other), AntColony.ALPHA) * Math.pow (desirability (other), AntColony.BETA);
	}

	public final void evaporatePheromones () {
		for (State n : getNeighbors()) {
			setPheromones (n, getPheromones(n) * (1 - AntColony.RHO));
		}
	}
}

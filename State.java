import java.util.*;
public abstract class State {

	public int id;		// unique ID number for each state; can be used as an index

	public State (int id) {
		this.id = id;
	}

	public abstract double distanceTo (State other);			// get the distance (cost) of an edge to state 'other'
	public abstract double getPheromones (State other);
	public abstract void setPheromones (State other, double amt, double alpha, double beta);
	public abstract ArrayList<? extends State> getNeighbors ();
	public abstract void reset ();

	public double desirability (State other) {
		return 1 / distanceTo (other);
	}

	public static int PRCALLS = 0;

	public abstract double getProbability (State other);
	/*
	public final double getProbability (State other) {
		PRCALLS ++;
		return Math.pow (getPheromones(other), AntColony.ALPHA) * Math.pow (desirability (other), AntColony.BETA);
	}
	*/

	public final void evaporatePheromones (AntColony ac) {
		for (State n : getNeighbors()) {
			setPheromones (n, getPheromones(n) * (1 - ac.RHO), ac.ALPHA, ac.BETA);
		}
	}
}

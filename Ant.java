import java.util.*;
public class Ant {

	private ArrayList<State> states;
	public double length;	// stores the length of the current path, for efficiency.

	public Ant () {
		states = new ArrayList<State>();
		length = 0;
	}

	public void add (State s) {
		if (!states.isEmpty()) {
			length += states.get (states.size()-1).distanceTo (s);
		}
		states.add (s);
	}

	public void depositPheromones () {
		double amt = AntColony.Q / length;
		for (int i=0; i < states.size()-1; i++) {
			State next = states.get(i+1);
			states.get(i).setPheromones (next, states.get(i).getPheromones(next) + amt);
		}
	}
}

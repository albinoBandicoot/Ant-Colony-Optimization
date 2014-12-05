import java.util.*;
public class TSPInstance extends Instance {
	
	public static final int SIZE = 500; // max x, y value for a city; should correspond with SIZE variable in AntColony.java
	public static Random rand = new Random (751);	// use a fixed seed so we can compare tweaking parameter values on the same random instance

	public TSPInstance (int n) {
		states = new ArrayList<State>();
		for (int i=0; i < n; i++) {
			states.add (new TSPState (i, rand.nextDouble()*SIZE, rand.nextDouble()*SIZE, n, this));
		}
		initial = states.get(0);
	}


	public ArrayList<State> getViableNeighbors (Ant a) {
		if (a.path.size() == states.size()) {	// then we want the only option to be returning to the start point
			ArrayList<State> res = new ArrayList<State>();
			res.add (a.path.get(0));
			return res;
		} else {
			ArrayList<State> viable = new ArrayList<State>();
			for (State s : states) {
				if (!a.path.contains(s)) viable.add (s);
			}
			return viable;
		}
	}
}



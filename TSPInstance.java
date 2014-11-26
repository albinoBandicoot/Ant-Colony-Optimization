import java.util.*;
public class TSPInstance extends Instance {

	public TSPInstance (int n) {
		states = new ArrayList<State>();
		for (int i=0; i < n; i++) {
			states.add (new TSPState (i, Math.random()*30, Math.random()*30, n, this));
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



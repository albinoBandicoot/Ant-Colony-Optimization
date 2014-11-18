import java.util.*;
public class TSPInstance {

	public ArrayList<TSPState> cities;

	public ArrayList<State> getViableNeighbors (Ant a) {
		if (a.path.size() == cities.size()) {	// then we want the only option to be returning to the start point
			ArrayList<State> res = new ArrayList<State>();
			res.add (a.path.get(0));
			return res;
		} else {
			ArrayList<State> viable = new ArrayList<State>();
			for (State s : cities) {
				if (!a.path.contains(s)) viable.add (s);
			}
			return viable;
		}
	}
}



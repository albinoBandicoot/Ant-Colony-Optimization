import java.util.*;
import java.io.*;
public class TSPInstance extends Instance {
	
	public static Random rand = new Random (751);	// use a fixed seed so we can compare tweaking parameter values on the same random instance

	public TSPInstance (int n) {
		states = new ArrayList<State>();
		for (int i=0; i < n; i++) {
			states.add (new TSPState (i, rand.nextDouble()*AntColony.SIZE, rand.nextDouble()*AntColony.SIZE, n, this));
		}
		initial = states.get(0);
		for (State s : states) {
			((TSPState) s).initProbabilities(1,1);
			((TSPState) s).initDistances();

		}
	}

	public TSPInstance (File f) throws IOException {
		ArrayList<double[]> cities = new ArrayList<double[]>();
		Scanner sc = new Scanner (f);
		while (sc.hasNext()) {
			cities.add (new double[]{sc.nextDouble(), sc.nextDouble()});
		}
		states = new ArrayList<State>();
		for (int i=0; i < cities.size(); i++) {
			states.add (new TSPState (i, cities.get(i)[0], cities.get(i)[1], cities.size(), this));
		}
		initial = states.get(0);
		for (State s : states) {
			((TSPState) s).initProbabilities(1,1);
			((TSPState) s).initDistances();
		}
		// this has stored the real distances. Now we can move around the x,y coords of the states in order to display them better, without affecting the path lengths.
		double[] min = {1e20, 1e20};
		double[] max = new double[2];
		for (double[] point : cities) {
			min[0] = Math.min (min[0], point[0]);
			min[1] = Math.min (min[1], point[1]);
			max[0] = Math.max (max[0], point[0]);
			max[1] = Math.max (max[1], point[1]);
		}
		double xsize = max[0] - min[0];
		double ysize = max[1] - min[1];
		double size = Math.max (xsize, ysize);
		double scale = (AntColony.SIZE-20) / size;
		double xextra_room = (1 - xsize / size) * AntColony.SIZE / 2;
		double yextra_room = (1 - ysize / size) * AntColony.SIZE / 2;
		System.out.printf ("Min: [%f, %f], Max: [%f, %f], size = %f, scale = %f\n", min[0], min[1], max[0], max[1], size, scale);
		for (State s : states) {
			TSPState t = (TSPState) s;
			t.x = (t.x - min[0]) * scale + xextra_room + 10;
			t.y = (t.y - min[1]) * scale + yextra_room + 10;
			t.y = AntColony.SIZE - t.y;
		}
	}

	public void reset (AntColony ac) {
		for (State s : states) {
			s.reset();
			((TSPState) s).initProbabilities(ac.ALPHA, ac.BETA);
		}
	}

	public double pheromoneSum () {
		double sum = 0;
		for (State s : states) {
			TSPState t = (TSPState) s;
			for (int i=0; i < t.pheromones.length; i++) {
				sum += t.pheromones[i];
			}
		}
		return sum;
	}

	public ArrayList<State> getViableNeighbors (Ant a) {
		if (a.path.size() == states.size()) {	// then we want the only option to be returning to the start point
			ArrayList<State> res = new ArrayList<State>();
			res.add (a.path.get(0));
			return res;
		} else {
			ArrayList<State> viable = new ArrayList<State>();
			for (State s : states) {
				if (!a.pathset.contains(s)) viable.add (s);
			}
			return viable;
		}
	}
}



import java.util.*;
public class AntColony {

	public static final int SIZE = 500;		// Draws a display window of dimensions (size, size); should correspond with SIZE variable in TSPInstance.java
	public static final double ALPHA = 2;	// exponent on pheromone amount; controls how strongly pheromones influence the decision about which neighbor node to visit next
	public static final double BETA  = 2;	// exponent on distance.
	public static final double Q = 100;		// constant multiplier for how much pheromone each ant deposits.
	public static final double RHO = 0.01;	// pheromone evaporation coefficient. All pheromones are multiplied by (1-RHO) 
											// before this generation's ants make their contributions.
	public static final double ELITE_PHEROMONE_MUL = 1;	// the elite ant deposits extra pheromones; multiply the normal by this amount.
	
	public static Ant runACO (Instance inst, int num_ants, int num_generations) {
		Picture pic = new Picture(SIZE, SIZE); 
		pic.setPenColor(255, 255, 255);
		pic.drawRectFill(0, 0, SIZE, SIZE);
		pic.setPenColor(145, 145, 145);
		for (int i = 0; i < SIZE; i = i + SIZE/10) {
			pic.drawLine(i, 0, i, SIZE);
			pic.drawLine(0, i, SIZE, i);
		}
		pic.setPenColor(0, 0, 0);
		
		Ant elite = null;
		for (int i=0; i < num_generations; i++) {
			System.out.println("Generation " + i);
			// first make an array of ants that start on the start state 
			Ant[] ants = new Ant[num_ants];
			for (int j=0; j < num_ants; j++) {
				ants[j] = new Ant (inst);
			}

			// now run the ants 
			boolean all_done = true;
			do {
				all_done = true;
				for (Ant a : ants) {
					all_done &= a.extendPath();
				}
			} while (!all_done);

			// now find the best ant
			if (elite == null) elite = ants[0];
			for (int j=0; j < num_ants; j++) {
				if (ants[j].length < elite.length) elite = ants[j];
			}
			System.out.println("Best ant in iteration " + i + " has length " + elite.length);
			visualize(elite.path, pic);
			pic.setPenColor(255, 255, 255);
			pic.drawRectFill(0, 0, SIZE, SIZE);
			pic.setPenColor(145, 145, 145);
			for (int i1 = 0; i1 < SIZE; i1 = i1 + SIZE/10) {
				pic.drawLine(i1, 0, i1, SIZE);
				pic.drawLine(0, i1, SIZE, i1);
			}
			pic.setPenColor(0, 0, 0);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			// now update pheromones
			// first the uniform decay
			for (State c : inst.states) {
				c.evaporatePheromones();
			}
			// now add the ant contributions
			for (Ant a : ants) {
				a.depositPheromones(1);
			}
			// have the elite path deposit extra pheromones
			elite.depositPheromones(ELITE_PHEROMONE_MUL);
		}
		return elite;
	}
	
	public static void visualize(ArrayList<State> path, Picture pic) {
		int xCurr = -1;
		int yCurr = -1;
		int xNext = -1;
		int yNext = -1;
		for (int i = 0; i < path.size()-1; i++) { // first and last city in path are the same
			State currCity = path.get(i);
			State nextCity = path.get(i+1);
			xCurr = (int) ((TSPState) currCity).x;
			yCurr = (int) ((TSPState) currCity).y;
			xNext = (int) ((TSPState) nextCity).x;
			yNext = (int) ((TSPState) nextCity).y;
			pic.drawCircleFill(xCurr, yCurr, 5);
			pic.drawLine(xCurr, yCurr, xNext, yNext);
		}
		pic.display();
	}
	
	
	public static void main (String[] args) {
//		TSPInstance t = new TSPInstance(10);
		TSPInstance t = new TSPInstance(Integer.parseInt (args[0]));
		Ant soln = runACO (t, Integer.parseInt (args[1]), Integer.parseInt (args[2]));
		System.out.println("Path length: " + soln.length);
		for (State s : soln.path) {
			System.out.println("City #" + s.id + ": " + (TSPState) s);
		}
	}
}

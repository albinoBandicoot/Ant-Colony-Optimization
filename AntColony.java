import java.util.*;
public class AntColony {

	public static final double ALPHA = 1;	// exponent on pheromone amount; controls how strongly pheromones influence the decision about which neighbor node to visit next
	public static final double BETA  = 2;	// exponent on distance.
	public static final double Q = 1;		// constant multiplier for how much pheromone each ant deposits.
	public static final double RHO = 0.2;	// pheromone evaporation coefficient. All pheromones are multiplied by (1-RHO) 
											// before this generation's ants make their contributions.
	public static final double ELITE_PHEROMONE_MUL = 5;	// the elite ant deposits extra pheromones; multiply the normal by this amount.
	
	public ArrayList<State> runACO (Instance inst, int num_ants, int num_generations) {
		Ant elite = null;
		for (int i=0; i < num_generations; i++) {
			// first make an array of ants that start on the start state 
			Ant[] ants = new Ant[num_ants];
			for (int j=0; j < num_ants; j++) {
				ants[j] = new Ant (inst);
			}

			// now run the ants 
			boolean all_done = true;
			do {
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
		return elite.path;
	}

}

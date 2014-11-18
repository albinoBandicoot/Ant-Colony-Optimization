import java.util.*;
public class AntColony {

	public static final double ALPHA = 1;	// exponent on pheromone amount; controls how strongly pheromones influence the decision about which neighbor node to visit next
	public static final double BETA  = 2;	// exponent on distance.
	public static final double Q = 1;		// constant multiplier for how much pheromone each ant deposits.
	public static final double RHO = 0.2;	// pheromone evaporation coefficient. All pheromones are multiplied by (1-RHO) 
											// before this generation's ants make their contributions.

}

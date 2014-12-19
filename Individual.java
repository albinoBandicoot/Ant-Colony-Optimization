public class Individual {

	/* One individual in the genetic algorithm for breeding ant colonies */

	public AntColony ac;
	public double fitness;

	/* Allowable ranges for random values of parameters. Order is alpha, beta, q, rho; the same as in the get/setParam methods of AntColony */
	public static final double[] paramMins =  {0.05, 0.05, 0.1, 0};
	public static final double[] paramMaxes = {20.0, 20.0, 1000, 1};

	/* More parameters controlling the genetic algorithm mechanics*/
	public static final int ANTS = 20;			// number of ants to use 
	public static final int ACO_GENERATIONS = 90;	// number of generations to run the ant colonies
	public static final double MUTATION_SIZE = 0.3;	// control on how large mutations are (as a proportion of the allowable range)


	// these control the range of random weights for the linear combinations used in the crossover
	public static final double MIN_WT = -0.3;
	public static final double MAX_WT = 1.3;

	/* Generate a random individual */
	public Individual () {
		ac = new AntColony();
		ac.ALPHA = Math.random() * (paramMaxes[0] - paramMins[0]) + paramMins[0];
		ac.BETA  = Math.random() * (paramMaxes[1] - paramMins[1]) + paramMins[1];
		ac.Q     = Math.random() * (paramMaxes[2] - paramMins[2]) + paramMins[2];
		ac.RHO   = Math.random() * (paramMaxes[3] - paramMins[3]) + paramMins[3];

		ac.NUM_ANTS = ANTS;
	}

	/* Generate a child by crossover of the two parents */
	public Individual (Individual a, Individual b) {
		// for each parameter, pick a random weight W between MIN_WT and MAX_WT. 
		// The child's value is W*a.param + (1-W)*b.param
		this();
		for (int i=0; i <= 3; i++) {
			double W = Math.random() * (MAX_WT - MIN_WT) + MIN_WT;
			double val = W*a.ac.getParam(i) + (1-W) * b.ac.getParam(i);
			ac.setParam (i, Math.min (paramMaxes[i], Math.max (paramMins[i], val)));
		}
	}

	/* Mutate an individual */
	public void mutate () {
		int param = (int) (Math.random() * 4);
		double val = ac.getParam (param);
		// add/subtract a random value up to MUTATION_SIZE times the size of the allowed range
		val += (paramMaxes[param] - paramMins[param]) * MUTATION_SIZE * (Math.random() * 2 - 1);
		val = Math.min (paramMaxes[param], Math.max (paramMins[param], val));	// clamp to allowed range
	}

	public double evalFitness (TSPInstance[] tests) {
		fitness = 0;
		for (TSPInstance t : tests) {
			t.reset (ac);	// this clears up garbage from previous runs and also initializes the probabilities 
							// correctly given the alpha, beta values for this ant colony
			fitness += mappingFunc (ac.runACO (t, ACO_GENERATIONS, null, false).length);
		}
		fitness /= tests.length;
		return fitness;
	}

	/* This function is very important. It controls how the raw lengths from the runs of ACO are mapped to fitness
	 * values. The high exponent is used to make the differences between different individuals larger, as typically
	 * the length differences are quite small, and so there is very little selection pressure. It is also important
	 * to use C/len instead of C*len, since we want to select for minimum lengths, not maximum ones. */
	public double mappingFunc (double len) {
		return Math.pow(5400/len, 10);
	}

	public String toString () {
		return "alpha = " + ac.ALPHA + "; beta = " + ac.BETA + "; Q = " + ac.Q + "; rho = " + ac.RHO + "; fitness = " + fitness;
	}

}	

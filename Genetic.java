public class Genetic {

	/* Genetic Algorithm to breed Ant Colony TSP Solvers
	 *
	 * Chromosomes consist of values of the parameters ALPHA, BETA, RHO, and Q
	*/

	public Individual[] parents;
	public Individual[] children;
	public Individual elite;
	public TSPInstance[] tests;

	public double fitsum;

	public int POP_SIZE = 20;
	public double MUTATION_RATE = 0.1;
	public int NUM_TESTS = 7;
	public int NUM_CITIES = 20;
	public int GENERATIONS = 25;

	/* Initialize the population with random individuals, and generate random test instances of TSP */
	public Genetic () {
		parents = new Individual[POP_SIZE];
		children = new Individual[POP_SIZE];
		tests = new TSPInstance[NUM_TESTS];
		for (int i=0; i < NUM_TESTS; i++) {
			tests[i] = new TSPInstance (NUM_CITIES);
		}
		for (int i=0; i < POP_SIZE; i++) {
			parents[i] = new Individual ();
		}
	}

	/* Evaluate the fitness of all of the parents, and compute fitsum, the sum of these fitnesses */
	public void evalFitness () {
		fitsum = 0;
		int idx = 0;
		for (Individual i : parents) {
			fitsum += i.evalFitness (tests);
			System.out.println (i);
		}
	}

	/* Find the parent with the highest fitness */
	public Individual findEliteParent () {
		int idx = 0;
		double best_fitness = 0;
		for (int i=0; i < POP_SIZE; i++) {
			if (parents[i].fitness > best_fitness) {
				best_fitness = parents[i].fitness;
				idx = i;
			}
		}
		return parents[idx];
	}

	/* Choose parents randombly with probability p.fitness / fitsum. */
	public Individual pickParent () {
		double r = Math.random() * fitsum;
		double acc = 0;
		int i = 0;
		while (acc < r) {
			acc += parents[i].fitness;
			i++;
		}
		return parents[i-1];
	}

	/* Run one generation of the genetic algorithm */
	public void runGeneration () {
		evalFitness();	// first evaluate the fitness of all the parents
		Individual e = findEliteParent();	// find the best in this generation and update the elite if it's better
		if (elite == null) {
			elite = e;
		} else if (e.fitness > elite.fitness) {
			elite = e;
		}
		// always include the elite in the new generation
		children[0] = elite;
		// now generate children until we have enough
		for (int i=1; i < POP_SIZE; i++) {
			Individual a = pickParent ();
			Individual b = pickParent ();
			children[i] = new Individual (a, b);	// crossover
			if (Math.random() < MUTATION_RATE) {	// potentially mutate
				children[i].mutate();
			}
		}
		// now swap the parents and children
		Individual[] temp = parents;
		parents = children;
		children = temp;
	}

	public void run () {
		for (int i=0; i < GENERATIONS; i++) {
			System.out.println ("Running GENETIC generation " + i);
			runGeneration ();
			System.out.println ("Current elite = " + elite);
		}
	}

	public static void main (String[] args) {
		Genetic g = new Genetic ();
		g.run ();
		System.out.println ("Elite: " + g.elite);
	}
}

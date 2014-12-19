import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.*;
public class AntColony {

	/* This is the main class for the ant colony algorithm. It contains the guts, as well as the visualization code,
	 * which makes use of the Picture class. */

	public static int SIZE = 600;	// size of the graphics display window

	/* Parameters for the Ant Colony algorithm */
	public double ALPHA = 2;	// exponent on pheromone amount; controls how strongly pheromones influence the decision about which neighbor node to visit next
	public double BETA  = 1;	// exponent on distance.
	public double Q = 100;		// constant multiplier for how much pheromone each ant deposits.
	public double RHO = 0.15;	// pheromone evaporation coefficient. All pheromones are multiplied by (1-RHO) 
											// before this generation's ants make their contributions.
	
	public int NUM_ANTS = 50;	// number of ants to use
	public static int NUM_GENERATIONS = 100;	// number of generations to run

	public double NORMAL_PHEROMONE_MUL = 1;	// multiplier on normal ant pheromone deposition. It could be interesting to try setting this to 0
											// and the elite multiplier to 1, so that only the elite ant deposits pheromones.
	public double ELITE_PHEROMONE_MUL = 1;	// the elite ant deposits extra pheromones; multiply the normal by this amount.
	
	/* Information saved for graphing and analysis purposes */
	public double[] elitelens;
	public double[] avglens;
	public double[] delta_phersum;
	public double prev_phersum;

	/* Settings for visualization */
	public static final int[] BACKGROUND_COLOR = {255, 255, 255}; 
	public static final int[] GRID_COLOR = {145, 145, 145}; 
	public static final int[] ANT_COLOR = {0, 0, 0}; 
	public static final int[] CITY_COLOR = {0, 0, 0}; 
	public static final int[] ELITE_COLOR = {255, 0, 0}; 
	public static final int ANT_WIDTH = 1;
	public static final int ELITE_WIDTH = 4;
	public static final int CITY_RADIUS = 5;
	public static final int GRID_SPACING = 20; // draws grid lines SIZE/GRID_SPACING units apart
	public static final int SLEEP_TIME = 0; // number of milliseconds the program sleeps per generation (for visualization purposes)


	public AntColony () {
		// default parameter values
	}

	public AntColony (double alpha, double beta, double q, double rho, int nants) {
		ALPHA = alpha;
		BETA = beta;
		Q = q;
		RHO = rho;
		NUM_ANTS = nants;
	}

	public void setParam (int param, double val) {
		switch (param){
			case 0:
				ALPHA = val;	break;
			case 1:
				BETA = val;		break;
			case 2:
				Q = val;		break;
			case 3:
				RHO = val;		break;
			case 4:
				NUM_ANTS = (int) val;	break;
			default:
				System.out.println("Invalid paramter. Choices are 0: alpha, 1: beta, 2: Q, 3: rho");
				System.exit(1);
		}
	}

	public double getParam (int param) {
		switch (param) {
			case 0:	return ALPHA;
			case 1: return BETA;
			case 2: return Q;
			case 3: return RHO;
			case 4: return NUM_ANTS;
		}
		return 0;
	}

	public static String paramName (int param) {
		return new String[]{"ALPHA", "BETA", "Q", "RHO", "NUM_ANTS"}[param];
	}

	/* Run the ant colony algorithm for num_generations on the given instance. Pass in a Picture to draw
	 * on, or null to run in non-GUI mode. */
	public Ant runACO (Instance inst, int num_generations, Picture pic, boolean verbose) {
		inst.reset (this);
		boolean graphical = pic != null;

		// these arrays record information for graphing and analysis
		elitelens = new double[num_generations];	// length of the elite path at each generation
		avglens = new double[num_generations];		// average of the path lengths of all the ants in each generation
		delta_phersum = new double[num_generations];	// how much the sum of all the pheromones along all the edges changes from generation to generation
		prev_phersum = ((TSPInstance) inst).pheromoneSum();
		if (graphical) {
			pic.setPenColor(BACKGROUND_COLOR[0], BACKGROUND_COLOR[1], BACKGROUND_COLOR[2]);
			pic.drawRectFill(0, 0, SIZE, SIZE);
			pic.setPenColor(GRID_COLOR[0], GRID_COLOR[1], GRID_COLOR[2]);
			for (int i = 0; i < SIZE; i = i + SIZE/10) {
				pic.drawLine(i, 0, i, SIZE);
				pic.drawLine(0, i, SIZE, i);
			}
			pic.setPenColor(ANT_COLOR[0], ANT_COLOR[1], ANT_COLOR[2]);
		}
		
		Ant elite = null;
		for (int i=0; i < num_generations; i++) {
			// first make an array of ants that start on the start state 
			Ant[] ants = new Ant[NUM_ANTS];
			for (int j=0; j < NUM_ANTS; j++) {
				ants[j] = new Ant (this, inst);
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
			double tot = 0;
			if (elite == null) elite = ants[0];
			for (int j=0; j < NUM_ANTS; j++) {
				tot += ants[j].length;
				if (ants[j].length < elite.length) elite = ants[j];
			}
			elitelens[i] = elite.length;
			avglens[i] = tot / NUM_ANTS;

			if (graphical) {
				for (Ant a : ants) {
					visualize(a.path, pic, false);
				}
				visualize(elite.path, pic, true);
				pic.display();
				// reset background and grid
				pic.setPenColor(BACKGROUND_COLOR[0], BACKGROUND_COLOR[1], BACKGROUND_COLOR[2]);
				pic.drawRectFill(0, 0, SIZE, SIZE);
				pic.setPenColor(GRID_COLOR[0], GRID_COLOR[1], GRID_COLOR[2]);
				for (int i1 = 0; i1 < SIZE; i1 = i1 + SIZE/GRID_SPACING) {
					pic.drawLine(i1, 0, i1, SIZE);
					pic.drawLine(0, i1, SIZE, i1);
				}
				pic.setPenColor(ANT_COLOR[0], ANT_COLOR[1], ANT_COLOR[2]);
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// now update pheromones
			// first the uniform decay
			for (State c : inst.states) {
				c.evaporatePheromones(this);
			}
			// now add the ant contributions
			for (Ant a : ants) {
				a.depositPheromones(NORMAL_PHEROMONE_MUL);
			}
			// have the elite path deposit extra pheromones
			elite.depositPheromones(ELITE_PHEROMONE_MUL);

			double newph = ((TSPInstance) inst).pheromoneSum();
			delta_phersum[i] = newph - prev_phersum;
			prev_phersum = newph;
			if (verbose) System.out.println("Generation " + i + "; elite length " + elite.length + "; average length " + avglens[i] + "; phersum, delta  = " + newph + ", " + delta_phersum[i]);
		}
		if (verbose)	System.out.println();
		return elite;
	}
	
	public static void visualize(ArrayList<State> path, Picture pic, boolean isShortest) {
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
			pic.setPenColor(CITY_COLOR[0], CITY_COLOR[1], CITY_COLOR[2]);
			pic.drawCircleFill(xCurr, yCurr, CITY_RADIUS);
			if (isShortest) {
				pic.setPenWidth(ELITE_WIDTH);
				pic.setPenColor(ELITE_COLOR[0], ELITE_COLOR[1], ELITE_COLOR[2]);
			} else {
				pic.setPenWidth(ANT_WIDTH);
				pic.setPenColor(ANT_COLOR[0], ANT_COLOR[1], ANT_COLOR[2]);
			}
			pic.drawLine(xCurr, yCurr, xNext, yNext);
			if (isShortest) {
				pic.setPenWidth(ANT_WIDTH);
				pic.setPenColor(ANT_COLOR[0], ANT_COLOR[1], ANT_COLOR[2]);
			}
		}
	}

	public static void consume (Scanner console) {
		console.nextLine();
	}

	public static void main (String[] args) {
		Scanner console = new Scanner(System.in);
		TSPInstance t = null;
		System.out.println("Welcome to our ACO Travelling Salesman Problem implementation!");
		System.out.println();
		int option = 0;
		AntColony ac = new AntColony ();
		while (t == null) {
			System.out.println ("How do you want to generate your TSP instance?");
			System.out.println ("(1) load an instance from a file");
			System.out.println ("(2) generate a random instance");
			System.out.print (">>> ");
			try {
				option = console.nextInt ();
				if (option == 1) {
					System.out.print("Enter a filename: ");
					String fname = console.next();
					try {
						t = new TSPInstance (new File (fname)); 
					} catch (IOException e) {
						System.out.println ("There was a problem reading the file. ");
						e.printStackTrace ();
//						continue;
					}
				} else if (option == 2) {
					System.out.print ("How many cities would you like to generate? ");
					try {
						int ncities = console.nextInt();
						if (ncities > 0) {
							t = new TSPInstance (ncities);
						} else {
							System.out.println ("Expecting a positive integer.");
						}
					} catch (InputMismatchException e) {
						System.out.println ("Invalid input. Please type an integer.");
						consume (console);
					}

				} else {
					System.out.println ("Invalid input. Please type either 1 or 2");
					consume (console);
				}
			} catch (Exception e) {
				System.out.println ("Invalid input. Please type either 1 or 2");
				consume (console);
			}
		}

		System.out.print ("Number of ants: ");
		ac.NUM_ANTS = console.nextInt();

		System.out.print ("Number of generations to run: ");
		NUM_GENERATIONS = console.nextInt();

		Picture pic = null;	
		while (option != 5) {
			System.out.println("What do you want to do?");
			System.out.println("(1) run with GUI");
			System.out.println("(2) run without GUI");
			System.out.println("(3) set parameters");
			System.out.println("(4) generate graph");
			System.out.println("(5) quit");
			System.out.print(">>> ");
			try {
				option = console.nextInt();
			} catch (Exception e) {
				System.out.println ("Not a valid input; please type an integer from 1-5");
				continue;
			}
			System.out.println();
			if (option == 1) {
				pic = new Picture (SIZE, SIZE);
			} else if (option == 2) {
				pic = null;
			} else  if (option == 3) {
				for (int i=0; i <= 3; i++) {
					System.out.print (paramName(i) + ": ");
					ac.setParam (i, console.nextDouble());
				}
				continue;
			} else if (option == 4) {
				System.out.print ("Graph style: (1) plot average path lengths against time   (2) plot avg. path lengths & convergence times against parameter: ");
				int style = console.nextInt();
				System.out.print ("Which parameter to vary? (1) alpha (2) beta (3) Q (4) rho (5) num ants (adjusting Q): ");
				int param = console.nextInt() - 1;
				System.out.print ("Starting value: ");
				double start = console.nextDouble();
				System.out.print ("Ending value: ");
				double end = console.nextDouble();
				System.out.print ("Number of steps: ");
				int nsteps = console.nextInt();
				Variator v = new Variator (paramName(param), start, end, nsteps);

				System.out.print ("Average results over N runs. N = ");
				int nruns = console.nextInt();
				System.out.print ("Output file (.png extension): ");
				String fname = console.next();

				BufferedImage result = null;
				if (style == 1) {	// lengths against time
					System.out.print ("X-axis scale (number of pixels per generation): ");
					int xscale = console.nextInt();
					result = Graphing.graphLengths (ac, t, NUM_GENERATIONS, nruns, v, xscale, false);
					
				} else if (style == 2) {
					System.out.print ("X-axis scale (number of pixels per parameter step): ");
					int xscale = console.nextInt();
					result = Graphing.graphParam (ac, t, NUM_GENERATIONS, nruns, v, xscale, 0);
				}

				try {
					ImageIO.write (result, "png", new File (fname));
				} catch (IOException e) {
					System.out.println ("Could not write output.");
					e.printStackTrace();
				}
				continue;
				
			} else if (option == 5) {
				System.exit(0);
			}
			System.out.println();

			Ant soln = ac.runACO(t, NUM_GENERATIONS, pic, true);
			System.out.println("Path length: " + soln.length);
			for (State s : soln.path) {
				System.out.println("City #" + s.id + ": " + (TSPState) s);
			}
		}
	}
}

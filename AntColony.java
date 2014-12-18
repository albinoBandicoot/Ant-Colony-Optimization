import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.*;
public class AntColony {

	public static final int SIZE = 500;		// Draws a display window of dimensions (size, size); should correspond with SIZE variable in TSPInstance.java
	public static double ALPHA = 2;	// exponent on pheromone amount; controls how strongly pheromones influence the decision about which neighbor node to visit next
	public static double BETA  = 1;	// exponent on distance.
	public static double Q = 3;		// constant multiplier for how much pheromone each ant deposits.
	public static double RHO = 0.15;	// pheromone evaporation coefficient. All pheromones are multiplied by (1-RHO) 
											// before this generation's ants make their contributions.
	
	public static int NUM_ANTS = 50;	// I only made this static so a Variator could play with it.

	public static final double NORMAL_PHEROMONE_MUL = 1;
	public static final double ELITE_PHEROMONE_MUL = 1;	// the elite ant deposits extra pheromones; multiply the normal by this amount.
	
	public static double[] elitelens;
	public static double[] avglens;
	public static double[] delta_phersum;
	public static double prev_phersum;


	public static Ant runACO (Instance inst, int num_generations, Picture pic) {
		boolean graphical = pic != null;
		elitelens = new double[num_generations];
		avglens = new double[num_generations];
		delta_phersum = new double[num_generations];
		prev_phersum = ((TSPInstance) inst).pheromoneSum();
		if (graphical) {
			pic.setPenColor(255, 255, 255);
			pic.drawRectFill(0, 0, SIZE, SIZE);
			pic.setPenColor(145, 145, 145);
			for (int i = 0; i < SIZE; i = i + SIZE/10) {
				pic.drawLine(i, 0, i, SIZE);
				pic.drawLine(0, i, SIZE, i);
			}
			pic.setPenColor(0, 0, 0);
		}
		
		Ant elite = null;
		for (int i=0; i < num_generations; i++) {
			if (!graphical) System.out.print ("\rGeneration: " + i);
			// first make an array of ants that start on the start state 
			Ant[] ants = new Ant[NUM_ANTS];
			for (int j=0; j < NUM_ANTS; j++) {
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
			double tot = 0;
			if (elite == null) elite = ants[0];
			for (int j=0; j < NUM_ANTS; j++) {
				tot += ants[j].length;
				if (ants[j].length < elite.length) elite = ants[j];
			}
			elitelens[i] = elite.length;
			avglens[i] = tot / NUM_ANTS;

			if (graphical) {
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
					Thread.sleep(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// now update pheromones
			// first the uniform decay
			for (State c : inst.states) {
				c.evaporatePheromones();
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
			if (graphical) System.out.println("Generation " + i + "; elite length " + elite.length + "; average length " + avglens[i] + "; phersum, delta  = " + newph + ", " + delta_phersum[i]);
		}
		System.out.println();
		return elite;
	}
	
	public static void visualize(ArrayList<State> path, Picture pic) {
		int xCurr = -1;
		int yCurr = -1;
		int xNext = -1;
		int yNext = -1;
		for (int i = 0; i < path.size()-2; i++) { // first and last city in path are the same
			State currCity = path.get(i);
			State nextCity = path.get(i+1);
			xCurr = (int) ((TSPState) currCity).x;
			yCurr = (int) ((TSPState) currCity).y;
			xNext = (int) ((TSPState) nextCity).x;
			yNext = (int) ((TSPState) nextCity).y;
			pic.drawCircleFill(xCurr, yCurr, 3);
			if (i == path.size() - 1) {
				pic.drawCircleFill (xNext, yNext, 3);
			}
			pic.drawLine(xCurr, yCurr, xNext, yNext);
		}
		pic.display();
	}

	/* Produce a graph of the final path length and number of generations before finding the eventual elite path
	 * on a particular TSP instance while varying one of the parameters (alpha, beta, Q, RHO). The parameter goes
	 * on the X-axis, and the path length and generation count goes on the Y */

	// set yscale to 0 for auto
	public static BufferedImage graphParam (TSPInstance t, int ng, int nruns, Variator v, int xstep, double yscale) {
		int xsize = xstep * v.nsteps;
		BufferedImage gr = new BufferedImage (xsize, 500, 1);
		Graphics2D g = (Graphics2D) gr.getGraphics();
		g.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor (Color.WHITE);
		g.fillRect (0,0, xsize, 500);

		double[] elens = new double[v.nsteps+2];
		double[] conv_time = new double[v.nsteps+2];
		System.out.println("nruns = " + nruns);
		for (int RUN = 0; RUN < nruns; RUN++) {
			int i = 0;
			while (v.hasNext()) {
				v.advance();
				Ant elite = runACO (t, ng, null);
	//			elens[i] = avglens[ng-1];
				elens[i] += elite.length;
				conv_time[i] += findConvTime (elitelens);
				t.reset();	// clear the pheromones so we can go again
				i++;
			}
			v.reset();
		}
		for (int i=0; i < elens.length; i++) {
			elens[i] /= nruns;
			conv_time[i] /= nruns;
		}
		if (yscale == 0) {
			yscale = elens[0]*1.1;
		}
		graphArray (g, Color.RED, elens, yscale, xstep);
		graphArray (g, Color.BLUE, conv_time, ng, xstep);

		g.setColor (Color.BLACK);
		g.drawString (paramName (v.param) + " from " + v.start + " to " + v.end, xsize - 170, 25);
		int w = 0;
		for (int j=0; j < 4; j++) {
			if (j != v.param) {
				g.drawString (paramName (j) + " = " + getParam (j), xsize -170, 45 + w*20);
				w++;
			}
		}
		axes (g, v.start, v.end, v.stepamt, 0, elens[0]*1.1, elens[0]*1.1/5, xsize, 500, paramName (v.param), "Stuff");
		return gr;
	}

	public static int findConvTime (double[] e) {
		double val = e[e.length-1];
		for (int i=e.length-1; i>=0; i--){ 
			if (e[i] != val) {
				return i;
			}
		}
		return 0;
	}

	public static String paramName (int param) {
		return new String[]{"ALPHA", "BETA", "Q", "RHO", "ALPHA, BETA"}[param];
	}

	public static void setParam (int param, double val) {
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

	public static double getParam (int param) {
		switch (param) {
			case 0:	return ALPHA;
			case 1: return BETA;
			case 2: return Q;
			case 3: return RHO;
			case 4: return ALPHA;
		}
		return 0;
	}

	public static Color getColor (float d) {
		if (d < 0.5) {
			d *= 2;
			return new Color (1-d, d, 0, 0.6f);
		} else {
			d = (d-0.5f)*2;
			return new Color (0, 1-d, d, 0.6f);
		}
	}

	public static BufferedImage graphLengths (TSPInstance t, int ng, int nruns, Variator v, int xscale, boolean graph_elites) {
		BufferedImage gr = new BufferedImage (ng*xscale, 500, 1);
		Graphics2D g = (Graphics2D) gr.getGraphics();
		g.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor (Color.WHITE);
		g.fillRect (0,0,ng*xscale,500);

		double ybound = 0;
		while (v.hasNext()) {
			double[] avgavg = new double[ng];
			double[] eliteavg = new double[ng];
			v.advance();
			float d = ((float) v.curstep) / v.nsteps;
			Color col = getColor (d);

			for (int i=0; i < nruns; i++) {
				runACO (t, ng, null);
				for (int j=0; j < avglens.length; j++) {
					avgavg[j] += avglens[j] / nruns;
					eliteavg[j] += elitelens[j] / nruns;
				}
				t.reset();
			}
			if (ybound == 0) ybound = avglens[0] * 1.25;
			if (graph_elites) 
				graphArray (g, Color.BLACK, eliteavg, ybound, xscale);
			graphArray (g, col, avgavg, ybound, xscale);
//			t = new TSPInstance(t.states.size());
		}
		axes (g, 0, ng, ng/5, 0, ybound, ybound/5, ng*xscale, 500, "Generations", "Path length");
		return gr;
	}

	public static final int XAXIS_OFFSET = 35;
	public static final int YAXIS_OFFSET = 10;
	public static final int XLABEL_YPOS = 15;
	public static final int TIC_LEN = 8;
	public static void axes (Graphics2D g, double xmin, double xmax, double xtick, double ymin, double ymax, double ytick, int xs, int ys, String xlabel, String ylabel) {
		g.setColor (Color.BLACK);
		g.setStroke (new BasicStroke (2));
		g.drawLine (YAXIS_OFFSET, ys - XAXIS_OFFSET, xs, ys - XAXIS_OFFSET);
		g.drawLine (YAXIS_OFFSET, ys - XAXIS_OFFSET, YAXIS_OFFSET, 0);

		g.setStroke (new BasicStroke (1));
		int i = 0;
		double xstep = (xmax - xmin)/xtick * (xs - YAXIS_OFFSET);
		System.out.println("xstep = " + xstep);
		for (double x = xmin; x < xmax; x += xtick) {
			int xpos = (int) (i * xstep);
			System.out.println("xpos = " + xpos);
			g.drawLine (xpos, ys-(XAXIS_OFFSET+TIC_LEN/2), xpos, ys-(XAXIS_OFFSET - TIC_LEN/2));
		}
		g.drawString (xlabel, xs/2 - 50, ys - XLABEL_YPOS);
	}


	public static void graphArray (Graphics2D g, Color col, double[] array, double ybound, int xscale) {
		g.setColor (col);
		g.setStroke (new BasicStroke (2));
		for (int x=0; x < array.length-1; x++) {
			int cur_y = 500 - (int) (array[x]*(500 - XAXIS_OFFSET)/ybound + XAXIS_OFFSET);
			int next_y = 500 - (int) (array[x+1]*(500 - XAXIS_OFFSET)/ybound + XAXIS_OFFSET);
			g.drawLine (x*xscale + YAXIS_OFFSET, cur_y, (x+1)*xscale + YAXIS_OFFSET, next_y);
			if (xscale > 10) {	// draw little circles
				g.fillOval (x*xscale + YAXIS_OFFSET - 4, cur_y - 4, 8, 8);
			}
		}
	}
	
	public static void main (String[] args) throws java.io.IOException{
//		TSPInstance t = new TSPInstance(10);
		if (args[0].equals ("graph")) {
			Scanner sc = new Scanner (System.in);
			//System.out.println("# of cities: ");
			//int nc = sc.nextInt();
			System.out.print("# of ants: ");
			NUM_ANTS = sc.nextInt();
			TSPInstance t = new TSPInstance (new File ("uscap.txt"));
			BufferedImage img = graphParam (t, 120, 10, new Variator ("Beta", 0.1, 2.5, 40), 25, 0);
			//BufferedImage img = graphLengths (t, 150, 6, new Variator ("Beta", 0.1, 2.5, 20), 4, false);
			//BufferedImage img = graphLengths (t, 90, 100, new Variator(), 6, true);
			ImageIO.write (img, "png", new File (args[1]));
		} else {
	//		Picture pic = new Picture(SIZE, SIZE); 
			Picture pic = null;
			TSPInstance t;
			char c = args[0].charAt(0);
			if (c <= '9' && c >= '0') {
				t = new TSPInstance(Integer.parseInt (args[0]));
			} else {
				t = new TSPInstance (new File (args[0]));
			}
			NUM_ANTS = Integer.parseInt (args[1]);
			Ant soln = runACO (t, Integer.parseInt (args[2]), pic);
			System.out.println("Path length: " + soln.length);
			/*
			for (State s : soln.path) {
				System.out.println("City #" + s.id + ": " + (TSPState) s);
			}
			*/
		}
	}
}

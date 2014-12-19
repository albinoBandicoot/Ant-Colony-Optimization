import java.awt.image.BufferedImage;
import java.awt.*;
public class Graphing {

	/* Produce a graph of the final path length and number of generations before finding the eventual elite path
	 * on a particular TSP instance while varying one of the parameters (alpha, beta, Q, RHO). The parameter goes
	 * on the X-axis, and the path length and generation count goes on the Y */

	// set yscale to 0 for auto
	public static BufferedImage graphParam (AntColony ac, TSPInstance t, int ng, int nruns, Variator v, int xstep, double yscale) {
		int xsize = xstep * v.nsteps;
		BufferedImage gr = new BufferedImage (xsize, 500, 1);
		Graphics2D g = (Graphics2D) gr.getGraphics();
		g.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor (Color.WHITE);
		g.fillRect (0,0, xsize, 500);

		double[] elens = new double[v.nsteps+2];
		double[] conv_time = new double[v.nsteps+2];
		for (int RUN = 0; RUN < nruns; RUN++) {
			int i = 0;
			while (v.hasNext()) {
				v.advance(ac);
				t.reset(ac);	// clear the pheromones so we can go again
				Ant elite = ac.runACO (t, ng, null, false);
				elens[i] = ac.avglens[ng-1];
				conv_time[i] += findConvTime (ac.elitelens);
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
		g.drawString (AntColony.paramName (v.param) + " from " + v.start + " to " + v.end, xsize - 170, 25);
		int w = 0;
		for (int j=0; j < 4; j++) {
			if (j != v.param) {
				g.drawString (AntColony.paramName (j) + " = " + ac.getParam (j), xsize -170, 45 + w*20);
				w++;
			}
		}
		axes (g, v.start, v.end, v.stepamt, 0, elens[0]*1.1, elens[0]*1.1/5, xsize, 500, AntColony.paramName (v.param), "Stuff");
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
		for (double x = xmin; x < xmax; x += xtick) {
			int xpos = (int) (i * xstep);
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

	public static Color getColor (float d) {
		if (d < 0.5) {
			d *= 2;
			return new Color (1-d, d, 0, 0.6f);
		} else {
			d = (d-0.5f)*2;
			return new Color (0, 1-d, d, 0.6f);
		}
	}

	public static BufferedImage graphLengths (AntColony ac, TSPInstance t, int ng, int nruns, Variator v, int xscale, boolean graph_elites) {
		BufferedImage gr = new BufferedImage (ng*xscale, 500, 1);
		Graphics2D g = (Graphics2D) gr.getGraphics();
		g.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor (Color.WHITE);
		g.fillRect (0,0,ng*xscale,500);

		double ybound = 0;
		while (v.hasNext()) {
			double[] avgavg = new double[ng];
			double[] eliteavg = new double[ng];
			v.advance(ac);
			float d = ((float) v.curstep) / v.nsteps;
			Color col = getColor (d);

			for (int i=0; i < nruns; i++) {
				t.reset(ac);
				ac.runACO (t, ng, null, false);
				for (int j=0; j < ac.avglens.length; j++) {
					avgavg[j] += ac.avglens[j] / nruns;
					eliteavg[j] += ac.elitelens[j] / nruns;
				}
			}
			if (ybound == 0) ybound = ac.avglens[0] * 1.25;
			if (graph_elites) 
				graphArray (g, Color.BLACK, eliteavg, ybound, xscale);
			graphArray (g, col, avgavg, ybound, xscale);
//			t = new TSPInstance(t.states.size());
		}
		axes (g, 0, ng, ng/5, 0, ybound, ybound/5, ng*xscale, 500, "Generations", "Path length");
		return gr;
	}
}


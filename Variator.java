public class Variator {

	public int param;
	public double start;
	public double end;
	public int nsteps;
	public double stepamt;

	public int curstep;

	public static final String[] pnames = {"Alpha", "Beta", "Q", "Rho", "nants"};

	public Variator () {	// this one just goes once without chaging anything
		param = -1;
		curstep = 0;
		nsteps = 1;
	}

	public Variator (String name, double s, double e, int steps) {
		if (name.equals(pnames[0])) param = 0;
		if (name.equals(pnames[1])) param = 1;
		if (name.equals(pnames[2])) param = 2;
		if (name.equals(pnames[3])) param = 3;
		if (name.equals(pnames[4])) param = 4;
		start = s;
		end = e;
		nsteps = steps;
		curstep = 0;
		stepamt = (end - start) / nsteps;
	}

	public void advance () {
		if (param == -1) {
			curstep++;
			return;
		}
		System.out.println("Setting " + pnames[param] + " to " + ((curstep*stepamt) + start));
		if (param == 4 && curstep > 0) {
			AntColony.Q *= ((curstep*stepamt) + start) / ((curstep+1)*stepamt + start);
			System.out.println("Adjusted Q to " + AntColony.Q);
		}
		AntColony.setParam (param, (curstep++)*stepamt + start);
	}

	public boolean hasNext () {
		return curstep < nsteps;
	}

	public void reset () {
		curstep = 0;
	}
}

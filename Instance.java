import java.util.*;
public abstract class Instance {

	public ArrayList<State> states;
	public State initial;

	public abstract ArrayList<State> getViableNeighbors (Ant a) ;

}


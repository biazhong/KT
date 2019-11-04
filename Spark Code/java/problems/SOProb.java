package problems;
import rngstream.*;

/**
 * An abstract class for SO problems.
 *
 */
public abstract class SOProb {
	/**
	 * The simulation result.
	 */
	protected SOAnswer ans;
	/**
	 * Random number stream used.
	 */
	protected RngStream rStream;
	/**
	 * ID of the system
	 */
	protected int sysid;
	/**
	 * Number of replications to simulate in the next run.
	 */
	protected int numReplications;
	
	/**
	 * Setter
	 * @param stream The random number stream to use.
	 */
	private void setStream( RngStream stream ) {
		rStream = stream;
	}
	/**
	 * Setter
	 * @param id The system ID to use.
	 */
	private void setSystem( int id ) {
		sysid = id;
	}
	/**
	 * Setter
	 * @param nRep The number of replications to simulate in the next run.
	 */
	private void setNumReplications( int nRep ) {
		numReplications = nRep;
	}
	/**
	 * Runs simulation given parameters.
	 * 
	 * @param id System ID
	 * @param nRep Number of replications
	 * @param stream Random number stream to use
	 */
	public void runSystem (int id, int nRep, RngStream stream ){
		ans = new SOAnswer();
		setStream( stream );
		setSystem( id );
		setNumReplications ( nRep );
		runSystem();
	}
	public abstract void runSystem();
	/**
	 * Returns the simulation result.
	 * @return The simulation result.
	 */
	public SOAnswer getAns() {
		return ans;
	}	

	/**
	 * 
	 * @param id ID of the system
	 * @param nGroups Number of screening groups used.
	 * @return Screening group ID given system ID and number of groups.
	 */
	public static int get_group( int id, int nGroups ) {
		return id % nGroups;		
	}
}
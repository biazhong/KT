package problems;


/**
 * 		SO problem answer container.
 * 		A class to store and manage SO problem results.
 *
 */
public class SOAnswer {

	/**
	 * Sample size
	 */
	int size;
	int elim, batchSize;
	/**
	 * Estimated mean of objective value.
	 */
	double fn;
	/**
	 * Estimated variance of objective value.
	 */
	double FnVar;
	
	/**
	 * Estimated sum of squared objective value.
	 */
	double FnSumSq;
	/**
	 * A string storing RngStream seeds
	 */
	String seedString;
	/**
	 * Simulation completion time in nanoseconds
	 */
	long simtime; 
	public SOAnswer() {
		size = elim = batchSize = 0;
		fn = FnVar = FnSumSq = 0.;
		seedString = null;
	}
	public long getSimtime() {
		return simtime;
	}
	public void setSimtime(long simtime) {
		this.simtime = simtime;
	}
	public int getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	public String getSeedString() {
		return seedString;
	}
	public void setSeedString(String seedString) {
		this.seedString = seedString;
	}
	public void setElim( int e ) {
		this.elim = e;
	}
	public int getElim() {
		return this.elim;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public double getFn() {
		return fn;
	}
	public void setFn(double fn) {
		this.fn = fn;
	}
	public double getFnVar() {
		return FnVar;
	}
	public void setFnVar(double fnVar) {
		FnVar = fnVar;
	}
	public double getFnSumSq() {
		return FnSumSq;
	}
	public void setFnSumSq(double fnSumSq) {
		FnSumSq = fnSumSq;
	}
	/**
	 * Adds a sample to current result, updating only mean and sample size.
	 * @param _fn The mean of the additional sample.
	 * @param _sz The size of the additional sample.
	 */
	public void addSample(double _fn, int _sz) {
		int newSize=_sz+this.size;
		double newFn = (_sz * _fn + this.size * this.fn)/newSize;
		this.fn = newFn;
		this.size = newSize;
	}
	
	void clear() {
		size = elim = batchSize = 0;
		fn = FnVar = FnSumSq = 0.;
		seedString = "";
	}
}
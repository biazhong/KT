package problems;
/**
*
* Implements TpMax (Throughput Maximization) test problem.
*
*/
public class TpMax extends SOProb {

	/**
	 * Parameter RB determines size of the problem.
	 */
	int RB = 20;
	
	/**
	 * Variance of the Log-normal distribution used to randomize burn-in.
	 * Set to -1 to fix burn-in to the default value of 2000.
	 */
	double burnin_param = -1;

	/**
	 * 
	 * @param s Converted to RB.
	 * @param cov Converted to burnin_param.
	 */
	public TpMax(String s, String cov) {
		RB = Integer.parseInt(s);
		burnin_param = Double.parseDouble(cov);
	}
	
	/**
	 * Calculates the number of systems under the given parameter.
	 * 
	 * @param param Parameter RB.
	 * @return The number of systems with R=B=param.
	 */
	public static long getNumSystems(String param) {
		long _RB = Long.parseLong(param);
		return (_RB - 1) * (_RB - 2) / 2 * (_RB - 1);  
	}
	
	/**
	 * Decision variables.
	 */
	int[] x_disc;

	/**
	 * Converts a system ID to the array of decision variables and store in
	 * {@link #x_disc} 
	 */
	private void Idx2System_Disc() {
		//finds the system corresponding to the index
		x_disc = new int [5];
		
		int rr = RB * 2 - 3;
		int n  = sysid / (RB-1) + 1;
		x_disc[3] = sysid % (RB-1) + 1;
		x_disc[4] = RB - x_disc[3];
		x_disc[0] = (int) Math.ceil( ( (double)rr - Math.sqrt( (double) rr*rr-8.*n) )  / 2);
		x_disc[1] = RB - 1 + (n - (rr-x_disc[0]) * x_disc[0] /2) - x_disc[0];
		x_disc[2] = RB - x_disc[0] - x_disc[1];
		
	}

	/**
	 * Simulate the throughput maximization problem.
	 */
	public void runSystem() {
		this.Idx2System_Disc();
		long t0 = System.nanoTime();

		int i,j; double t;

		//Matrix<double> ServiceT  (param->nstages, param->njobs+1, 0.0);
		int njobs = 2050;
		int nstages = 3;
		int burnin= 2000;	//number of burn-in jobs
		
		int[] burnins = null;
		if(burnin_param > 0) {
			 
			//lognormal
			double mu = Math.log(burnin) - burnin_param / 2.;
			double sig = Math.sqrt(burnin_param);			
			burnins = new int[numReplications];
			for (i = 0; i < numReplications; ++i)  {
				int v = (int) Math.exp( rStream.RandNormal(0., 1.) * sig + mu );
				burnins[i] = (v > 20000) ? 20000:v;
			}
			
		}
		int _njobs, _burnin;
		int _diff = njobs - burnin;
		int _nstages = nstages;

		//decompose the vector of discrete decision variables, x_disc, into two
		//vectors representing the solution
		int[] r = { x_disc[0], x_disc[1], x_disc[2] }; 	//rates of service times
		int[] b = { x_disc[3], x_disc[4] }; 	  		//buffer spaces before stage i

		


		double[] tp = new double[numReplications];
		double tp_total = 0.0;
		this.ans.FnSumSq = 0.0;
		this.ans.setSize(numReplications);
		// create random number stream
		//	SORngStream ServiceStream;

		for (int k=0; k<numReplications; ++k) {
			_burnin = (burnin_param > 0) ? burnins[k] : burnin;
			_njobs = _burnin + _diff;
			double[][] sTime = new double [_njobs][_nstages];
			double[][] ExitTimes = new double [_nstages][_njobs+1];
			//			Matrix<double> ExitTimes (param->_nstages, param->njobs+1, 0.0);
			//matrix with the time at which each job leaves stage i (row i).
			// generate random data

			//		pStream->ResetNextSubstream();
			for (i=0;i<_njobs;i++) {
				for (j=0;j<_nstages;j++) {
					sTime[i][j]=rStream.RandExponential((double) r[j]);
				}
			}

			////////////////////////////////////
			// done generating random numbers
			///////////////////////////////////

			///////////////////////
			// begin simulation
			///////////////////////

			for (i=1; i <= _njobs; ++i) {
				t = sTime[i-1][0];
				//First stage(indexed 0): if time at which the i--b(0)th job left stage 1 is less
				//than the time at which this job is ready to leave stage 0 (there is
				//buffer space available) job leaves stage 0 as soon as it is ready.
				if (ExitTimes[1][Math.max(0,i-b[0])] <= ExitTimes[0][i-1]+t) {
					ExitTimes[0][i] = ExitTimes[0][i-1]+t;
				}else {
					//No buffer space available, job leaves as soon as buffer space is
					//available.
					ExitTimes[0][i] = ExitTimes[1][Math.max(0,i-b[0])];
				}

				for (j=2; j<=_nstages-1; j++) {
					t = sTime[i-1][j-1];
					//if (time at which previous order left > time this order left
					//previous stage)Then there must be a queue
					if(ExitTimes[j-1][i-1]>ExitTimes[j-2][i]) {
						//if there is buffer space available
						if(ExitTimes[j][Math.max(0,i-b[j-1])] <= ExitTimes[j-1][i-1]+t) {
							ExitTimes[j-1][i] = ExitTimes[j-1][i-1]+t;
						} else {
							//No buffer space available, job leaves as soon as buffer space is available.
							ExitTimes[j-1][i] = ExitTimes[j][Math.max(0,i-b[j-1])];
						}
					} else {
						//There is no queue, job starts to be worked on as soon as it leaves previous stage.
						if (ExitTimes[j][Math.max(0,i-b[j-1])] <= ExitTimes[j-2][i]+t) {
							//If there is buffer space available, job leaves as soon as ready
							ExitTimes[j-1][i] = ExitTimes[j-2][i]+t;
						} else{
							//No buffer space available, job leaves as soon as buffer space is available.
							ExitTimes[j-1][i] = ExitTimes[j][Math.max(0,i-b[j-1])];
						}
					}
				}

				//last stage: all jobs leave as soon as ready
				t=sTime[i-1][_nstages-1];
				//if there is not a queue (i.e. previous job finished before current job leaves previous stage)
				if (ExitTimes[_nstages-1][i-1] <= ExitTimes[_nstages-2][i]) {
					//leaves as soon as ready, entered to be worked on as soon as finished in previous stage
					ExitTimes[_nstages-1][i] = ExitTimes[_nstages-2][i]+t;
				} else {
					//Entered to be worked on after previous job was finished, leaves as soon as ready.
					ExitTimes[_nstages-1][i] = ExitTimes[_nstages-1][i-1]+t;
				}
			}// end for loop of the ith job
			tp[k] = ((double) (_njobs-_burnin))/(ExitTimes[_nstages-1][_njobs]-ExitTimes[_nstages-1][_burnin]);
			tp_total += tp[k];
			this.ans.FnSumSq += tp[k]*tp[k];
		}
		
		
		this.ans.simtime = Math.max(2, System.nanoTime()-t0);

		this.ans.fn = tp_total/numReplications;
		this.ans.FnVar = 0.0;
		for (int k=0; k<numReplications; k++)  this.ans.FnVar += Math.pow(tp[k]-this.ans.fn,2);
		if(numReplications > 1) {
			this.ans.FnVar = this.ans.FnVar / (numReplications-1);
		}else {
			this.ans.FnVar = 0.0;
		}

	}
}
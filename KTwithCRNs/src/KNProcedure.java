import java.util.ArrayList;
import java.util.Random;

public class KNProcedure {
	private double rho;
	private double alpha;
	private double delta;
	private double sigma;
	private ArrayList<Double> mu = new ArrayList<Double>();
	private int n0;
	
	private Random R = new Random();
	public KNProcedure(){
		this.rho = 0d;
		this.alpha = 0.05;
		this.delta = 0.1;
		this.sigma = 1;
		this.mu.add(0.1);
		this.mu.add(0.0);
		this.n0 = 20;
	}
	
	public KNProcedure(double rho,double alpha, double delta, double sigma, ArrayList<Double> mu, int n0){
		this.rho = rho;
		this.alpha = alpha;
		this.delta = delta;
		this.sigma = sigma;
		this.mu.addAll(mu);
		this.n0 = n0;
	}
	
	public ArrayList<Double> getResult(){
		ArrayList<Double> finalResults = new ArrayList<Double>();
		finalResults.add(0.0); //Total Sample Size;
		finalResults.add(-1.0); //Best Alt
		
		if(mu.size()==1) {
			finalResults.set(1, 0.0);
			return finalResults;
		}
		
		
		double[][] A = { {1,rho},
				{rho,1}
               };
		double[][] L = Cholesky.cholesky(A);
		
		
		ArrayList<Double> AVG = new ArrayList<Double>();
		
		double difAVG =0d;
		double S2 = 0d;
		
		AVG.add(0d);
		AVG.add(0d);
		
		
		for(int n=0;n<n0;n++) {
			double[] rand = new double[2];
        	rand[0] = R.nextGaussian();
        	rand[1]= R.nextGaussian();
        	double[] vec = new double[2];
        	
        	for(int i=0;i<2;i++) {
        		vec[i]=0;
        		for(int j=0;j<2;j++) {
        			vec[i] = vec[i]+L[i][j]*rand[j];
        		}
        		vec[i]=vec[i]+mu.get(i);
        	}
        	
        	AVG.set(0, AVG.get(0)+vec[0]);
        	AVG.set(1, AVG.get(1)+vec[1]);
        	
        	S2 = S2+(vec[0]-vec[1])*(vec[0]-vec[1]);
        	difAVG = difAVG + vec[0]-vec[1];
		}
		AVG.set(0, AVG.get(0)/n0);
		AVG.set(1, AVG.get(1)/n0);
		S2 = (S2 - difAVG*difAVG/n0)/(n0-1);
		
		
		double h2 = (n0-1)*(Math.pow(2*alpha, -2.0/(n0-1))-1);
		
		int N = (int)(Math.floor(h2*S2)/(delta*delta));
		
		int t = n0;
		
		boolean termination = false;
		
		while(termination==false) {
			double[] rand = new double[2];
        	rand[0] = R.nextGaussian();
        	rand[1]= R.nextGaussian();
        	double[] vec = new double[2];
        	
        	for(int i=0;i<2;i++) {
        		vec[i]=0;
        		for(int j=0;j<2;j++) {
        			vec[i] = vec[i]+L[i][j]*rand[j];
        		}
        		vec[i]=vec[i]+mu.get(i);
        	}
        	
        	AVG.set(0, (AVG.get(0)*t+vec[0])/(t+1));
        	AVG.set(1, (AVG.get(1)*t+vec[1])/(t+1));
        	t++;
        	int maxAVGIndex = main.getMaxIndex(AVG);
			if(t>N){
				finalResults.set(0, 2.0*t);
				finalResults.set(1, maxAVGIndex*1.0);
				termination = true;
				
			}else{
				if(t*(AVG.get(0)-AVG.get(1))<-h2*S2/(2*delta)+delta*t/2) {
					finalResults.set(0, 2.0*t);
					finalResults.set(1, 1.0);
					termination = true;
				}else if(t*(AVG.get(1)-AVG.get(0))<-h2*S2/(2*delta)+delta*t/2) {
					finalResults.set(0, 2.0*t);
					finalResults.set(1, 0.0);
					termination = true;
				}
			}
		}
		return finalResults;
	}
}

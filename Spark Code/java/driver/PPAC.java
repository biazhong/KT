package driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import problems.SOAnswer;
import problems.SOProb;
import problems.TpMax;
import rngstream.RngStream;

public class PPAC {
double alpha,delta;
	
	String param,cov;
	
	int n1;
	
	Map<Integer, SOAnswer> inGroupContainer;
	
	int bestID;
	SOAnswer bestAns;
	
	int totalSample=0;
	
	long simulationTime = (long)0.0;
	long compareTime = (long)0.0;
	
	
	public PPAC() {
		this.alpha = 0.05;
		this.delta = 0.1;
		this.param = "20";
		this.cov ="0.2";
		this.n1 = 10;
	}
	public PPAC(double alpha, double delta, String param, String cov, int n1, Map<Integer, SOAnswer> inGroupContainer) {
		this.alpha = alpha;
		this.delta = delta;
		this.param = param;
		this.cov = cov;
		this.n1 = n1;
		this.inGroupContainer = inGroupContainer;
	}
	
	public int getBestID(){
		return bestID;
	}
	
	public SOAnswer getBestAnswer(){
		return bestAns;
	}
	
	public int getTotalSample() {
		return totalSample;
	}
	
	
	public long getSimTime() {
		return simulationTime;
	}
	
	public long getCompareTime() {
		return compareTime;
	}
	
	public void runSystem() {
		List<Integer> sysIDs = new ArrayList<Integer>(inGroupContainer.keySet());
		List<Integer> I = new ArrayList<Integer>();
		for(int i=0 ; i < sysIDs.size(); i++){
			I.add(i);
		}
		if(sysIDs.size()==1){
			bestID = sysIDs.get(0);
			bestAns = inGroupContainer.get(sysIDs.get(0));
			totalSample = 0;
		}else{
			long [] seed = new long[6];
			Random R = new Random();
			
			double getSeed = R.nextDouble()*1000000000;
			getSeed = Math.ceil(getSeed);
			
			for(int m=0; m<6; m++) seed[m]=(long)getSeed;
			
			for(int i=0;i < sysIDs.size();i++){
				inGroupContainer.get(sysIDs.get(i)).setSeedString(RngStream.SeedToStr(seed));
			}
			
			double[][] S2 = new double[sysIDs.size()][sysIDs.size()];
			double[][] diffAVG = new double[sysIDs.size()][sysIDs.size()];
			
			for(int n = 0; n < n1; n++){
				double[] vec = new double[sysIDs.size()];
				
				for(int i=0; i < sysIDs.size(); i++){
					SOProb prob = new TpMax(param,cov);
					RngStream rStream = new RngStream();
					rStream.setSeed(RngStream.StrToSeed(inGroupContainer.get(sysIDs.get(i)).getSeedString()));
					
					long _tt = System.currentTimeMillis();
					prob.runSystem(sysIDs.get(i),1,rStream);
					simulationTime =  simulationTime +System.currentTimeMillis()-_tt;
					
					
					SOAnswer ans = prob.getAns();
					vec[i]=ans.getFn();
					
					long[] tempSeed = new long[6];
					double[] state = rStream.getState();
					for(int m=0; m < 6; m++) tempSeed[m]=(long)state[m];
					
					inGroupContainer.get(sysIDs.get(i)).setSeedString(RngStream.SeedToStr(tempSeed));
					inGroupContainer.get(sysIDs.get(i)).addSample(ans.getFn(), ans.getSize());
					
					for(int j=0; j < i; j++){
						S2[i][j] = S2[i][j] + (vec[i]-vec[j])*(vec[i]-vec[j]);
						S2[j][i] = S2[i][j];
						
						diffAVG[i][j] = diffAVG[i][j] + vec[i] - vec[j];
						diffAVG[j][i] = -1d*diffAVG[i][j];
					}
				}
			}
			double maxS2 = -1.0;
			for(int i = 0; i < sysIDs.size();i++){
				for(int j=0; j < i; j++){
					S2[i][j] = (S2[i][j] - diffAVG[i][j]*diffAVG[i][j]/n1)/(n1-1);
					S2[j][i] = S2[i][j];
					if(S2[i][j]>maxS2) {
						maxS2 = S2[i][j];
					}
				}
			}
			double lambda = delta/2.0;
			double h2 = (n1-1)*(Math.pow(alpha/(sysIDs.size()-1), -2.0/(n1-1))-1)/(4*(delta-lambda));
			
			int t = n1;
			
			while(sysIDs.size()>1){
				for (int i=0; i<sysIDs.size(); i++){
					SOProb prob =  new TpMax(param,cov);
					RngStream rStream = new RngStream();
					rStream.setSeed(RngStream.StrToSeed(inGroupContainer.get(sysIDs.get(i)).getSeedString()));
					
					long _tt = System.currentTimeMillis();
					prob.runSystem(sysIDs.get(i),1,rStream);
					simulationTime = simulationTime + System.currentTimeMillis()-_tt;
					
					SOAnswer ans = prob.getAns();
					
					long[] tempSeed = new long[6];
					double[] state = rStream.getState();
					for(int m=0; m < 6; m++) tempSeed[m]=(long)state[m];
					
					inGroupContainer.get(sysIDs.get(i)).setSeedString(RngStream.SeedToStr(tempSeed));
					inGroupContainer.get(sysIDs.get(i)).addSample(ans.getFn(), ans.getSize());
				}
				t++;
				
				long _tt = System.currentTimeMillis();
				for(int i=0; i < sysIDs.size();i++){
					boolean eliCheck = false;
					for(int j=i+1;j<sysIDs.size();j++){
						double tempS = S2[I.get(i)][I.get(j)];
						int N = (int)(Math.floor((h2*maxS2)/(lambda)));
						if(t>N){
							if(inGroupContainer.get(sysIDs.get(i)).getFn()>inGroupContainer.get(sysIDs.get(j)).getFn()){
								totalSample = totalSample + t;
								I.remove(j);
								sysIDs.remove(j);
								j--;
							}else{
								eliCheck=true;
							}
						}else{
							double diff = inGroupContainer.get(sysIDs.get(i)).getFn()-inGroupContainer.get(sysIDs.get(j)).getFn();
							if(t*diff<-h2*tempS-(delta-lambda)*t){
								eliCheck=true;
							}
							if(t*diff > h2*tempS+(delta-lambda)*t){
								totalSample = totalSample + t;
								I.remove(j);
								sysIDs.remove(j);
								j--;
							}
						}
					}
					if(eliCheck == true){
						totalSample = totalSample + t;
						I.remove(i);
						sysIDs.remove(i);
						i--;
					}
				}
				compareTime = compareTime + System.currentTimeMillis() - _tt;
			}
			bestID = sysIDs.get(0);
			bestAns = inGroupContainer.get(sysIDs.get(0));
			totalSample = totalSample + t;
		}
	}
}
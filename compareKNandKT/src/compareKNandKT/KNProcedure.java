package compareKNandKT;

import java.util.ArrayList;
import java.util.Random;

public class KNProcedure {
	private int k;
	private double alpha;
	private double delta;
	private double sigma;
	private ArrayList<Double> mu = new ArrayList<Double>();
	private Random R = new Random();
	public KNProcedure(){
		this.k = 2;
		this.alpha = 0.05;
		this.delta = 0.1;
		this.sigma = 1;
		this.mu.add(0.1);
		this.mu.add(0.0);
	}
	
	public KNProcedure(int k, double alpha, double delta, double sigma, ArrayList<Double> mu){
		this.k = k;
		this.alpha = alpha;
		this.delta = delta;
		this.sigma = sigma;
		this.mu.addAll(mu);
	}
	
	public ArrayList<Double> getResult(){
		ArrayList<Double> finalResults = new ArrayList<Double>();
		finalResults.add(0.0);
		finalResults.add(-1.0);
		
		ArrayList<Integer> I = new ArrayList<Integer>();
		ArrayList<Double> AVG = new ArrayList<Double>();
		double a = 2*sigma*sigma*Math.log((k-1)/(2*alpha))/delta;
		int N =(int)(Math.floor(2*a/delta)+1);
		for(int i = 0;i<k;i++){
			I.add(i);
		//	System.out.println(i);
			AVG.add(0.0);
		}
		int t = 0;
		while(I.size()>1){
			
			for(int i =0; i < I.size();i++){
				AVG.set(i, (AVG.get(i)*t+R.nextGaussian()*sigma+mu.get(I.get(i)))/(t*1.0+1));
			}
			t++;
			int maxAVGIndex = main.getMaxIndex(AVG);
			if(t>N){
				finalResults.set(0, finalResults.get(0)+I.size()*t);
				finalResults.set(1, I.get(maxAVGIndex)*1.0);
				return finalResults;
			}else{
				int tempI = I.get(0);
				double tempAVG = AVG.get(0);
				I.set(0, I.get(maxAVGIndex));
				AVG.set(0, AVG.get(maxAVGIndex));
				
				I.set(maxAVGIndex, tempI);
				AVG.set(maxAVGIndex, tempAVG);
				
				for(int i = 1; i < I.size();i++){
					if(t*(AVG.get(0)-AVG.get(i))>a-t*delta/2){
						I.set(i, I.get(I.size()-1));
						I.remove(I.size()-1);
						AVG.set(i, AVG.get(AVG.size()-1));
						AVG.remove(AVG.size()-1);
						
						i--;
						finalResults.set(0, finalResults.get(0)+t);
					}
				}
			}
		}
		finalResults.set(0, finalResults.get(0)+I.size()*t);
		finalResults.set(1,I.get(0)*1.0);
		return finalResults;
	}
}

package compareKNandKT;
import java.util.ArrayList;

public class selectionOfC {
	private int k;
	private int C;
	private double alpha;
	private double delta;
	private double sigma;
	private ArrayList<Double> mu = new ArrayList<Double>();
	
	public selectionOfC(){
		this.k = 2;
		this.C = 2;
		this.alpha = 0.05;
		this.delta = 0.1;
		this.sigma = 1;
		this.mu.add(0.1);
		this.mu.add(0.0);
	}
	
	public selectionOfC(int k, int C, double alpha, double delta, double sigma, ArrayList<Double> mu){
		this.k = k;
		this.C = C;
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
		for(int i = 0;i<k;i++){
			I.add(i);
		}
		
		int round = 1;
		while(I.size()>1){
			ArrayList<Integer> tempI = new ArrayList<Integer>();
			ArrayList<Double> tempMu = new ArrayList<Double>();
			int COUNT = (int)(Math.ceil(Math.log(k)/Math.log(C)));
			
			
			int numOfGroup = (int)(Math.ceil(I.size()*1.0/C));
			int stopPoint = 0;

			double alpha_r= alpha/Math.pow(2.0, round*1.0);
			
			for(int i = 0; i < numOfGroup;i++){
				ArrayList<Integer> groupI = new ArrayList<Integer>();
				ArrayList<Double> groupMu = new ArrayList<Double>();
				
				if(i < numOfGroup-1){
					stopPoint = (i+1)*C;
				}else{
					stopPoint = I.size();
				}
				//System.out.println(I.size());
				for(int j = i*C;j<stopPoint;j++){
					groupI.add(I.get(j));
					groupMu.add(mu.get(j));
				}
				
				
				KNProcedure y = new KNProcedure(groupI.size(), alpha_r, delta, sigma, groupMu);
				
				
				ArrayList<Double> indResults = new ArrayList<Double>();
				indResults.addAll(y.getResult());
				
				finalResults.set(0, finalResults.get(0)+indResults.get(0));
				tempI.add(groupI.get(indResults.get(1).intValue()));
				tempMu.add(groupMu.get(indResults.get(1).intValue()));
			}
			I.clear();
			mu.clear();
			I.addAll(tempI);
			mu.addAll(tempMu);
			round++;
		}
		finalResults.set(1, I.get(0)*1.0);
		return finalResults;
		
		
	}
	
	
}
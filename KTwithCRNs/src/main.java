import java.util.ArrayList;
import java.util.Random;
public class main {
	public static void main(String[] args){
		int repeatTime = 1000;
		double averageT = 0;
		double correctness = 0;
		for(int count = 0; count<repeatTime;count++){
			int k = 100000;
			double alpha = 0.05;
			double delta = 0.1;
			double sigma = 1;	
			double rho = 0.0;
			int n0 = 20;
			
			//Generate Means 
			Random R1 = new Random();
			ArrayList<Double> mu = new ArrayList<Double>();
			
			for(int i =0; i < k;i++){
			
				if(i == k-1){
					mu.add(16*delta);
				}else{
					mu.add(Math.ceil(R1.nextDouble()*15)*delta);
				}
			}
			int bestAlt = getMaxIndex(mu);
			
			
			
			//Select The Best Alternative
			selectionOfTwo y = new selectionOfTwo(k,rho, alpha, delta, sigma, mu,n0);
			ArrayList<Double> tempResults = new ArrayList<Double>();
			tempResults.addAll(y.getResult());
			if(tempResults.get(1)==bestAlt){
				correctness ++;
			}
			averageT = averageT+tempResults.get(0);
			System.out.println(count+" "+bestAlt+" "+tempResults.get(1)+" "+tempResults.get(0));
		}
		System.out.println(correctness/repeatTime+" "+averageT/repeatTime);
	}
	
	public static int getMaxIndex(ArrayList<Double> array){
		int maxIndex = 0;
		double max = array.get(0);
		for(int i = 1; i<array.size();i++){
			if(array.get(i)>max){
				max = array.get(i);
				maxIndex = i;
			}
		}
		return maxIndex;
	}
}
package driver;
import java.io.IOException;

import problems.SOProb;
import problems.TpMax;
import rngstream.RngStream;
public class tset {
	public static void main(String[] args) throws Exception{
		int n1 = 20;
		double delta = 0.1;
		double rinottH = Rinott.rinott(300000,0.975, n1-1);
		System.out.println(rinottH);
		SOProb prob = new TpMax("128","-0.1");
		long [] seed = new long[6];
		for(int m=0; m<6; m++) seed[m]=(long)19231.0;
		RngStream rStream = new RngStream();
		rStream.setSeed(seed);
		prob.runSystem(568134, n1, rStream);
		double rinottSampleSize = Math.ceil(prob.getAns().getFnVar()*Math.pow(rinottH/(delta),2));
		
		System.out.println(rinottSampleSize);
		
	}
}

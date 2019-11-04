package driver;


public class Rinott {

	public static double ZCDF(double x)
	{
		int neg = (x < 0d) ? 1 : 0;
		if ( neg == 1) 
			x *= -1d;

		double k = (1d / ( 1d + 0.2316419 * x));
		double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
				k - 0.356563782) * k + 0.319381530) * k;
		y = 1.0 - 0.3989422803   * Math.exp(-0.5 * x * x) * y;

		return (1 - neg) * y + neg * (1d - y);
	}

	
	public static double CHIPDF(int N, double C, double[] lngam) {
		double FLN2 = ((double) N) / 2.;
		double TMP = -FLN2 * Math.log(2d) - lngam[N-1] + (FLN2 - 1d) * Math.log(C) - C/2.;
		return Math.exp(TMP);
	}

	/**
	 * Computes the Rinott Constant
	 * @param T Number of systems
	 * @param PSTAR 1 - alpha, i.e. Power of the test. Usually set to 0.95
	 * @param NU Stage-0 sample size - 1
	 * @return The Rinott Constant
	 */
	public static double rinott(long T, double PSTAR,  int NU) {
		double LNGAM[] = new double[50];
		NU = Math.min(NU, 50);
		double WEX[] = new double [32];
		double W[] = {.10921834195238497114,  
				.21044310793881323294,
				.23521322966984800539,     
				.19590333597288104341,
				.12998378628607176061,     
				.70578623865717441560E-1,
				.31760912509175070306E-1,  
				.11918214834838557057E-1,
				.37388162946115247897E-2,  
				.98080330661495513223E-3,
				.21486491880136418802E-3,  
				.39203419679879472043E-4,
				.59345416128686328784E-5,  
				.74164045786675522191E-6,
				.76045678791207814811E-7,  
				.63506022266258067424E-8,
				.42813829710409288788E-9,  
				.23058994918913360793E-10,
				.97993792887270940633E-12, 
				.32378016577292664623E-13,
				.81718234434207194332E-15, 
				.15421338333938233722E-16,
				.21197922901636186120E-18, 
				.20544296737880454267E-20,
				.13469825866373951558E-22, 
				.56612941303973593711E-25,
				.14185605454630369059E-27, 
				.19133754944542243094E-30,
				.11922487600982223565E-33, 
				.26715112192401369860E-37,
				.13386169421062562827E-41, 
				.45105361938989742322E-47				
		};
		double X[] = {.44489365833267018419E-1,  
				.23452610951961853745,
				.57688462930188642649,    
				.10724487538178176330E1,
				.17224087764446454411E1,  
				.25283367064257948811E1,
				.34922132730219944896E1,  
				.46164567697497673878E1,
				.59039585041742439466E1,  
				.73581267331862411132E1,
				.89829409242125961034E1,  
				.10783018632539972068E2,
				.12763697986742725115E2,  
				.14931139755522557320E2,
				.17292454336715314789E2,  
				.19855860940336054740E2,
				.22630889013196774489E2,  
				.25628636022459247767E2,
				.28862101816323474744E2,  
				.32346629153964737003E2,
				.36100494805751973804E2,  
				.40145719771539441536E2,
				.44509207995754937976E2,  
				.49224394987308639177E2,
				.54333721333396907333E2,  
				.59892509162134018196E2,
				.65975377287935052797E2,  
				.72687628090662708639E2,
				.80187446977913523067E2,  
				.88735340417892398689E2,
				.98829542868283972559E2,  
				.11175139809793769521E3};
		for (int I = 1; I <= 32; ++I) {
			WEX[I-1] = W[I-1] * Math.exp(X[I-1]);
		}
		LNGAM[0] = 0.5723649429;
		LNGAM[1] = 0.0;
		for (int i = 2; i <= 25; ++i) {
			LNGAM[2*i - 2] = Math.log(i - 1.5) + LNGAM[2*i - 4];
			LNGAM[2*i - 1] = Math.log(i - 1.0) + LNGAM[2*i - 3];
		}
		double DUMMY = 1.0;

		double H = 4.0;
		double LOWERH = 0.0;
		double UPPERH = 20.00;
		double ANS = 0.0, TMP = 0.0;
		for (int LOOPH = 1; LOOPH <= 50; ++LOOPH) {
			ANS = 0.0;
			for  (int J = 1; J <= 32; ++J) {
				TMP = 0.0;
				for (int I = 1; I <= 32; ++I) {
					TMP += WEX[I-1] 
							* ZCDF( H / Math.sqrt(NU * (1d / X[I-1]+1d/X[J-1] ) / DUMMY )) 
							* CHIPDF( NU, DUMMY * X[I-1], LNGAM ) * DUMMY ;
				}
				TMP = Math.pow(TMP, T-1);
				ANS = ANS + WEX[J-1] * TMP * CHIPDF(NU, DUMMY * X[J-1], LNGAM) * DUMMY;
			}
			if (Math.abs(ANS - PSTAR) <= 0.000001) {
				return H;
			} else if (ANS > PSTAR) {
				UPPERH = H;
				H = (LOWERH + UPPERH)/2.0d;
			} else {
				LOWERH = H;
				H = (LOWERH + UPPERH)/2.0d;
			}
		}
		return H;
	}
}
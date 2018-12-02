package driver;

import java.io.IOException;
import java.util.*;

import rngstream.*;
import problems.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;



public class mainRNCRNC{
	enum Counters {
		MAP_TIME, REDUCE_TIME,TOTAL_SAMPLE,SIM_TIME,COMPARE_TIME,BEST_ID;
	}
	
	
	public static class knockoutMapper extends Mapper<LongWritable, Text, IntWritable, Text>{
		
		//private int globalseed;
		private int nGroups;
		
		private Configuration conf;
		
		
		
		
		
		
		
		public void setup(Context context) throws IOException, InterruptedException{
			conf = context.getConfiguration();
		//	globalseed = conf.getInt("seed", 14853);
			nGroups = conf.getInt("nGroups", 4);
		}
		
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			 long _tt = System.currentTimeMillis();
				
				
			 List<String> tokenList = new ArrayList<String>();
			 String line=value.toString();
			 StringTokenizer itr = new StringTokenizer(line);
		     
			 while (itr.hasMoreTokens()) {
		    	 tokenList.add(itr.nextToken());
		     }
			
			 
			 int sysid = Integer.parseInt(tokenList.get(0));
		//	 long [] seed = null;
		//	 if(tokenList.size() >= 2) {
		//		 seed = RngStream.StrToSeed(tokenList.get(1));
		//	 }else {
		//		 seed = new long[6];
		//		 for(int i = 0; i < 6; i++) {
		//			 seed[i]=(long)sysid+globalseed;
		//		 }
		//	 }
			 int groupID = sysid % nGroups;
				
			 String outputStr = sysid+ "";//+RngStream.SeedToStr(seed);
			 
			 context.write(new IntWritable(groupID),new Text(outputStr));
			 _tt = System.currentTimeMillis()-_tt;
			 
			 context.getCounter(Counters.MAP_TIME).increment(_tt);
			 
		}
	}
	public static class knockoutReducer extends Reducer<IntWritable,Text,IntWritable,Text> {	
		private double alpha,delta;
		private String param,cov;
		private int n1, nSysInGroup,nGroups,startingRound;
		private int stageSampleSize = 0;
		private Configuration conf;
	
		
		
		
		
		public void setup(Context context) throws IOException, InterruptedException{
			conf = context.getConfiguration();
			
			alpha = conf.getDouble("alpha", 0.05);
			delta = conf.getDouble("delta", 0.1);
			
			param = conf.get("param");
			cov = conf.get("cov");
			
			n1 = conf.getInt("n1", 10);
			nSysInGroup = conf.getInt("nSysInGroup", 5);
			nGroups = conf.getInt("nGroups",4);
			startingRound = conf.getInt("startingRound", 5);
		}
		
		
		public void reduce(IntWritable key, Iterable<Text> values, Context context) 
				throws IOException, InterruptedException {
			long _tt = System.currentTimeMillis();
			long _simTime = (long)0.0;
			long _comTime = (long)0.0;
			
			Map<Integer, SOAnswer> sysContainer = new HashMap<Integer, SOAnswer>();
			
			
			
			
			for(Text value:values) {
				List<String> tokenList = new ArrayList<String>();
				String line = value.toString();
				StringTokenizer tokenizer = new StringTokenizer(line);
				
				SOAnswer ans = new SOAnswer();
				
				while (tokenizer.hasMoreTokens()) {
					tokenList.add(tokenizer.nextToken());
				}				
				//	ans.setSeedString(tokenList.get(1));
				sysContainer.put(Integer.parseInt(tokenList.get(0)), ans);
				
			}
			
			int round = 1;
			RngStream unif = new RngStream();
			long[] seed = new long[6];
			for(int i = 0 ; i < 6; i++) {
				seed[i]=1264;
			}
			unif.setSeed(seed);
			
			List<Integer> surIDs = new ArrayList<Integer>(sysContainer.keySet()); 
			
			while(surIDs.size()>1) {
				
				int numOfComGroups  = sysContainer.size()/nSysInGroup+1;
				Map<Integer, SOAnswer> surviveContainer = new HashMap<Integer, SOAnswer>();
				
				for (int i = 1; i < numOfComGroups;i++){
					Map<Integer, SOAnswer> inGroupContainer = new HashMap<Integer, SOAnswer>();
					for(int j = 0; j < nSysInGroup;j++) {
						
						int sample  = (int) Math.ceil(unif.randU01()*sysContainer.size())-1;
						inGroupContainer.put(surIDs.get(sample), sysContainer.get(surIDs.get(sample)));
						sysContainer.remove(surIDs.get(sample));
						surIDs.remove(sample);
					}
					
					KN kn = new KN(alpha/Math.pow(2, round),delta,param,cov,n1,inGroupContainer);

					
					kn.runSystem();
					_simTime = _simTime+kn.getSimTime();
					_comTime = _comTime+kn.getCompareTime();
					stageSampleSize = stageSampleSize + kn.getTotalSample();
					
					SOAnswer tempAns = new SOAnswer();
					tempAns.setSeedString(kn.getBestAnswer().getSeedString());
					surviveContainer.put(kn.getBestID(), tempAns);
				}
				if(!sysContainer.isEmpty()) {
					KN kn = new KN(alpha/Math.pow(2, round),delta,param,cov,n1,sysContainer);
					kn.runSystem();
					_simTime = _simTime+kn.getSimTime();
					_comTime = _comTime+kn.getCompareTime();
					
					
					stageSampleSize = stageSampleSize + kn.getTotalSample();
					SOAnswer tempAns = new SOAnswer();
					tempAns.setSeedString(kn.getBestAnswer().getSeedString());
					surviveContainer.put(kn.getBestID(), tempAns);
				}
				sysContainer.clear();
				sysContainer.putAll(surviveContainer);
				surIDs.clear();
				surIDs.addAll(sysContainer.keySet());
				round++;
			}
			////////////////////////////
			
	
			
			List<Integer> bestID = new ArrayList<Integer>(sysContainer.keySet());
			
			
			SOProb prob = new TpMax(param,cov);
			RngStream rStream = new RngStream();
			rStream.setSeed(RngStream.StrToSeed(sysContainer.get(bestID.get(0)).getSeedString()));
			
			long _t = System.currentTimeMillis();
			prob.runSystem(bestID.get(0), n1, rStream);
			
			_simTime=_simTime+System.currentTimeMillis()-_t;
			
			
			
			sysContainer.replace(bestID.get(0),prob.getAns());
			double rinottH = Rinott.rinott(nGroups, 1-alpha/Math.pow(2, startingRound), n1-1);
			int rinottSampleSize =(int) Math.ceil(sysContainer.get(bestID.get(0)).getFnVar()*Math.pow(rinottH/delta,2))-n1;
			if (rinottSampleSize< 0) rinottSampleSize = 0;
			
			
			
			SOProb probNext = new TpMax(param,cov);
			_t = System.currentTimeMillis();
			
			
			
			probNext.runSystem(bestID.get(0), rinottSampleSize, rStream);
			_simTime=_simTime+System.currentTimeMillis()-_t;
			stageSampleSize = stageSampleSize+n1+rinottSampleSize;
			
			
			sysContainer.get(bestID.get(0)).addSample(probNext.getAns().getFn(), rinottSampleSize);
			
			
			
			
			
			
			String outputStr =sysContainer.get(bestID.get(0)).getFn()+" "+rinottSampleSize;
			context.write(new IntWritable(bestID.get(0)),new Text(outputStr));
			_tt = System.currentTimeMillis() - _tt;	
			context.getCounter(Counters.TOTAL_SAMPLE).increment(stageSampleSize);
			context.getCounter(Counters.REDUCE_TIME).increment(_tt);
			context.getCounter(Counters.SIM_TIME).increment(_simTime);
			context.getCounter(Counters.COMPARE_TIME).increment(_comTime);

		}
	}
	public static class finalMapper extends Mapper<LongWritable, Text, IntWritable, Text>{
		
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException { 
			
			long _tt = System.currentTimeMillis();
				
			
			 List<String> tokenList = new ArrayList<String>();
			 String line=value.toString();
			 StringTokenizer itr = new StringTokenizer(line);
		     
			 while (itr.hasMoreTokens()) {
		    	 tokenList.add(itr.nextToken());
		     }
			 
			 int sysid = Integer.parseInt(tokenList.get(0));
			 double xBar = Double.parseDouble(tokenList.get(1));
			 
			 String outputStr = sysid+" "+xBar;
			 
			 context.write(new IntWritable(1),new Text(outputStr));
			 _tt = System.currentTimeMillis()-_tt;
			 context.getCounter(Counters.MAP_TIME).increment(_tt);
			 
		}
	}
	

	public static class finalReducer extends Reducer<IntWritable,Text,NullWritable,Text> {
		
		public void reduce(IntWritable key, Iterable<Text> values, Context context) 
				throws IOException, InterruptedException {
			long _tt = System.currentTimeMillis();
			int BestID = -1;
			double BestFn = -100000;
			
			
			for(Text value:values) {
				List<String> tokenList = new ArrayList<String>();
				String line = value.toString();
				StringTokenizer tokenizer = new StringTokenizer(line);
				
				
				while (tokenizer.hasMoreTokens()) {
					tokenList.add(tokenizer.nextToken());
				}				
				if(Double.parseDouble(tokenList.get(1))>BestFn) {
					BestID = Integer.parseInt(tokenList.get(0));
					BestFn = Double.parseDouble(tokenList.get(1));
				}
			}	
							
			String outputStr = "The best ID is: "+BestID;
			context.write(NullWritable.get(), new Text(outputStr));
			_tt = System.currentTimeMillis() - _tt;
			
			context.getCounter(Counters.BEST_ID).increment(BestID);
			context.getCounter(Counters.REDUCE_TIME).increment(_tt);
		}
	}
	//[inputPath] [outputPath] [Stage 1 sample size n1] [alpha] [delta] [parameter RB] [parameter cov] [nCores][nSysInGroup]
	public static void main(String[] args) throws Exception{
		System.out.println("args[0]="+args[0]);
		String inputPath = args[1];
		String outputPath = args[2];
		int i = 3;
		int n1 = Integer.parseInt(args[i++]);
		double alpha = Double.parseDouble(args[i++]);
		double delta = Double.parseDouble(args[i++]);
		String param = args[i++];
		String cov = args[i++];
		//int seed = Integer.parseInt(args[i++]);
		int nCores = Integer.parseInt(args[i++]);
		int nSysInGroup = Integer.parseInt(args[i++]);
		
		
		
		long nSys = TpMax.getNumSystems(param);
		
		int startingRound = (int)Math.ceil(Math.log(nSys/nCores)/Math.log(nSysInGroup))+1;
		System.out.println(startingRound);
				
		System.out.println("Total Number of Systems = "+nSys);
		
		
		
		int numReducers = nCores;
		
		
		long start = System.currentTimeMillis();
		
		double total_map_time, total_reduce_time, total_sim_time,total_comp_time;
		total_map_time = total_reduce_time =total_sim_time = total_comp_time = 0.0;
		
		long BESTID = -1;
		
		int total_sample = 0;
		
		Path outPath = null;
		FileSystem fs = null;
		
		
		Configuration conf1 = new Configuration();
		Job job1 = Job.getInstance(conf1,"Knocktout");
		job1.setJarByClass(mainRNCRNC.class);
		job1.setMapperClass(knockoutMapper.class);
		//job1.setCombinerClass(knockoutReducer.class);
		job1.setReducerClass(knockoutReducer.class);
		job1.setMapOutputKeyClass(IntWritable.class);
		job1.setMapOutputValueClass(Text.class);
		job1.setOutputKeyClass(IntWritable.class);
		job1.setOutputValueClass(Text.class);
		job1.setSpeculativeExecution(false);
		job1.setNumReduceTasks(nCores);
		
		//job1.getConfiguration().setInt("seed",seed);
		job1.getConfiguration().setInt("nGroups", numReducers);
		job1.getConfiguration().setDouble("alpha",alpha);
		job1.getConfiguration().setDouble("delta", delta);
		job1.getConfiguration().set("param",param);
		job1.getConfiguration().set("cov",cov);
		job1.getConfiguration().setInt("n1", n1);
		job1.getConfiguration().setInt("nSysInGroup", nSysInGroup);
		job1.getConfiguration().setInt("startingRound", startingRound);
		
		fs = FileSystem.get(conf1);
		outPath = new Path(outputPath+"/groupScreenResults");
		if(fs.exists(outPath)) fs.delete(outPath,true);
		FileInputFormat.addInputPath(job1, new Path(inputPath));
		FileOutputFormat.setOutputPath(job1, outPath);
		job1.waitForCompletion(true);
		
		
		total_map_time += job1.getCounters().findCounter(Counters.MAP_TIME).getValue()/1000.0;
		total_reduce_time += job1.getCounters().findCounter(Counters.REDUCE_TIME).getValue()/1000.0;
		total_sim_time +=job1.getCounters().findCounter(Counters.SIM_TIME).getValue()/1000.0;
		total_comp_time +=job1.getCounters().findCounter(Counters.COMPARE_TIME).getValue()/1000.0;
		total_sample += job1.getCounters().findCounter(Counters.TOTAL_SAMPLE).getValue();
		
		
		System.out.println("Stage 1 finished!");
		
		Configuration conf2 = new Configuration();
		outPath = null;
		Job job2 = Job.getInstance(conf2,"finalstage");
		job2.setJarByClass(mainRNCRNC.class);
		job2.setMapperClass(finalMapper.class);
		//job2.setCombinerClass(finalReducer.class);
		job2.setReducerClass(finalReducer.class);
		job2.setMapOutputKeyClass(IntWritable.class);
		job2.setMapOutputValueClass(Text.class);
		job2.setOutputKeyClass(IntWritable.class);
		job2.setOutputValueClass(Text.class);
		job2.setSpeculativeExecution(false);
		job2.setNumReduceTasks(1);
		
		
		
		fs = FileSystem.get(conf2);
		FileInputFormat.addInputPath(job2, new Path(outputPath+"/groupScreenResults"));
		outPath = new Path(outputPath + "/FINAL RESULTS");
		if (fs.exists(outPath))			fs.delete(outPath, true);
		FileOutputFormat.setOutputPath(job2, outPath);
		job2.waitForCompletion(true);
		
		total_map_time += job2.getCounters().findCounter(Counters.MAP_TIME).getValue()/1000.0;
		total_reduce_time += job2.getCounters().findCounter(Counters.REDUCE_TIME).getValue()/1000.0;
		BESTID = job2.getCounters().findCounter(Counters.BEST_ID).getValue();
		
		long end = System.currentTimeMillis();
		
		
		System.out.println((end-start)/1000.0+" MAP TIME "+total_map_time+" REDUCE TIME "+total_reduce_time+" SIM TIME "+total_sim_time+" COMPARE TIME "+total_comp_time+" TOTAL SAMPLE "+total_sample+" BEST ID "+BESTID);
		System.exit(0);
	}
}
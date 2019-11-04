package org.spark.learning

import rngstream._
import driver._
import problems._
import collection.mutable._;
import collection.JavaConversions._;
import org.apache.spark._;
import org.apache.spark.SparkContext._;
import scala.util._;

object demoRinott {
  def withinCoreSelection(coreID: Int, n1: Int, alpha: Double, delta: Double, param: String, 
    cov: String, nSysInGroup: Int, nCores: Int, nSys: Int, acc_sim: Accumulator[Long], acc_sim_t: Accumulator[Long], acc_com_t: Accumulator[Long] ):(Int,Double)={

  
    val iniAns = new SOAnswer();
    val surIDs = new java.util.ArrayList[java.lang.Integer]();
    val initiate:java.lang.Integer = 1;

    
    val sysContainer: java.util.Map[java.lang.Integer,SOAnswer]=Map(initiate->iniAns);
    sysContainer.clear();
    
    
    
    
    
    for(i <- 1 to nSys if i % nCores == coreID){
      val sysID:java.lang.Integer = i;
      val ans = new SOAnswer();
   
      val R = new java.util.Random();
      
      val getSeed = Math.ceil(R.nextDouble()*1000000).toLong;
      val seed = Array[Long](getSeed,getSeed,getSeed,getSeed,getSeed,getSeed);
      ans.setSeedString(RngStream.SeedToStr(seed));
      sysContainer+=(sysID->ans);
    }
    
    surIDs.addAll(sysContainer.keySet());
    var tempAVG = -0.1;
    var bestID = -1;
    for(i<-0 to surIDs.size()-1){
      val prob = new TpMax(param,cov);
      val rStream = new RngStream();
      rStream.setSeed(RngStream.StrToSeed(sysContainer.get(surIDs.get(i)).getSeedString()));
      var _t = System.currentTimeMillis();
      prob.runSystem(surIDs.get(i),n1,rStream);
      acc_sim_t+= System.currentTimeMillis()-_t;
      sysContainer.replace(surIDs.get(i),prob.getAns());
      val rinottH = Rinott.rinott(nSys,1-alpha,n1-1);
      var rinottSampleSize=Math.ceil(sysContainer.get(surIDs.get(i)).getFnVar()*Math.pow(rinottH/delta,2)).toInt-n1;
      if(rinottSampleSize < 0) rinottSampleSize = 0;
      
      val probNext = new TpMax(param,cov);
      
      _t = System.currentTimeMillis();
      probNext.runSystem(surIDs.get(i),rinottSampleSize,rStream);
      acc_sim_t+= System.currentTimeMillis()-_t;
      acc_sim += rinottSampleSize + n1;
      
      sysContainer.get(surIDs.get(i)).addSample(probNext.getAns().getFn(),rinottSampleSize);
      if(sysContainer.get(surIDs.get(i)).getFn()>tempAVG){
        tempAVG = sysContainer.get(surIDs.get(i)).getFn();
        bestID = surIDs.get(i);
      }
      
    }
    
    (bestID,tempAVG);
    
  }
  
  
  def main(args:Array[String]):Unit={
    
    
    
    
    val start_t = System.nanoTime();
    //val outputFile = "/home/puyang/Desktop/output";//args(0);
    val n1 = args(0).toInt;
    val alpha = args(1).toDouble;
    val delta = args(2).toDouble;
    val param = args(3);
    val cov = args(4);
    val nCores = args(5).toInt;
    val nSysInGroup = args(6).toInt;
    
 
    
    val nSys=problems.TpMax.getNumSystems(param).toInt;
    val maxR = Math.ceil(Math.log(nSys/nCores)/Math.log(nSysInGroup)).toInt + 1;
    
    println(nSys);
    println(maxR);
    
    val conf = new SparkConf().setAppName("KT-Spark").set("spark.cores.max",nCores.toString());
    val sc = new SparkContext(conf);
    
    val coreID = sc.parallelize(0 to nCores-1,nCores);
    
    
    val accum_sim = sc.accumulator(0L,"Accumulator: total sample size");
    val accum_sim_t = sc.accumulator(0L,"Accumulator: total simulation time");
    val accum_com_t = sc.accumulator(0L, "Accumulator: comparison time");
    
    val coreOutPut = coreID.map(withinCoreSelection(_, n1, alpha, delta, param, 
    cov, nSysInGroup,nCores ,nSys ,accum_sim, accum_sim_t, accum_com_t)).cache();

    val finalOutPut = coreOutPut.reduce((x,y)=>if(x._2>y._2) x else y);
    val final_t = (System.nanoTime() - start_t).toDouble/1e9;
    println(f"Total time = $final_t%.2f secs.");
    println(finalOutPut._1);
    println(accum_sim.value);
    println(accum_sim_t.value);
    println(accum_com_t.value);
    sc.stop
  }
}
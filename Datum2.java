package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.util.IntCounter;

import java.util.ArrayList;

public class Datum2 {
    int goldIdx;
    ArrayList<IntCounter> features;
    public Datum2(){
        goldIdx=-1;
        this.features=new ArrayList<IntCounter>();
    }
    public Datum2 (int k){
        goldIdx=k;
        this.features=new ArrayList<IntCounter>();
    }
    public void setGoldIdx(int k){
        goldIdx=k;
    }
    public int getGoldIdx(){
        return goldIdx;
    }
    public void insertFeature(IntCounter featrow){
        this.features.add(featrow);
    }
}

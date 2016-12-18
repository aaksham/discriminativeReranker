package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.IntCounter;

import java.util.ArrayList;
import java.util.List;

public class Datum {
    //public Tree goldTree;
    //public KbestList list;
    ArrayList<IntCounter> features;

    public Datum(/*Tree t,KbestList l*/){
        //this.goldTree=t;
        //this.list=l;
        this.features=new ArrayList<IntCounter>();
    }
    public void insertFeature(IntCounter featrow){
        this.features.add(featrow);
    }
}

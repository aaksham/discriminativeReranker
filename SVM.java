package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.assignments.rerank.LossAugmentedLinearModel;
import edu.berkeley.nlp.util.IntCounter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by aaksha on 11/11/16.
 */
public class SVM implements LossAugmentedLinearModel {
    @Override
    public UpdateBundle getLossAugmentedUpdateBundle(Object datum, IntCounter weights) {
        Datum data=(Datum)datum;
        int l=data.features.size();
        double goldscore=calcScore(weights,data.features.get(0));
        double maxscore=0;
        int maxi=0;
        for (int i=1;i<l;i++){
            double score=calcScore(weights,data.features.get(i));
            if (score>maxscore||maxscore==0){
                maxscore=score;
                maxi=i;
            }
        }
        UpdateBundle ub=new UpdateBundle(data.features.get(0),data.features.get(maxi),-1.0*(goldscore-maxscore));
        return ub;
    }
    public double calcScore(IntCounter weights,IntCounter fv){
        Iterable featureIndices=fv.keySet();
        Iterator fi=featureIndices.iterator();
        double score=0;
        while(fi.hasNext()){
            int featureIndex=(int)fi.next();
            score=score+weights.get(featureIndex)*fv.get(featureIndex);
        }
        return score;
    }
}

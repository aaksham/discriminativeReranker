package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;

import java.util.*;

public class BasicParsingReranker implements ParsingReranker {
    Indexer<String> featureIndexer = new Indexer<String>();
    IntCounter weights=new IntCounter();
    int epochs=20;
    public BasicParsingReranker(Iterable<Pair<KbestList,Tree<String>>> kbestListsAndGoldTrees){
        int counter=0;
        AwesomeFeatureExtractor3 af = new AwesomeFeatureExtractor3();
        ArrayList<Datum2> trainingData=new ArrayList<>();
        Iterator kblgt=kbestListsAndGoldTrees.iterator();
        while (kblgt.hasNext()) {
            Pair p = (Pair) kblgt.next();
            KbestList l = (KbestList) p.getFirst();
            Tree t = (Tree) p.getSecond();
            EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String> eval = new EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String>(Collections.singleton("ROOT"), new HashSet<String>(Arrays.asList(new String[] { "''", "``", ".", ":", "," })));
            String goldTreestr=t.toString();
            int goldIdx=-1;
            int k = l.getKbestTrees().size();
            double[] scores=l.getScores();
            double maxscore=scores[0];
            double minscore=scores[0];
            double maxf1score=0;
            for(int i=0;i<k;i++){
                double f1=eval.evaluateF1(l.getKbestTrees().get(i),t);
                if(f1>=maxf1score) {maxf1score=f1;goldIdx=i;}
                if(scores[i]>maxscore) maxscore=scores[i];
                if(scores[i]<minscore) minscore=scores[i];
            }
            double gap=(maxscore-minscore)/5;

            Datum2 d = new Datum2(goldIdx);

            int[] featsGArr = null;
            double goldscore=scores[goldIdx];
            int logbin=findGroup(maxscore,minscore,goldscore);
            featsGArr = af.extractFeaturesForTree(t,goldIdx,logbin,featureIndexer, true);
            d.insertFeature(makeFeatureCounter(featsGArr));
            for (int i = 0; i < k; i++) {
                double treescore=scores[i];
                logbin=findGroup(maxscore,minscore,treescore);
                Tree tree=l.getKbestTrees().get(i);
                int[] featsArr=af.extractFeaturesForTree(tree,i,logbin,featureIndexer,true);
                d.insertFeature(makeFeatureCounter(featsArr));
            }
            trainingData.add(d);
            counter += 1;
            if (counter % 10000 == 0) {
                System.out.println("Done " + Integer.toString(counter));
                System.out.println(featureIndexer.size());
            }
        }

        System.out.println(trainingData.size());
        System.out.println(featureIndexer.size());

        //initializing weights;
        for(int i=0;i<featureIndexer.size();i++) weights.incrementCount(i,0);

        for (int counter2=0;counter2<epochs;counter2++) {
            System.out.println("Epoch No.: "+Integer.toString(counter2));
            train(trainingData);
        }
    }

    public void train(ArrayList<Datum2> trainingData){
        int counter=0;
        for (Datum2 data: trainingData) {
            int k = data.features.size();
            double maxscore = 0;
            int maxi = 0;
            for (int i = 1; i < k; i++) {
                double score = calcScore(data.features.get(i));
                if (score > maxscore || maxscore == 0) {
                    maxscore = score;
                    maxi = i;
                }
            }
            IntCounter goldFeatures=data.features.get(data.getGoldIdx());
            IntCounter highFeatures=data.features.get(maxi);
            if (data.getGoldIdx()!=maxi){
                Iterable featureIndices=goldFeatures.keySet();
                Iterator fi=featureIndices.iterator();
                while(fi.hasNext()){
                    int featureIndex=(int)fi.next();
                    weights.incrementCount(featureIndex,goldFeatures.get(featureIndex)-highFeatures.get(featureIndex));
                }
            }
        }
    }
    public Tree<String> getBestParse(List<String> sentence, KbestList kbestList) {
        int k = kbestList.getKbestTrees().size();
        double[] scores=kbestList.getScores();
        double maxlistscore=scores[0];
        double minlistscore=scores[0];
        for(int i=0;i<k;i++){
            if(scores[i]>maxlistscore) maxlistscore=scores[i];
            if(scores[i]<minlistscore) minlistscore=scores[i];
        }

        Tree guessedTree=null;
        double maxscore=0;
        AwesomeFeatureExtractor3 af = new AwesomeFeatureExtractor3();

        for (int i = 0; i < k; i++) {
            double treescore=scores[i];
            int logbin=findGroup(maxlistscore,minlistscore,treescore);
            Tree tree=kbestList.getKbestTrees().get(i);
            int[] featsArr = af.extractFeaturesForTree(tree,i,logbin, featureIndexer, false);
            IntCounter datarow=makeFeatureCounter(featsArr);
            double score=calcScore(datarow);
            if (score>maxscore||maxscore==0){
                maxscore=score;
                guessedTree=tree;
            }
        }
        return guessedTree;
    }

    public int findGroup(double maxscore,double minscore, double treescore){
        double gap=(maxscore-minscore)/5;
        int grpno=0;
        if (treescore>=(maxscore-gap)) grpno=1;
        else if((maxscore-2*gap)<=treescore) grpno=2;
        else if((maxscore-3*gap)<=treescore) grpno=3;
        else if((maxscore-4*gap)<=treescore) grpno=4;
        else grpno=5;
        return grpno;
    }
    public IntCounter makeFeatureCounter(int[] featsArr){
        IntCounter datarow=new IntCounter();
        for(int j=0;j<featsArr.length;j++){datarow.incrementCount(featsArr[j],1);}
        return datarow;
    }

    public double calcScore(IntCounter fv){
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

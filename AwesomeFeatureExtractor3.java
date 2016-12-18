package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.ling.AnchoredTree;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;

import java.util.ArrayList;
import java.util.List;

public class AwesomeFeatureExtractor3 {
    public int [] extractFeaturesForTree(Tree tree, int idx, int grpno, Indexer<String> featureIndexer, boolean addFeaturesToIndexer) {
        List<Integer> feats = new ArrayList<Integer>();
        //addFeature("Posn=" + idx, feats, featureIndexer, addFeaturesToIndexer);
        addFeature("ScoreBucket="+grpno,feats,featureIndexer,addFeaturesToIndexer);
        List<String> words = tree.getYield();

        AnchoredTree<String> anchoredTree = AnchoredTree.fromTree(tree);
        for (AnchoredTree<String> subtree : anchoredTree.toSubTreeList()) {
            if (!subtree.isPreTerminal() && !subtree.isLeaf()) {
                String parent = "Parent=" + subtree.getLabel();
                //addFeature(parent, feats, featureIndexer, addFeaturesToIndexer);

                int counter=0;
                String rule = "Rule=" + subtree.getLabel() + " ->";
                String rule_ngram="nngram="+ subtree.getLabel() + " ->";
                for (AnchoredTree<String> child : subtree.getChildren()) {
                    rule += " " + child.getLabel();
                    if (!child.isLeaf()) counter += 1;
                    rule_ngram += " " + child.getLabel();
                    if ((counter==1 && subtree.getChildren().size()!=1)||(counter==2 && subtree.getChildren().size()!=2)||(counter==3&& subtree.getChildren().size()!=3)){
                        addFeature(rule_ngram, feats, featureIndexer, addFeaturesToIndexer);
                    }
                }
                addFeature(rule, feats, featureIndexer, addFeaturesToIndexer);
                String l=getBucket(subtree.getSpanLength())+", "+rule;
                addFeature(l,feats,featureIndexer,addFeaturesToIndexer);
                //l=getBucket(subtree.getSpanLength())+", "+parent;
                //addFeature(l,feats,featureIndexer,addFeaturesToIndexer);

                int si=subtree.getStartIdx();
                int ei=subtree.getEndIdx();

                String spanShape="";
                for (int j=si;j<ei;j++){
                    String word=words.get(j);
                    if (word.charAt(0)>='A' && word.charAt(0)<='Z') spanShape=spanShape+"X";
                    else if (word.charAt(0)>='a' && word.charAt(0)<='z') spanShape=spanShape+"x";
                    else if (Character.isDigit(word.charAt(0))) spanShape=spanShape+"1";
                    else if (word.length()==1) spanShape=spanShape+word;
                }
                addFeature(rule +"="+spanShape, feats, featureIndexer, addFeaturesToIndexer);

                String firstword = words.get(si);
                addFeature(firstword + "_1_" + rule, feats, featureIndexer, addFeaturesToIndexer);
                //addFeature(firstword + "_1_" + parent, feats, featureIndexer, addFeaturesToIndexer);

                String lastword = words.get(ei - 1);
                addFeature(lastword + "_e_" + rule, feats, featureIndexer, addFeaturesToIndexer);
                //addFeature(lastword + "_e_" + parent, feats, featureIndexer, addFeaturesToIndexer);

                if(si-1>=0){
                    String previousword=words.get(si-1);
                    //addFeature(previousword + ">" + parent, feats, featureIndexer, addFeaturesToIndexer);
                    addFeature(previousword + ">" + rule, feats, featureIndexer, addFeaturesToIndexer);
                }
                if(ei<words.size()){
                    String nextword=words.get(ei);
                    //addFeature(nextword + "<" + parent, feats, featureIndexer, addFeaturesToIndexer);
                    addFeature(nextword + "<" + rule, feats, featureIndexer, addFeaturesToIndexer);

                }
                if(counter==2) {
                    AnchoredTree leftchild = subtree.getChildren().get(0);
                    int splitindex = leftchild.getEndIdx();
                    if (splitindex - 1 >= 0) {
                        String wrdbefsplit = words.get(splitindex - 1);
                        addFeature(wrdbefsplit + "<->" + parent, feats, featureIndexer, addFeaturesToIndexer);
                        //addFeature(wrdbefsplit+"<->"+rule,feats,featureIndexer,addFeaturesToIndexer);
                    }
                    if(splitindex<words.size()){
                        String wrdaftersplit=words.get(splitindex);
                        addFeature(wrdaftersplit+"<-"+parent,feats,featureIndexer,addFeaturesToIndexer);
                        //addFeature(wrdaftersplit+"<->"+rule,feats,featureIndexer,addFeaturesToIndexer);
                    }
                }
                int rightBranchL=0;
                int nc=subtree.getChildren().size();
                AnchoredTree rightmostnode=subtree;
                while(nc>=1){
                    rightmostnode=(AnchoredTree)rightmostnode.getChildren().get(nc-1);
                    rightBranchL+=1;
                    nc=rightmostnode.getChildren().size();
                }
                l=getBucket(rightBranchL)+":"+parent;
                addFeature(l,feats,featureIndexer,addFeaturesToIndexer);

            }
        }

        int[] featsArr = new int[feats.size()];
        for (int i = 0; i < feats.size(); i++) {
            featsArr[i] = feats.get(i).intValue();
        }
        return featsArr;

    }

    private String getBucket(int l){
        if(l<=5) return Integer.toString(l);
        if (l<=10) return "10";
        if (l<=20) return "20";
        return ">=21";
    }

    private void addFeature(String feat, List<Integer> feats, Indexer<String> featureIndexer, boolean addNew) {
        if (addNew || featureIndexer.contains(feat)) {
            feats.add(featureIndexer.addAndGetIndex(feat));
        }
    }

    }

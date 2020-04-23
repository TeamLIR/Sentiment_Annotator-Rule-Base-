import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Parser {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //insert your sentence here

        String targetSentence = "During the plea process, Lee repeatedly asked his attorney whether he would face deportation; his attorney assured him that he would not be deported as a result of pleading guilty.";
        Annotation annotation = new Annotation(targetSentence);
        pipeline.annotate(annotation);
        processParseTree(parseTree(annotation).toString());

    }

    //Just returns the string containing complete parse tree structure
    public static Tree parseTree(Annotation ann) {
        List<CoreMap> sentences = ann.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            return tree;
        }
        return null;
    }


    public static List<String> extract_phrase(Tree tree, String label){
        List<String> list=new ArrayList<String>();
        List<Tree> subTreeList = tree.subTreeList();
        for (Tree tree1 : subTreeList) {
            if(tree1.label().value().equals(label)){
                Tree t = tree1;

                System.out.println(tree1);

            }

             }
        return list;

    }
    //returned parse tree processed in this method
    public static String[] processParseTree(String text) {

        //to split from the pattern SBAR IN
        //String[] phraseList = text.split("\\(SBAR \\(IN [a-z]+\\)");
        String[] phraseList = text.split("\\(SBAR \\(IN [a-z]+\\)|\\(SBAR \\(WHNP \\(WDT [a-z]+\\)");

        int count = 0;
        for (String phrase : phraseList) {

            //parantheses and parse tree nodes (Uppercase) are removed
            phrase = phrase.replaceAll("\\(", "").replaceAll("\\)", "").
                    replaceAll("[A-Z$]+ ", "").replaceAll(" [\\.]", " ").
                    replaceAll(" [\\,]", "").trim() + ".";
            phraseList[count] = phrase;

            System.out.println(phrase);

        }
        return phraseList;
    }
}
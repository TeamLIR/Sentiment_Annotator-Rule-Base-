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

        String targetSentence = "In 2008, federal officials received a tip from a confidential informant that Lee had sold the informant ecstasy and marijuana.";
        Annotation annotation = new Annotation(targetSentence);
        pipeline.annotate(annotation);
        processParseTree(parseTree(annotation));

    }

    //Just returns the string containing complete parse tree structure
    public static String parseTree(Annotation ann) {
        List<CoreMap> sentences = ann.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            return tree.toString();
        }
        return null;
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
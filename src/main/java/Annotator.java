
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCostAndGradient;
import edu.stanford.nlp.util.CoreMap;
import utils.NLPUtils;
import java.util.Scanner;

public class Annotator {
    public static String calculateSentiment(NLPUtils nlpUtils, String text){
        SentimentCostAndGradient.createPosTagMap();

        Annotation ann = nlpUtils.annotate(text);

        //to create the Pos tag map
        CustomizedSentimentAnnotator.createPosTagMapForSentence(ann);

        //this line is required, after creating POS tag map needs to annotate again
        ann = nlpUtils.annotate(text);

        List<CoreMap> sentences = ann.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sent : sentences) {
            return ParseTreeSplitter.SentimentClassification(sent);
        }
        return null;
    }


    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter String");

        String input = sc.nextLine();  // Read user input
       // Create the Stanford CoreNLP pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
       StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Annotate an example document.
        Annotation document = new Annotation(input);
        pipeline.annotate(document);
        System.out.println("Sentence -"+input);

        NLPUtils nlpUtils = new NLPUtils();
        String replaceSentenceWords = nlpUtils.replaceCoreferences(document);
        System.out.println("coref -"+replaceSentenceWords);
        System.out.println("Enter Parties");
        String party = sc.nextLine();  // Read user input

        Properties propsCoref = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");
        StanfordCoreNLP pipeline1 = new StanfordCoreNLP(props);

        //insert your sentence here

        Annotation annotation = new Annotation(replaceSentenceWords);
        pipeline1.annotate(annotation);
        List<String> subSentences= nlpUtils.processParseTree(nlpUtils.parseTree(annotation));
        String[] partylist = party.split(",");
       // System.out.println(partylist);


        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");
        NLPUtils nlpUtil = new NLPUtils(props);
        //normal server
//	    NLPUtils nlpUtils = new NLPUtils(props, "http://104.248.226.230", 9000);
        //customized server
        //NLPUtils nlpUtils = new NLPUtils(props, "http://142.93.243.74", 9000);
        try {
            CustomizedSentimentAnnotator.addSentimentLayerToCoreNLPSentiment(
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_positive_mini.csv",
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_negative_mini.csv",
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_neutral_mini.csv");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (String sub : subSentences) {
            // Create the Stanford CoreNLP pipeline
            int sum = 0;
            List<String> list=new ArrayList<String>();
            for (String i : partylist) {
                if (sub.toLowerCase().contains(i.trim().toLowerCase())) {
                    sum+=1;
                    list.add(i);
                   }
            }
            if (list.size()==1){
                System.out.println(sub);
                String sentiment = calculateSentiment(nlpUtil,sub);
                System.out.println(list.get(0) + " - "+ sentiment);
            }
        }




/*

        System.out.println("---");
        System.out.println("coref chains");
        for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
            System.out.println("\t" + cc);
        }
        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println("---");
            System.out.println("mentions");
            for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
                System.out.println("\t" + m);
            }
        }
*/



    }
}

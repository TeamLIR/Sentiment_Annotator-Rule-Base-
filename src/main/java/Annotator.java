
import java.io.FileNotFoundException;
import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCostAndGradient;
import edu.stanford.nlp.util.CoreMap;
import utils.NLPUtils;

public class Annotator {
    public static HashMap<String, ArrayList<ArrayList<String>> > sentimentmap = new HashMap<>();

    public static void updateSentimentMap(String party,List<String> sentiment){
        if (sentimentmap.keySet().contains(party)){
            ArrayList<ArrayList<String>> values= sentimentmap.get(party);
            values.add((ArrayList<String>) sentiment);
            sentimentmap.put(party,values);

        }
        else {
            ArrayList<ArrayList<String>> values = new ArrayList<>();
            values.add((ArrayList<String>) sentiment);
            sentimentmap.put(party,values);
        }

    }
    public static List<String> calculateSentiment(NLPUtils nlpUtils, String text){
        try {
            CustomizedSentimentAnnotator.addSentimentLayerToCoreNLPSentiment(
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_positive_mini.csv",
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_negative_mini.csv",
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_neutral_mini.csv");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SentimentCostAndGradient.createPosTagMap();

        Annotation ann = nlpUtils.annotate(text);

        //to create the Pos tag map
        CustomizedSentimentAnnotator.createPosTagMapForSentence(ann);

        //this line is required, after creating POS tag map needs to annotate again
        ann = nlpUtils.annotate(text);

        List<CoreMap> sentences = ann.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sent : sentences) {
            return ParseTreeSplitter.getSentimentScore(sent);
            //return ParseTreeSplitter.SentimentClassification(sent);
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
       // props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");
        propsCoref.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");
        StanfordCoreNLP pipeline1 = new StanfordCoreNLP(propsCoref);

        //insert your sentence here

        Annotation annotation = new Annotation(replaceSentenceWords);
        pipeline1.annotate(annotation);
        List<String> subSentences= nlpUtils.processParseTree(nlpUtils.parseTree(annotation).toString());
        String[] partylist = party.split(",");
       // System.out.println(partylist);


        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");
        NLPUtils nlpUtil = new NLPUtils(props);
        //normal server
//	    NLPUtils nlpUtils = new NLPUtils(props, "http://104.248.226.230", 9000);
        //customized server
        //NLPUtils nlpUtils = new NLPUtils(props, "http://142.93.243.74", 9000);



        for (String sub : subSentences) {
            String vp = nlpUtil.getVP(sub);
            System.out.println(vp);
            // Create the Stanford CoreNLP pipeline
            int sum = 0;
            List<String> list=new ArrayList<String>();
            for (String i : partylist) {
                if (sub.toLowerCase().contains(i.trim().toLowerCase())) {
                    sum+=1;
                    list.add(i);
                   }
            }
            if (list.size()==0){

            }
            else if (list.size()==1){
                System.out.println(sub);
                List<String> sentiment = calculateSentiment(nlpUtil,sub);
                updateSentimentMap(list.get(0),sentiment);
                System.out.println(list.get(0) + " - "+ sentiment);
            }
            else if (list.size()==2){
                //String vp=nlpUtil.getVP(sub);

                /*Properties prop = new Properties();
                prop.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");
                StanfordCoreNLP pipeline2 = new StanfordCoreNLP(prop);
                Annotation annotation2 = new Annotation(sub);*/
                //pipeline2.annotate(annotation2);
                //String vp= nlpUtils.extractVerbPrase(nlpUtils.parseTree(annotation2));
                if (vp.contains(list.get(0))){
                    List<String> sentiment = calculateSentiment(nlpUtil,vp);
                    updateSentimentMap(list.get(0),sentiment);
                    System.out.println(list.get(0) + " - "+ sentiment);
                    String otherSentiment ="";
                    String score="";
                    if (sentiment.get(0)=="Negative"){
                        otherSentiment= "Non-negative";
                        score =String.valueOf((1- Float.parseFloat(sentiment.get(1))));

                    }
                    else {
                        otherSentiment = "Negative";
                        score = String.valueOf((1- Float.parseFloat(sentiment.get(1))));
                    }
                    List<String> sentiment1 = Arrays.asList(new String[]{otherSentiment, score});
                    updateSentimentMap(list.get(1),sentiment1);

                    
                }


            }

        }
        System.out.println(sentimentmap);



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

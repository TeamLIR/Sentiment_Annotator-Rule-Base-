import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCostAndGradient;
import edu.stanford.nlp.util.CoreMap;
import utils.NLPUtils;


import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

public class SentimentDemo {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");
        NLPUtils nlpUtils = new NLPUtils(props);
        //normal server
//	    NLPUtils nlpUtils = new NLPUtils(props, "http://104.248.226.230", 9000);
        //customized server
         //NLPUtils nlpUtils = new NLPUtils(props, "http://142.93.243.74", 9000);



     //   String filePath = "/home/thejan/FYP/LegalDisourseRelationParser/sentence-feature-extractor/";
      //String filePath = "C:/Users/Asus/Desktop/FYP/Sentiment_Analyser/src/main/resources/";

        try {
            CustomizedSentimentAnnotator.addSentimentLayerToCoreNLPSentiment(
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_positive_mini.csv",
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_negative_mini.csv",
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_neutral_mini.csv");

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String sentence= "he was interested with a doing that.";

        System.out.println(calculateSentiment(nlpUtils,sentence));

        System.out.println(calculateSentimentScore(nlpUtils,sentence));
    }

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

    public static List<String> calculateSentimentScore(NLPUtils nlpUtils, String text){
        SentimentCostAndGradient.createPosTagMap();

        Annotation ann = nlpUtils.annotate(text);

        //to create the Pos tag map
        CustomizedSentimentAnnotator.createPosTagMapForSentence(ann);

        //this line is required, after creating POS tag map needs to annotate again
        ann = nlpUtils.annotate(text);

        List<CoreMap> sentences = ann.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sent : sentences) {
            return ParseTreeSplitter.getSentimentScore(sent);
        }
        return null;
    }



    public static String calculateSentimentOriginalModel (NLPUtils nlpUtils,String text){
        Annotation ann = nlpUtils.annotate(text);

        List<CoreMap> sentences = ann.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sent: sentences){
            return sent.get(SentimentCoreAnnotations.SentimentClass.class);
        }
        return null;
    }
}
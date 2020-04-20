import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

public class SentimentAnalyser {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,natlog,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // read some text in the text variable
        String text = "sam is charged with a crime";
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            System.out.println(sentence);
            int sentimentValue = SentimentOutput(sentence);
            System.out.println(sentimentValue);
        }
    }   // main()
    //return corenlp sentiment for each word
    public static int SentimentOutput(CoreMap sentence) {
        String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
        System.out.println(sentiment);
        //by default value is zero
        int sentimentValue = 0;
        if ("neutral".equals(sentiment.toLowerCase())) {
            sentimentValue = 0;
        } else if ("positive".equals(sentiment.toLowerCase())) {
            sentimentValue = 1;
        } else if ("negative".equals(sentiment.toLowerCase())) {
            sentimentValue = -1;
        }
        return sentimentValue;
    }

}

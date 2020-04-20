

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCostAndGradient;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import org.ejml.simple.SimpleMatrix;
import utils.NLPUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ParseTreeSplitter {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");
        NLPUtils nlpUtils = new NLPUtils(props);
        //        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //To create treemap at the very beginning.
        SentimentCostAndGradient.createPosTagMap();

        //insert your sentence here

        String targetSentence = "Lee contends that he can make this showing because he never would have " +
                "accepted a guilty plea had he known the result would be deportation.";

        String sourceSentence =
                "The Government contends that Lee cannot show prejudice from accepting a plea where his only hope at trial was that"
                        +
                        " something unexpected and unpredictable might occur that would lead to acquittal.";
        Boolean sentimentShift = subjectSentiment(nlpUtils, targetSentence, sourceSentence);
    }

    public static boolean subjectSentiment(NLPUtils nlpUtils, String targetSentence, String sourceSentence) {

        Annotation annTarget = nlpUtils.annotate(targetSentence);
        Annotation annSource = nlpUtils.annotate(sourceSentence);
        //        pipeline.annotate(ann);

       // String filePath = "/home/thejan/FYP/LegalDisourseRelationParser/sentence-feature-extractor/";
        //String filePath = "/home/viraj/FYP/";

        try {
            CustomizedSentimentAnnotator.addSentimentLayerToCoreNLPSentiment(
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_positive_mini.csv",
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_negative_mini.csv",
                    "E:\\fyp\\SentimentAnalyser\\src\\main\\resources" + "/DeviatedSentimentWords/non_neutral_mini.csv");

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ArrayList<SubjectSentimentPair> listTarget = processParseTree(parseTree(annTarget), nlpUtils);
        ArrayList<SubjectSentimentPair> filteredSubjectListTarget = filterSubjectSentiment(listTarget);
        ArrayList<SubjectSentimentPair> listSource = processParseTree(parseTree(annSource), nlpUtils);
        ArrayList<SubjectSentimentPair> filteredSubjectListSource = filterSubjectSentiment(listSource);
        Boolean result = (shiftinViewSentiment(filteredSubjectListTarget, filteredSubjectListSource));
        if (result) {
            System.out.println("Trueee");
            return true;
        } else {
            System.out.println("False");
            return false;
        }
    }

    public static ArrayList<SubjectSentimentPair> filterSubjectSentiment(ArrayList<SubjectSentimentPair> list) {
        ArrayList<String> subjects = new ArrayList<>();
        ArrayList<String> sentiments = new ArrayList<>();
        ArrayList<SubjectSentimentPair> ssPairs = new ArrayList<>();

        for (SubjectSentimentPair pair : list) {
            if (!subjects.contains(pair.subject)) {
                subjects.add(pair.subject);
                sentiments.add(pair.sentiment);
            } else {
                int subjectIndex = subjects.indexOf(pair.subject);
                String currentSentiment = sentiments.get(subjectIndex);
                if (!currentSentiment.equals(pair.sentiment)) {
                    sentiments.remove(subjectIndex);
                    subjects.remove(subjectIndex);
                }
            }
            //System.out.println(pair.subject + "  " + pair.sentiment);
        }

        for (int i = 0; i < subjects.size(); i++) {
            SubjectSentimentPair ssPair = new SubjectSentimentPair();
            ssPair.subject = subjects.get(i);
            ssPair.sentiment = sentiments.get(i);
            ssPairs.add(ssPair);
        }

        return ssPairs;

    }

    public static boolean shiftinViewSentiment(ArrayList<SubjectSentimentPair> filterListTarget,
                                               ArrayList<SubjectSentimentPair> filterListSource) {

        ArrayList<String> subjects = new ArrayList<>();
        ArrayList<String> sentiments = new ArrayList<>();
        Integer count = 0;

        for (SubjectSentimentPair pairT : filterListTarget) {
            subjects.add(pairT.subject);
            sentiments.add(pairT.sentiment);
        }
        for (SubjectSentimentPair pairS : filterListSource) {
            if (subjects.contains(pairS.subject)) {
                int subjectInd = subjects.indexOf(pairS.subject);
                String cSentiment = sentiments.get(subjectInd);
                if (!cSentiment.equals(pairS.sentiment)) {
                    count++;
                }
            }
        }
        if (count == 1) {
            return true;
        }
        return false;

    }

    ;

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
    public static ArrayList<SubjectSentimentPair> processParseTree(String text, NLPUtils nlpUtils) {

        ArrayList<SubjectSentimentPair> subjectSentimentPairs = new ArrayList<>();

        //to split from the pattern SBAR IN
        String[] phraseList = text.split("\\(SBAR \\(IN [a-z]+\\)");

        int count = 0;
        for (String phrase : phraseList) {

            //parantheses and parse tree nodes (Uppercase) are removed
            phrase = phrase.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("[A-Z]+ ", "").replaceAll(" [\\.]", " ")
                    .trim() + ".";
            phraseList[count] = phrase;

            //to identify subject sentiment pairs
            subjectSentimentPairs.add(intermediate_execution(phrase, nlpUtils));

            count += 1;
        }

        return subjectSentimentPairs;
    }

    //to calculate Subject Sentiment pairs
    public static SubjectSentimentPair intermediate_execution(String text, NLPUtils nlpUtils) {
        Annotation ann = nlpUtils.annotate(text);

        CustomizedSentimentAnnotator.createPosTagMapForSentence(ann);
        //this line is required
        ann = nlpUtils.annotate(text);

        return findSubjectAndSentiment(ann);
    }

    //outputs subject for a given sentence part
    public static SubjectSentimentPair findSubjectAndSentiment(Annotation ann) {

        List<CoreMap> sentences = ann.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sent : sentences) {
            SubjectSentimentPair pair = new SubjectSentimentPair();
            SemanticGraph sg = sent.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            pair.sentiment = SentimentClassification(sent);

            for (TypedDependency td : sg.typedDependencies()) {
                if (td.reln().toString().equals("nsubj") || td.reln().toString().equals("nsubjpass")) {
                    pair.subject = td.dep().originalText();
                    return pair;
                }
            }

            return pair;

        }

        return null;
    }

    //to calculate sentiment
    public static String SentimentClassification(CoreMap coreMapSentence) {
        final Tree tree = coreMapSentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
        
        final SimpleMatrix sm = RNNCoreAnnotations.getPredictions(tree);
        final String sentiment = coreMapSentence.get(SentimentCoreAnnotations.SentimentClass.class);

        if (sentiment.equals("Negative") || sentiment.toLowerCase().equals("verynegative")) {
            return sentiment;
        }

        //lowering threshold for negative
        if (Double.parseDouble(sm.toString().split("\n")[2]) >= 0.4) {
            return "Negative";
        }

        return "Non-negative";
    }
}

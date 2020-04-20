

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCostAndGradient;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class CustomizedSentimentAnnotator {

    /*
     *the words with deviated sentiment are stored in files, the directories of those files should
     * be provided. (location in the resources directory
     * */
    public static void addSentimentLayerToCoreNLPSentiment(String nonPositiveFilePath,
                                                           String nonNegativeFilePath,
                                                           String nonNeutralFilePath) throws FileNotFoundException {

//        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//        String pathNonPositive = classLoader.getResource(nonPositiveFilePath).getPath();
//        String pathNonNegative = classLoader.getResource(nonNegativeFilePath).getPath();
//        String pathNonNetural = classLoader.getResource(nonNeutralFilePath).getPath();

        Scanner nonPositiveScanner = new Scanner(new File(nonPositiveFilePath));
        while (nonPositiveScanner.hasNextLine()) {
            String line = nonPositiveScanner.nextLine();
            SentimentCostAndGradient.nonNeutralList.add(line);
        }

        Scanner nonNeutralScanner = new Scanner(new File(nonNeutralFilePath));
        while (nonNeutralScanner.hasNextLine()) {
            SentimentCostAndGradient.nonNeutralList.add(nonNeutralScanner.nextLine());
        }

        Scanner nonNegativeScanner = new Scanner(new File(nonNegativeFilePath));
        while (nonNegativeScanner.hasNextLine()) {
            SentimentCostAndGradient.nonNegativeList.add(nonNegativeScanner.nextLine());
        }
    }



    // if the sentences are not in database: use this method
    public static void
    createPosTagMapForSentence( Annotation annotation) {

        //to create empty map for the word-postag combinations inside SentimentCostAndGradient class
        SentimentCostAndGradient.createPosTagMap();

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap coreMapSentence : sentences) {
            for (CoreLabel token : coreMapSentence.get(CoreAnnotations.TokensAnnotation.class)) {

                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String posTag = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                SentimentCostAndGradient.addPosTagsOfWords(word, posTag);
            }
        }
    }

    /*
     * -1 for negative, 0 for neutral and 1 for positive
     */
    public static int getPhraseSentiment(CoreMap coreMapSentence) {
        String sentiment = coreMapSentence.get(SentimentCoreAnnotations.SentimentClass.class);

        switch (sentiment) {
            case "Negative":
                return -1;

            case "Neutral":
                return 1;

            case "Positive":
                return 1;

            default:
                return 1;
        }
    }
}
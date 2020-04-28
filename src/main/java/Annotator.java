
import java.io.FileNotFoundException;
import java.util.*;

import com.sun.xml.internal.ws.binding.FeatureListUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCostAndGradient;
import edu.stanford.nlp.util.CoreMap;
import utils.NLPUtils;

public class Annotator {
    public static HashMap<String, ArrayList<ArrayList<String>> > sentimentmap = new HashMap<>();
    public static HashMap<String, String> petList = new HashMap<>();
    public static HashMap<String, String> defList = new HashMap<>();
    public static String petitioner;
    public static String defendant;

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
            String file_path = "C:/Users/Asus/Desktop/fyp/Sentiment_Annotator-Rule-Base-/src/main/resources/DeviatedSentimentWords/";
            CustomizedSentimentAnnotator.addSentimentLayerToCoreNLPSentiment(
                    file_path+"non_positive_mini.csv",
                    file_path+"non_negative_mini.csv",
                    file_path+"non_neutral_mini.csv");
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

        System.out.println("Enter Petitioner members");
        petitioner = sc.nextLine();  // Read user input

        System.out.println("Enter Defendant members");
        defendant = sc.nextLine();  // Read user input

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");

        NLPUtils nlpUtils = new NLPUtils(props);
        Annotation annotation = nlpUtils.annotate(input);

        List<String> subSentences= nlpUtils.processParseTree(nlpUtils.parseTree(annotation).toString());
        if (subSentences.size()==0) {
            subSentences.add(input);
        }
        System.out.println(subSentences);

        List<String> partyList = new ArrayList<>(Arrays.asList(petitioner.split(",")));
        partyList.addAll(Arrays.asList(defendant.split(",")));

        for (String sub : subSentences) {
            List<String> phrase_list = nlpUtils.getPhrases(sub); //noun noun phrase and verb phrase
            String np = phrase_list.get(0);
            String vp = phrase_list.get(1);

            List<String> np_memberList= new ArrayList<String>();
            List<String> vp_memberList= new ArrayList<String>();
            for(String phrase : phrase_list) {
                int sum = 0;
                List<String> list = new ArrayList<String>();
                for (String i : partyList) {
                    String member = i.trim().toLowerCase() + "'s";
                    if (phrase.toLowerCase().contains(i.trim().toLowerCase()) | phrase.toLowerCase().contains(member)) {
                        sum += 1;
                        list.add(i);
                    }
                }
                if (phrase_list.indexOf(phrase)==0){
                    np_memberList = list;
                }
                else if (phrase_list.indexOf(phrase)==1){
                    vp_memberList = list;
                }
            }
            //
            List<String> merged = new ArrayList<>();
            merged.addAll(np_memberList);
            merged.addAll(vp_memberList);
            List<String> distinctValues = new ArrayList<>();
            for(int i=0;i<merged.size();i++){
                boolean isDistinct = false;
                for(int j=0;j<i;j++){
                    if(merged.get(i) == merged.get(j)){
                        isDistinct = true;
                        break;
                    }
                }
                if(!isDistinct){
                    distinctValues.add(merged.get(i));
                }
            }
            System.out.println(distinctValues);
            if (vp_memberList.size()==0){

            }
            if (merged.size()==1){
                String party = merged.get(0);
                System.out.println(sub);
                List<String> sentiment = calculateSentiment(nlpUtils,sub);
                updateSentimentMap(party,sentiment);
                System.out.println(party + " - "+ sentiment);
            }

            else if (merged.size()==2 | distinctValues.size()==2){
                List<String> sentiment = calculateSentiment(nlpUtils, vp);
                if (np_memberList.size()==vp_memberList.size()) {
                    System.out.println(sub);
                    if (vp.toLowerCase().contains(vp_memberList.get(0)) | vp.toLowerCase().contains(vp_memberList.get(0).toLowerCase() + " 's")) {
                        updateSentimentMap(vp_memberList.get(0), sentiment);
                        System.out.println(vp_memberList.get(0) + " - " + sentiment);
                        String otherSentiment = "";

                        if (sentiment.get(0).equals("Negative")) {
                            otherSentiment = "Non-negative";

                        } else {
                            otherSentiment = "Negative";
                        }

                        ArrayList<String> sentiment1 = new ArrayList(Arrays.asList(otherSentiment, sentiment.get(1)));
                        updateSentimentMap(np_memberList.get(0), sentiment1);
                    }
                }

                if(vp_memberList.size()>np_memberList.size()){
                    if (vp.toLowerCase().contains(vp_memberList.get(0).toLowerCase() + " 's " + vp_memberList.get(1).toLowerCase())) {
                        updateSentimentMap(vp_memberList.get(0), sentiment);
                        updateSentimentMap(vp_memberList.get(1), sentiment);
                        if (np_memberList.get(0).equals(vp_memberList.get(0)) | np_memberList.get(0).equals(vp_memberList.get(1))){
                            updateSentimentMap(np_memberList.get(0), sentiment);
                        }else {
                            String otherSentiment = "";

                            if (sentiment.get(0).equals("Negative")) {
                                otherSentiment = "Non-negative";

                            } else {
                                otherSentiment = "Negative";
                            }

                            ArrayList<String> sentiment1 = new ArrayList(Arrays.asList(otherSentiment, sentiment.get(1)));
                            updateSentimentMap(np_memberList.get(0), sentiment1);
                        }
                    }
                }
            }
        }
        System.out.println(sentimentmap);
        preparingOutput(sentimentmap);
    }

    private static void preparingOutput(HashMap<String, ArrayList<ArrayList<String>>> sentimentMap) {

        for (String key : sentimentMap.keySet()) {
            int pos = 0;
            int neg = 0;
            if (sentimentMap.get(key).size()>1){
                for (ArrayList<String> value: sentimentMap.get(key)) {
                    if (value.get(0).equals("Non-negative")){
                        pos +=1;
                    }else {
                        neg+=1;
                    }
                }
                if (neg>=pos){
                    makeOutput(key,"Negative");
                }else {
                    makeOutput(key,"Non-Negative");
                }
            } else {
                makeOutput(key,sentimentMap.get(key).get(0).get(0));
            }
        }
        System.out.println("pet" + petList);
        System.out.println("def" + defList);
    }

    private static void makeOutput(String party_member, String sentiment) {

        if (Arrays.asList(petitioner.split(",")).contains(party_member)) {
            petList.put(party_member, sentiment);
        } else if (Arrays.asList(defendant.split(",")).contains(party_member)) {
            defList.put(party_member, sentiment);
        }

    }

}

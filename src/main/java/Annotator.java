
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
    public static ArrayList<HashMap<String, String>> output1 = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> output2 = new ArrayList<>();

    public static HashMap<String, String> petList1 = new HashMap<>();
    public static HashMap<String, String> defList1 = new HashMap<>();

    public static HashMap<String, String> petList2 = new HashMap<>();
    public static HashMap<String, String> defList2 = new HashMap<>();
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
            String file_path = "E:\\fyp\\New folder\\Sentiment_Annotator-Rule-Base-\\src\\main\\resources\\DeviatedSentimentWords\\";
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

    public static void Annotator(String input1,String petition,String defend) {
       /* Scanner sc = new Scanner(System.in);  // Create a Scanner object

        System.out.println("Enter String");
        String input = sc.nextLine();  // Read user input

        System.out.println("Enter Petitioner members");
        petitioner = sc.nextLine();  // Read user input

        System.out.println("Enter Defendant members");
        defendant = sc.nextLine();  // Read user input*/
        petitioner = petition;
        defendant = defend;
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,depparse,sentiment");

        NLPUtils nlpUtils = new NLPUtils(props);
        Annotation annotation = nlpUtils.annotate(input1);

        List<String> subSentences= nlpUtils.processParseTree(nlpUtils.parseTree(annotation).toString());
        if (subSentences.size()==0) {
            subSentences.add(input1);
        }
        System.out.println(subSentences);
        List<String> partyList = new ArrayList<>();
        if (petitioner.trim().length()>0){
            partyList.addAll(Arrays.asList(petitioner.trim().split(",")));
        }
      if (defendant.trim().length()>0){
          partyList.addAll(Arrays.asList(defendant.trim().split(",")));
      }


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
                    String member = i.trim().toLowerCase() + " 's";
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
            //System.out.println(distinctValues);
            if (vp_memberList.size()==0){

            }
            if (merged.size()==1){
                String party = merged.get(0);
               // System.out.println(sub);
                List<String> sentiment = calculateSentiment(nlpUtils,sub);
                updateSentimentMap(party,sentiment);
               // System.out.println(party + " - "+ sentiment);
            }

            else if (merged.size()==2 | distinctValues.size()==2){
                List<String> sentiment = calculateSentiment(nlpUtils, vp);
                if (np_memberList.size()==vp_memberList.size()) {
                    //System.out.println(sub);
                    if (vp.toLowerCase().contains(vp_memberList.get(0)) | vp.toLowerCase().contains(vp_memberList.get(0).toLowerCase() + " 's")) {
                        updateSentimentMap(vp_memberList.get(0), sentiment);
                        //System.out.println(vp_memberList.get(0) + " - " + sentiment);
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
        preparingOutput1(sentimentmap);
        preparingOutput2(sentimentmap);
        output2.add(petList2);
        output2.add(defList2);

        output1.add(petList1);
        output1.add(defList1);

        System.out.println("output1 using sentiment       :" + output1);
        System.out.println("output2 using sentiment scores:" + output2);
    }



    private static void preparingOutput1(HashMap<String, ArrayList<ArrayList<String>>> sentimentMap) {

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
                if (neg>pos){
                    makeOutput1(key,"Negative");
                }else {
                    makeOutput1(key,"Non-Negative");
                }
            } else {
                makeOutput1(key,sentimentMap.get(key).get(0).get(0));
            }
        }
    }

    private static void preparingOutput2(HashMap<String, ArrayList<ArrayList<String>>> sentimentmap) {
        for (String key : sentimentmap.keySet()) {
            float pos = 0;
            float neg = 0;
            float p=0;
            float n=0;
            if (sentimentmap.get(key).size()>1){
                for (ArrayList<String> value: sentimentmap.get(key)) {
                    String score = value.get(1);
                    if (value.get(0).equals("Non-negative")){
                        pos +=Float.parseFloat(score) ;
                        p= (float) (p+1.0);
                    }else {
                        neg+=Float.parseFloat(score);
                        n= (float) (n + 1.0);
                    }
                }
                //System.out.println("positive:" + pos + ",   negative:" + neg);
                if (p!=0 && n!=0) {
                    if (neg % n > pos % p) {
                        makeOutput2(key, "Negative");
                    } else {
                        makeOutput2(key, "Non-Negative");
                    }
                }else {
                    if (neg>pos){
                        makeOutput2(key, "Negative");
                    } else {
                        makeOutput2(key, "Non-Negative");
                    }
                }
            } else {
                makeOutput2(key,sentimentmap.get(key).get(0).get(0));
            }
        }
    }

    private static void makeOutput1(String party_member, String sentiment) {

        if (Arrays.asList(petitioner.split(",")).contains(party_member)) {
            petList1.put(party_member, sentiment);
        } else if (Arrays.asList(defendant.split(",")).contains(party_member)) {
            defList1.put(party_member, sentiment);
        }

    }

    private static void makeOutput2(String party_member, String sentiment) {

        if (Arrays.asList(petitioner.split(",")).contains(party_member)) {
            petList2.put(party_member, sentiment);
        } else if (Arrays.asList(defendant.split(",")).contains(party_member)) {
            defList2.put(party_member, sentiment);
        }

    }

}

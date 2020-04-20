import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Properties;

public class DependencyParser {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
        props.setProperty("coref.algorithm", "neural");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // read some text in the text variable
        String text = "The insurer unsuccessfully sought through interpleader proceedings in Pennsylvania to secure release from all liability.";
        // create an empty Annotation just with the given text
        CoreDocument document = new CoreDocument(text);
        // run all Annotators on this text
        pipeline.annotate(document);

        CoreSentence sentence = document.sentences().get(1);
        System.out.println(sentence);
        SemanticGraph dependencyParse = sentence.dependencyParse();
        System.out.println("Example: dependency parse");
        System.out.println(dependencyParse);
        System.out.println();
    }
}

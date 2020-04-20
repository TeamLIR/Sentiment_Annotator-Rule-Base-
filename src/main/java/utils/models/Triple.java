package utils.models;

public class Triple {
    public String subject;
    public String object;
    public String relation;
    public String subjectLemma;
    public String objectLemma;
    public String relationLemma;
    public String sentence;
    public double confidence;

    @Override
    public String toString(){
        return new String(confidence+": ("+subject+"; "+relation+"; "+object+")");
    }
}

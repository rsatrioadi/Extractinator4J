package nl.tue.win.extractinator.stereotype;

import java.util.ArrayList;
import java.util.List;

public class Facts {
    private final List<String> words = new ArrayList<>();
    private long numConditionals;
    private long numLoops;
    private long numArrays;
    private long numCollections;
    private long numStatements;
    private long numExpressions;

    public List<String> getWords() {
        return words.subList(0, words.size());
    }

    public void setWords(List<String> words) {
        this.words.clear();
        this.words.addAll(words);
    }

    public long getNumConditionals() {
        return numConditionals;
    }

    public void setNumConditionals(long numConditionals) {
        this.numConditionals = numConditionals;
    }

    public long getNumLoops() {
        return numLoops;
    }

    public void setNumLoops(long numLoops) {
        this.numLoops = numLoops;
    }

    public long getNumArrays() {
        return numArrays;
    }

    public void setNumArrays(long numArrays) {
        this.numArrays = numArrays;
    }

    public long getNumCollections() {
        return numCollections;
    }

    public void setNumCollections(long numCollections) {
        this.numCollections = numCollections;
    }

    public long getNumStatements() {
        return numStatements;
    }

    public void setNumStatements(long numStatements) {
        this.numStatements = numStatements;
    }

    public long getNumExpressions() {
        return numExpressions;
    }

    public void setNumExpressions(long numExpressions) {
        this.numExpressions = numExpressions;
    }
}

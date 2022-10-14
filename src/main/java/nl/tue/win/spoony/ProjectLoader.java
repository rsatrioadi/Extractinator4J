package nl.tue.win.spoony;

public class ProjectLoader {

    private final String fileName;
    private final String outputPrefix;

    public ProjectLoader(String[] args) {
        this.fileName = args[args.length - 2];
        this.outputPrefix = args[args.length - 1];
    }

    public String getFileName() {
        return fileName;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }
}

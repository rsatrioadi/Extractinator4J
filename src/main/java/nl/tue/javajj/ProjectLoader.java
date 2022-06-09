package nl.tue.javajj;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ProjectLoader {

    private final String[] args;

    public ProjectLoader(String[] args) {
        this.args = args;
    }

    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> tmp = new ArrayList<>();
        String fileName = args[args.length-1];
        try(ZipFile zipFile = new ZipFile(fileName)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".java")) {
                    InputStream stream = zipFile.getInputStream(entry);
                    tmp.add(StaticJavaParser.parse(stream));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.unmodifiableList(tmp);
    }
}

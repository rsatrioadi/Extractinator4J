package nl.tue.win.extractinator;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceZip;
import nl.tue.win.javajj.model.Project;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class ProjectLoader {

    private final String[] args;
    private final Project project;
    private final JavaSymbolSolver symbolSolver;
    private final TypeSolver typeSolver;
    private final MemoryTypeSolver memSolver;
    private final String fileName;
    private final String outputPrefix;

    public ProjectLoader(String[] args) {
        this.args = args;
        this.project = new Project(getName());
        this.fileName = args[args.length - 2];
        this.outputPrefix = args[args.length - 1];
        this.memSolver = new MemoryTypeSolver();
        this.typeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                memSolver);
        this.symbolSolver = new JavaSymbolSolver(typeSolver);
    }

    public String getName() {
        return args[args.length - 1].replace(".zip", "");
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public TypeSolver getTypeSolver() {
        return typeSolver;
    }

    public MemoryTypeSolver getMemSolver() {
        return memSolver;
    }

    public JavaSymbolSolver getSymbolSolver() {
        return symbolSolver;
    }

    public List<CompilationUnit> getCompilationUnits() throws IOException {
        SourceZip sourceZip = new SourceZip(Paths.get(fileName), new ParserConfiguration().setSymbolResolver(symbolSolver));
        List<Pair<Path, ParseResult<CompilationUnit>>> cuList = sourceZip.parse();
        return cuList.stream().parallel()
                .map(pair -> pair.b.getResult())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public Project getProject() {
        return project;
    }
}

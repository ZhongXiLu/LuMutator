package lumutator.tracer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import lumutator.Configuration;
import lumutator.tracer.debugger.Debugger;
import lumutator.tracer.debugger.Observer;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Tracer: trace a set of tests.
 */
public abstract class Tracer {

    /**
     * Trace a set of tests.
     *
     * @param config           The {@link Configuration}.
     * @param files            All the test files.
     * @param inspectorMethods Set of all inspector methods in the source classes.
     * @throws IOException    If it failed parsing the test files.
     * @throws ParseException If it failed parsing the test files.
     */
    public static void trace(Configuration config, List<File> files, Set<String> inspectorMethods) throws IOException, ParseException {
        File directory = new File("traces");
        directory.deleteOnExit();   // TODO: fix this?
        if (!directory.exists()) {
            directory.mkdir();
        }

        for (File file : files) {
            CompilationUnit compilationUnit = JavaParser.parse(file);
            final String classToDebug = String.format(
                    "%s.%s",
                    compilationUnit.getPackage().getName(),
                    FilenameUtils.removeExtension(file.getName())
            );

            Observer observer = new Observer(String.format("traces/%s.txt", classToDebug), inspectorMethods);   // TODO: add custom filepath
            Debugger debugger = new Debugger(config, classToDebug, observer);

            // Set breakpoint at start of each test (@Test)
            for (TypeDeclaration decl : compilationUnit.getTypes()) {
                for (BodyDeclaration member : decl.getMembers()) {
                    for (AnnotationExpr annotation : member.getAnnotations()) {
                        if (annotation.getName().toString().equals("Test")) {   // TODO: also include @Before, ...?
                            MethodDeclaration field = (MethodDeclaration) member;
                            debugger.addBreakpoint(field.getName());
                        }
                    }
                }
            }

            debugger.run();
        }
    }

}

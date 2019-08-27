package lumutator.tracer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import lumutator.tracer.debugger.Debugger;
import lumutator.tracer.debugger.Observer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.json.JSONArray;
import org.json.JSONObject;

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
     * @param directory        Directory that contains the test files that need to be traced.
     * @param inspectorMethods Set of all inspector methods in the source classes.
     * @return The traces of the tests.
     * @throws IOException    If it failed parsing the test files.
     * @throws ParseException If it failed parsing the test files.
     */
    public static JSONArray trace(String directory, Set<String> inspectorMethods) throws IOException, ParseException {
        List<File> testFiles = (List<File>) FileUtils.listFiles(
                new File(directory),
                new RegexFileFilter("(?i)^(.*?test.*?)"),       // only match test files
                DirectoryFileFilter.DIRECTORY
        );

        JSONArray traces = new JSONArray();
        for (File file : testFiles) {
            CompilationUnit compilationUnit = JavaParser.parse(file);
            final String classToDebug = String.format(
                    "%s.%s",
                    compilationUnit.getPackage().getName(),
                    FilenameUtils.removeExtension(file.getName())
            );

            Observer observer = new Observer(inspectorMethods);
            Debugger debugger = new Debugger(classToDebug, observer);

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
            //System.out.println(new JSONObject().put(file.getCanonicalPath(), observer.getTrace()));
            traces.put(new JSONObject().put(file.getCanonicalPath(), observer.getTrace()));
        }

        //System.out.println(traces.toString(4));
        return traces;
    }

}

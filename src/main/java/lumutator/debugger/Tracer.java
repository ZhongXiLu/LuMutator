package lumutator.debugger;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import com.sun.jdi.event.Event;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Tracer: create a tracefile by examining a running program; used by Debugger.
 * TODO: rename to `Observer`
 */
public class Tracer {

    /**
     * Write to tracefile.
     */
    private BufferedWriter writer;

    /**
     * Constructor.
     *
     * @param outputFile The tracefile.
     * @throws IOException If it somehow failed creating an output directory.
     */
    public Tracer(String outputFile) throws IOException {
        writer = new BufferedWriter(new FileWriter(outputFile));
    }

    /**
     * Trace the (break)point at the event.
     *
     * @param event The event to be traced.
     */
    public void trace(Event event) {
        // https://dzone.com/articles/examining-variables-jdi


    }

    /**
     * Close the tracer (and tracefile).
     *
     * @throws IOException If it somehow failed closing the tracefile.
     */
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Print all the local variables on the stackframe.
     *
     * @param frame The stackframe.
     */
    public void locals(StackFrame frame) {
        try {
            Map<LocalVariable, Value> visibleVariables = frame.getValues(frame.visibleVariables());
            writer.write("[Local Variables]\n");
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
                //ClassType classType = (ClassType) entry.getKey().type();
                //System.out.println(classType.methods());

                // Variable value
                writer.write(entry.getKey().name() + "=" + entry.getValue() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

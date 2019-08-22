package lumutator.tracer.debugger;

import com.sun.jdi.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Observer: create a tracefile by observing a running program; used by Debugger.
 */
public class Observer {

    /**
     * Write to tracefile.
     */
    private BufferedWriter writer;

    /**
     * Set of all inspector methods in the source classes.
     */
    private Set<String> inspectorMethods;

    /**
     * Constructor.
     *
     * @param outputFile       The tracefile.
     * @param inspectorMethods Set of all inspector methods in the source classes.
     * @throws IOException If it somehow failed creating an output directory.
     */
    public Observer(String outputFile, Set<String> inspectorMethods) throws IOException {
        writer = new BufferedWriter(new FileWriter(outputFile));
        this.inspectorMethods = inspectorMethods;
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
     * Trace the current state of the virtual machine.
     * (reference: https://dzone.com/articles/examining-variables-jdi)
     * <p>
     * (1) Local variables on the stackframe.
     * (2) States of non-primitive objects.
     *
     * @param vm       The current running virtual machine.
     * @param thread   The current running thread.
     * @param location Current location of the vm.
     */
    public void observe(VirtualMachine vm, ThreadReference thread, Location location) {
        // TODO: probably better to store trace in .json format?
        try {
            Map<LocalVariable, Value> visibleVariables = thread.frame(0).getValues(thread.frame(0).visibleVariables());
            writer.write(location.toString() + "\n");

            // Check each local variable
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {

                // Check if it's a non-primitive datatype
                try {
                    // Non-primitive datatype => use inspector methods to inspect state
                    ClassType classType = (ClassType) entry.getKey().type();
                    for (Method method : classType.methods()) {
                        Matcher matcher = Pattern.compile("([^(]+)\\(").matcher(method.toString());
                        if (matcher.find() && inspectorMethods.contains(matcher.group(1))) {
                            // Execute inspector method
                            writer.write(String.format(
                                    "%s=%s\n",
                                    String.format("%s.%s()", entry.getKey().name(), method.name()),
                                    Debugger.evaluate(String.format("%s.%s()", entry.getKey().name(), method.name()), vm, thread.frame(0))
                            ));
                        }
                    }

                } catch (java.lang.ClassCastException e) {
                    // Primitive datatype => just get the value
                    writer.write(entry.getKey().name() + "=" + entry.getValue() + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

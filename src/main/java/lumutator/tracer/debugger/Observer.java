package lumutator.tracer.debugger;

import com.sun.jdi.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Observer: josn trace object by observing a running program; used by Debugger.
 */
public class Observer {

    /**
     * Json array that stores the trace.
     */
    private JSONArray json;

    /**
     * Set of all inspector methods in the source classes.
     */
    private Set<String> inspectorMethods;

    /**
     * Constructor.
     *
     * @param inspectorMethods Set of all inspector methods in the source classes.
     */
    public Observer(Set<String> inspectorMethods) {
        json = new JSONArray();
        this.inspectorMethods = inspectorMethods;
    }

    /**
     * Get the trace.
     *
     * @return The trace in form of a JSON array.
     */
    public JSONArray getTrace() {
        return json;
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
        try {
            Map<LocalVariable, Value> visibleVariables = thread.frame(0).getValues(thread.frame(0).visibleVariables());

            // Check each local variable
            JSONArray traces = new JSONArray();
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {

                // Check if it's a non-primitive datatype
                try {
                    // TODO: recursive: call again if return value is non-primitive datatype?
                    // Non-primitive datatype => use inspector methods to inspect state
                    ClassType classType = (ClassType) entry.getKey().type();
                    for (Method method : classType.methods()) {
                        Matcher matcher = Pattern.compile("([^(]+)\\(").matcher(method.toString());
                        if (matcher.find() && inspectorMethods.contains(matcher.group(1))) {
                            // Execute inspector method
                            Value value = Debugger.evaluate(String.format("%s.%s()", entry.getKey().name(), method.name()), vm, thread.frame(0));
                            JSONObject trace = new JSONObject().put(
                                    String.format("%s.%s()", entry.getKey().name(), method.name()),
                                    value == null ? JSONObject.NULL : value
                            );
                            traces.put(trace);
                        }
                    }

                } catch (java.lang.ClassCastException e) {
                    // Primitive datatype => just get the value
                    JSONObject trace = new JSONObject().put(entry.getKey().name(), entry.getValue());
                    traces.put(trace);

                } catch (ClassNotLoadedException e) {
                    // TODO: fix this?

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            json.put(new JSONObject().put(String.valueOf(location.lineNumber()), traces));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

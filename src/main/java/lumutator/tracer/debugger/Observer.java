package lumutator.tracer.debugger;

import com.sun.jdi.*;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Observer: observe a running program and create a trace; used by {@link Debugger}.
 */
public class Observer {

    /**
     * Json object that stores the trace.
     */
    private JSONObject json;

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
        json = new JSONObject();
        this.inspectorMethods = inspectorMethods;
    }

    /**
     * Get the trace.
     *
     * @return The trace in form of a JSON object.
     */
    public JSONObject getTrace() {
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
            JSONObject trace = new JSONObject();
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {

                // Check if it's a non-primitive datatype
                try {
                    if (entry.getValue() instanceof StringReference || entry.getValue() == null) {
                        // String and null are considered a "complex" type, enforce primitive type
                        throw new ClassCastException();
                    } else {
                        // TODO: recursive: call again if return value is non-primitive datatype?
                        // Non-primitive datatype => use inspector methods to inspect state
                        ClassType classType = (ClassType) entry.getKey().type();
                        for (Method method : classType.methods()) {
                            Matcher matcher = Pattern.compile("([^(]+)\\(").matcher(method.toString());
                            if (matcher.find() && inspectorMethods.contains(matcher.group(1))) {
                                // Execute inspector method
                                Value value = Debugger.evaluate(String.format("%s.%s()", entry.getKey().name(), method.name()), vm, thread.frame(0));
                                addTrace(trace, String.format("%s.%s()", entry.getKey().name(), method.name()), value);
                            }
                        }
                    }

                } catch (java.lang.ClassCastException e) {
                    // Primitive datatype => just get the value
                    addTrace(trace, entry.getKey().name(), entry.getValue());

                } catch (ClassNotLoadedException e) {
                    // Should not be possible
                }
            }

            // TODO: do other comparisons (e.g. compare local objects to each other)

            if (!trace.isEmpty()) {
                json.toString();    // For some reason this prevents a bug (json becomes null)
                json.put(String.valueOf(location.lineNumber()), trace);
            }

        } catch (AbsentInformationException | IncompatibleThreadStateException e) {
            throw new RuntimeException("Incompatible thread state: " + e.getMessage());
        }
    }

    /**
     * Add new entry in the current trace.
     *
     * @param trace The current trace.
     * @param key   The key, i.e. variable name, expression, ...
     * @param value The value of the key.
     */
    private void addTrace(JSONObject trace, String key, Value value) {
        if (value instanceof PrimitiveValue) {
            if (value instanceof BooleanValue) {
                trace.put(key, ((BooleanValue) value).value());
            } else if (value instanceof ByteValue) {
                trace.put(key, ((ByteValue) value).value());
            } else if (value instanceof CharValue) {
                trace.put(key, ((CharValue) value).value());
            } else if (value instanceof DoubleValue) {
                trace.put(key, ((DoubleValue) value).value());
            } else if (value instanceof FloatValue) {
                trace.put(key, ((FloatValue) value).value());
            } else if (value instanceof IntegerValue) {
                trace.put(key, ((IntegerValue) value).value());
            } else if (value instanceof LongValue) {
                trace.put(key, ((LongValue) value).value());
            } else if (value instanceof ShortValue) {
                trace.put(key, ((ShortValue) value).value());
            } else {
                // void
                trace.put(key, JSONObject.NULL);
            }

        } else if (value instanceof ObjectReference) {
            if (value instanceof StringReference) {
                trace.put(key, ((StringReference) value).value());
            }
            // TODO: else: ArrayReference or ObjectReference

        } else {
            // null
            trace.put(key, JSONObject.NULL);
        }
    }

}

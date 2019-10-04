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

            // (1) Check each local variable
            JSONObject trace = new JSONObject();
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
                traceObject(vm, thread, trace, entry.getKey().name(), entry.getValue());
            }

            // (2) Check class fields
            ObjectReference thisObject = thread.frame(0).thisObject();
            for (Field field: thisObject.referenceType().allFields()) {
                traceObject(vm, thread, trace, field.name(), thisObject.getValue(field));
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
     * Trace an object, this might be a of a primitive type or a complex type.
     * Works recursively, meaning if a method return another object, this object gets traced too.
     *
     * @param vm       The current running virtual machine.
     * @param thread   The current running thread.
     * @param trace    The current trace.
     * @param variable Name of the variable, can also be an expression that returns a variable.
     * @param value    The value of the variable.
     * @throws IncompatibleThreadStateException If incompatible thread state.
     */
    private void traceObject(VirtualMachine vm, ThreadReference thread, JSONObject trace, String variable, Value value)
            throws IncompatibleThreadStateException {

        // TODO: fix problem with circular dependency => infinite recursion?
        // TODO: add test for this + extend bank application with `Bank` class

        // Check if it's a non-primitive datatype
        try {
            if (value instanceof StringReference || value == null) {
                // String and null are considered a "complex" type, enforce primitive type
                throw new ClassCastException();
            } else {
                // TODO: recursive: call again if return value is non-primitive datatype?
                // Non-primitive datatype => use inspector methods to inspect state
                ClassType classType = (ClassType) value.type();
                for (Method method : classType.methods()) {
                    Matcher matcher = Pattern.compile("([^(]+)\\(").matcher(method.toString());
                    if (matcher.find() && inspectorMethods.contains(matcher.group(1))) {
                        // Execute inspector method
                        Value evaluatedValue = Debugger.evaluate(String.format("%s.%s()", variable, method.name()), vm, thread.frame(0));
                        traceObject(vm, thread, trace, String.format("%s.%s()", variable, method.name()), evaluatedValue);
                        //addTrace(trace, String.format("%s.%s()", variable, method.name()), evaluatedValue);
                    }
                }
            }

        } catch (java.lang.ClassCastException e) {
            // Primitive datatype => just get the value
            addTrace(trace, variable, value);

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

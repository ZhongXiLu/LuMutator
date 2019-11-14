package lumutator.tracer.debugger;

import com.sun.jdi.*;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
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
            JSONObject trace = new JSONObject();

            // (1) Check each local variable
            Map<LocalVariable, Value> visibleVariables = thread.frame(0).getValues(thread.frame(0).visibleVariables());
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
                traceObject(vm, thread, trace, entry.getKey().typeName(), entry.getKey().name(), entry.getValue(), new HashSet<>());
            }

            // (2) Check class fields (i.e. fields of the test class itself)
            ObjectReference thisObject = thread.frame(0).thisObject();
            if (thisObject != null) {
                for (Field field : thisObject.referenceType().allFields()) {
                    traceObject(vm, thread, trace, field.typeName(), field.name(), thisObject.getValue(field), new HashSet<>());
                }
            }

            // Commit new trace
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
     * @param vm             The current running virtual machine.
     * @param thread         The current running thread.
     * @param trace          The current trace.
     * @param varType        The type assigned to the variable (does NOT necessarily equal to the type of the variable).
     * @param variable       Name of the variable, can also be an expression that returns a variable.
     * @param value          The value of the variable.
     * @param visitedClasses Map of all the seen classes, this is just to prevent infinite recursion.
     * @throws IncompatibleThreadStateException If incompatible thread state.
     */
    private void traceObject(VirtualMachine vm, ThreadReference thread, JSONObject trace, String varType, String variable,
                             Value value, HashSet<String> visitedClasses) throws IncompatibleThreadStateException {

        // Check if it's a non-primitive datatype
        try {
            if (value instanceof StringReference || value == null) {
                // String and null are here considered a "complex" type, enforce primitive type
                throw new ClassCastException();

            } else if (value instanceof ArrayReference) {
                // Array
                List<Value> values = ((ArrayReference) value).getValues();
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i) != null) {
                        traceObject(vm, thread, trace, values.get(i).type().name(), String.format("%s[%s]", variable, i), values.get(i), visitedClasses);
                    } else {
                        traceObject(vm, thread, trace, null, String.format("%s[%s]", variable, i), values.get(i), visitedClasses);
                    }
                }

            } else {
                // Non-primitive datatype => use inspector methods to inspect state
                ClassType classType = (ClassType) value.type();

                // Check if we're not back at the same class, this is to prevent infinite recursion.
                if (!visitedClasses.contains(classType.name())) {
                    visitedClasses.add(classType.name());

                    // (1) Inspector methods
                    for (Method method : classType.methods()) {
                        Matcher matcher = Pattern.compile("([^(]+)\\(").matcher(method.toString());
                        if (matcher.find() && inspectorMethods.contains(matcher.group(1))) {
                            // Execute inspector method
                            Value evaluatedValue = Debugger.evaluate(String.format("%s.%s()", variable, method.name()), vm, thread.frame(0));
                            // Check if casting is needed
                            String returnType = method.returnTypeName();
                            if (varType.equals(classType.name())) {
                                traceObject(vm, thread, trace, returnType, String.format("%s.%s()", variable, method.name()), evaluatedValue, visitedClasses);
                            } else {
                                String castToClass = classType.name().replace('$', '.');
                                traceObject(vm, thread, trace, returnType, String.format("((%s) %s).%s()", castToClass, variable, method.name()), evaluatedValue, visitedClasses);
                            }
                        }
                    }

                    // (2) Public member fields
                    ObjectReference objectRef = (ObjectReference) value;
                    for (Field field : classType.visibleFields()) {
                        if (field.isPublic()) {
                            traceObject(vm, thread, trace, field.typeName(), String.format("%s.%s", variable, field.name()), objectRef.getValue(field), visitedClasses);
                        }
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

        } else {
            // null
            trace.put(key, JSONObject.NULL);
        }
    }

}

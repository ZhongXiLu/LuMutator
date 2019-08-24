package lumutator.tracer.debugger;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.tools.example.debug.expr.ExpressionParser;
import lumutator.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Interface for JDI; created to debug a single test class.
 * Reference: http://itsallbinary.com/java-debug-interface-api-jdi-hello-world-example-programmatic-stepping-through-the-code-lines/
 */
public class Debugger {

    /**
     * Observer to create a tracefile.
     */
    private Observer observer;

    /**
     * Connector that connects LuMutator to the program to debug.
     */
    private LaunchingConnector launchingConnector;

    /**
     * Environment of the VM.
     */
    private Map<String, Connector.Argument> env;

    /**
     * The class to debug.
     */
    private String classToDebug;

    /**
     * List of all the methods where there's a breakpoint.
     */
    private List<String> breakpoints = new ArrayList<>();

    /**
     * Set up connector and some options.
     *
     * @param config       The configuration.
     * @param classToDebug The class to debug.
     * @param observer     Observer to be used at each breakpoint to observe the current state.
     */
    public Debugger(Configuration config, String classToDebug, Observer observer) {
        //System.out.println("=== Debugging " + classToDebug + " ===");
        this.classToDebug = classToDebug;
        this.observer = observer;

        // Prepare connector
        launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();

        // Set some options
        env = launchingConnector.defaultArguments();
        env.get("main").setValue(config.get("testRunner") + " " + classToDebug);
        if (config.hasParameter("javaHome")) {
            env.get("home").setValue(config.get("javaHome"));
        }
        env.get("options").setValue(String.format("-classpath %s ", config.get("classPath")));
    }

    /**
     * Evaluate an expression.
     *
     * @param expression The expression to be evaluated.
     * @param vm         The current running virtual machine.
     * @param stackFrame The current stack frame.
     * @return The result of the expression, null if it failed.
     */
    public static Value evaluate(String expression, VirtualMachine vm, final StackFrame stackFrame) {
        ExpressionParser.GetFrame frameGetter;
        frameGetter = new ExpressionParser.GetFrame() {
            @Override
            public StackFrame get() {
                return stackFrame;
            }
        };
        try {
            return ExpressionParser.evaluate(expression, vm, frameGetter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Add breakpoint at start of a method.
     *
     * @param method The method where the breakpoint is added.
     */
    public void addBreakpoint(String method) {
        breakpoints.add(method);
    }

    /**
     * Start the VM.
     */
    public void run() {
        try {
            VirtualMachine vm = launchingConnector.launch(env);

            // Create initial prepare event
            ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
            classPrepareRequest.addClassFilter(classToDebug);
            classPrepareRequest.enable();

            String currentMethod = "";  // store the current method we're in, so we can determine the amount of steps
            EventSet eventSet;
            while ((eventSet = vm.eventQueue().remove(100)) != null) {

                for (Event event : eventSet) {
                    //System.out.println(event);

                    if (event instanceof ClassPrepareEvent) {
                        final ClassPrepareEvent evt = (ClassPrepareEvent) event;
                        final ClassType classType = (ClassType) evt.referenceType();

                        // Set a breakpoint at the start of each method
                        // TODO: also test extended methods?
                        for (Method method : classType.methods()) {
                            if (breakpoints.contains(method.name())) {
                                List<Location> locations = method.allLineLocations();
                                if (!locations.isEmpty()) {
                                    BreakpointRequest breakRequest = vm.eventRequestManager().createBreakpointRequest(locations.get(0));
                                    breakRequest.enable();
                                }
                            }
                        }
                    }

                    if (event instanceof BreakpointEvent || event instanceof StepEvent) {
                        final ThreadReference thread;
                        final Location location;
                        if (event instanceof BreakpointEvent) {
                            thread = ((BreakpointEvent) event).thread();
                            location = ((BreakpointEvent) event).location();
                        } else {
                            thread = ((StepEvent) event).thread();
                            location = ((StepEvent) event).location();
                        }

                        if (event instanceof BreakpointEvent) {
                            event.request().disable();

                            // At start of method
                            currentMethod = location.method().name();

                            // Create new step (over) request
                            StepRequest stepRequest = event.virtualMachine().eventRequestManager().createStepRequest(
                                    thread, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
                            stepRequest.enable();
                        }

                        // Check if still in current method
                        if (!currentMethod.equals(location.method().name())) {
                            // Out of method => stop step request
                            event.request().disable();
                        } else {
                            // Observe current state
                            observer.observe(vm, thread, location);
                        }
                    }

                    vm.resume();
                }
            }

        } catch (VMDisconnectedException e) {
            //System.out.println("VM is now disconnected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

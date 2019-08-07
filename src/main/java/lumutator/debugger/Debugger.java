package lumutator.debugger;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import lumutator.Configuration;

import java.util.List;
import java.util.Map;


/**
 * Interface for JDI; created to debug a single test class.
 * Based on: http://itsallbinary.com/java-debug-interface-api-jdi-hello-world-example-programmatic-debugging-for-beginners/
 */
public class Debugger {

    /**
     * Connector that connects LuMutator to the program to debug.
     */
    private LaunchingConnector launchingConnector;

    /**
     * The virtual machine that is running the program to debug.
     */
    private VirtualMachine vm = null;

    /**
     * Environment of the VM.
     */
    private Map<String, Connector.Argument> env;

    /**
     * The class to debug.
     */
    private String classToDebug;

    /**
     * Set up connector and some options.
     *
     * @param config       The configuration.
     * @param classToDebug The class to debug.
     */
    public Debugger(Configuration config, String classToDebug) {
        this.classToDebug = classToDebug;

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
     * Start the VM.
     */
    public void run() {
        try {
            vm = launchingConnector.launch(env);

            // Create initial prepare event
            ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
            classPrepareRequest.addClassFilter(classToDebug);
            classPrepareRequest.enable();
            EventSet eventSet = null;

            while ((eventSet = vm.eventQueue().remove(100)) != null) {

                for (Event event : eventSet) {
                    //System.out.println(event);

                    if (event instanceof ClassPrepareEvent) {
                        ClassPrepareEvent evt = (ClassPrepareEvent) event;
                        ClassType classType = (ClassType) evt.referenceType();

                        for (Method method : classType.methods()) {
                            List<Location> locations =  method.allLineLocations();
                            if (!locations.isEmpty()) {
                                BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(locations.get(0));
                                bpReq.enable();
                            }
                        }
                    }

                    if (event instanceof BreakpointEvent) {
                        // disable the breakpoint event
                        event.request().disable();

                        // get values of all variables that are visible and print
                        StackFrame stackFrame = ((BreakpointEvent) event).thread().frame(0);
                        Map<LocalVariable, Value> visibleVariables = (Map<LocalVariable, Value>) stackFrame
                                .getValues(stackFrame.visibleVariables());
                        System.out.println("Local Variables =");
                        for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
                            System.out.println("	" + entry.getKey().name() + " = " + entry.getValue());
                        }

                    }
                    vm.resume();

                }
            }
        } catch (VMDisconnectedException e) {
            System.out.println("VM is now disconnected.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

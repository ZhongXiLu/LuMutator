package lumutator.debugger;

import lumutator.Configuration;

import java.io.*;

/**
 * Java Debugger interface for JDB.
 */
public class Debugger implements Runnable {

    /**
     * The process builder for the debug process.
     */
    private ProcessBuilder processBuilder;

    /**
     * Writer of the debugger process.
     */
    private BufferedWriter writer = null;

    /**
     * Constructor.
     *
     * @param config        The configuration.
     * @param classToDebug  The class to debug.
     */
    public Debugger(Configuration config, String classToDebug) {
        this.processBuilder = new ProcessBuilder(
                "jdb",
                "-sourcepath", config.get("sourcePath"),
                "-classpath", config.get("classPath"),
                config.get("testRunner"), classToDebug
        );
        this.processBuilder.redirectErrorStream(true);   // error and out stream in one combined stream
        this.processBuilder.directory(new File(config.get("projectDir")));
    }

    /**
     * Start the debugger process (JDB).
     */
    @Override
    public void run() {
        try {
            Process process = this.processBuilder.start();

            this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            // TODO: redirect output to file?
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
            this.writer.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a command to the debugger.
     *
     * @param command The command.
     */
    public void write(String command) {
        if (this.writer != null) {
            try {
                this.writer.write(command + "\n");
                this.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check whether the debugging process is running.
     *
     * @return True if running.
     */
    public boolean isRunning() {
        return (this.writer != null);   // equivalent to checking whether the writer is set to null or not
    }
}

package lumutator.purity;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link PurityAnalyzer}; only test the parsing and retrieving the inspector methods from the analysis.
 */
public class PurityAnalyzerTest {

    /**
     * Some test lines.
     */
    final private String line1 = "package.class{ public void methodName() } => domain-specific side-effect free\n";
    final private String line2 = "nested.package.class{ static private String methodName2(int,int) } => impure\n";
    final private String line3 = "class{ package.class methodName3(package.otherClass) } => compile time pure\n";

    /**
     * Test the {@link PurityAnalyzer#getMethodName(String)} method.
     */
    @Test
    public void testGetMethodName() {
        try {
            Method method = PurityAnalyzer.class.getDeclaredMethod("getMethodName", String.class);
            method.setAccessible(true);
            assertEquals("package.class.methodName", method.invoke(null, line1));
            assertEquals("nested.package.class.methodName2", method.invoke(null, line2));
            assertEquals("class.methodName3", method.invoke(null, line3));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Test the {@link PurityAnalyzer#getPurityResult(String)} method.
     */
    @Test
    public void testGetPurityResult() {
        try {
            Method method = PurityAnalyzer.class.getDeclaredMethod("getPurityResult", String.class);
            method.setAccessible(true);
            assertEquals("domain-specific side-effect free", method.invoke(null, line1));
            assertEquals("impure", method.invoke(null, line2));
            assertEquals("compile time pure", method.invoke(null, line3));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Test the {@link PurityAnalyzer#getMethodParameters(String)} method.
     */
    @Test
    public void testGetMethodParameters() {
        try {
            Method method = PurityAnalyzer.class.getDeclaredMethod("getMethodParameters", String.class);
            method.setAccessible(true);
            assertEquals("", method.invoke(null, line1));
            assertEquals("int,int", method.invoke(null, line2));
            assertEquals("package.otherClass", method.invoke(null, line3));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Test the {@link PurityAnalyzer#getReturnType(String)} method.
     */
    @Test
    public void testGetReturnType() {
        try {
            Method method = PurityAnalyzer.class.getDeclaredMethod("getReturnType", String.class);
            method.setAccessible(true);
            assertEquals("void", method.invoke(null, line1));
            assertEquals("String", method.invoke(null, line2));
            assertEquals("package.class", method.invoke(null, line3));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Test the {@link PurityAnalyzer#getAccessModifier(String)} method.
     */
    @Test
    public void testGetAccessModifier() {
        try {
            Method method = PurityAnalyzer.class.getDeclaredMethod("getAccessModifier", String.class);
            method.setAccessible(true);
            assertEquals("public", method.invoke(null, line1));
            assertEquals("private", method.invoke(null, line2));
            assertEquals("default", method.invoke(null, line3));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Test the {@link PurityAnalyzer#getInspectorMethodIfExists(String, boolean)} method.
     */
    @Test
    public void testGetInspectorMethodIfExists() {
        try {
            Method method = PurityAnalyzer.class.getDeclaredMethod("getInspectorMethodIfExists", String.class, boolean.class);
            method.setAccessible(true);
            // "" means it is not an inspector method
            assertEquals("", method.invoke(null, line1, true));
            assertEquals("", method.invoke(null, line2, false));
            assertEquals("", method.invoke(null, line3, true));
            final String line4 = "class{ private int pureMethod() } => compile time pure\n";
            assertEquals("", method.invoke(null, line4, true));
            assertEquals("class.pureMethod", method.invoke(null, line4, false));
            final String line5 = "class{ public int pureMethod() } => compile time pure\n";
            assertEquals("class.pureMethod", method.invoke(null, line5, true));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Should not be possible
            fail();
        }
    }
}

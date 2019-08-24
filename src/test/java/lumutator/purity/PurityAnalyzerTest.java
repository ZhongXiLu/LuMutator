package lumutator.purity;

import org.junit.Test;

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
    final private String line3 = "class{ private package.class methodName3(package.otherClass) } => compile time pure\n";

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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test the {@link PurityAnalyzer#getInspectorMethodIfExists(String)} method.
     */
    @Test
    public void testGetInspectorMethodIfExists() {
        try {
            Method method = PurityAnalyzer.class.getDeclaredMethod("getInspectorMethodIfExists", String.class);
            method.setAccessible(true);
            // "" means it is not an inspector method
            assertEquals("", method.invoke(null, line1));
            assertEquals("", method.invoke(null, line2));
            assertEquals("", method.invoke(null, line3));
            final String line4 = "class{ private int pureMethod() } => compile time pure\n";
            assertEquals("class.pureMethod", method.invoke(null, line4));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}

package liquibase;

import org.junit.jupiter.api.Test;
import liquibase.ContextExpression;
import java.util.Set;
import org.springframework.test.util.ReflectionTestUtils;
import liquibase.Contexts;
import org.mockito.Mockito;
import liquibase.util.StringUtil;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class ContextExpressionReproducedTests {

    @Test
    void matchesReproducedTest() {
        ContextExpression v = new ContextExpression();
        ReflectionTestUtils.setField(v, "contexts", Set.of("ctx1"));
        ReflectionTestUtils.setField(v, "originalString", "ctx1");
        Contexts v1 = new Contexts();
        ReflectionTestUtils.setField(v1, "contextStore", Set.of("ctx1"));
        MockedStatic<StringUtil> staticMockUtil = Mockito.mockStatic(StringUtil.class, Mockito.CALLS_REAL_METHODS);
        String v2 = "ctx1";
        staticMockUtil.when(() -> StringUtil.trimToEmpty(Mockito.<String>any())).thenReturn(v2);
        boolean v3 = v.matches(v1);
        Assertions.assertTrue(v3);
        staticMockUtil.close();
    }

    @Test
    void matchesReproducedTest1() {
        ContextExpression v = new ContextExpression();
        ReflectionTestUtils.setField(v, "contexts", Set.of("ctx1"));
        ReflectionTestUtils.setField(v, "originalString", "ctx1");
        Contexts v1 = new Contexts();
        ReflectionTestUtils.setField(v1, "contextStore", Set.of("ctx2"));
        MockedStatic<StringUtil> staticMockUtil = Mockito.mockStatic(StringUtil.class, Mockito.CALLS_REAL_METHODS);
        String v2 = "ctx1";
        staticMockUtil.when(() -> StringUtil.trimToEmpty(Mockito.<String>any())).thenReturn(v2);
        boolean v3 = v.matches(v1);
        Assertions.assertFalse(v3);
        staticMockUtil.close();
    }

    @Test
    void matchesReproducedTest2() {
        ContextExpression v = new ContextExpression();
        ReflectionTestUtils.setField(v, "contexts", Set.of("ctx2"));
        ReflectionTestUtils.setField(v, "originalString", "ctx2");
        Contexts v1 = new Contexts();
        ReflectionTestUtils.setField(v1, "contextStore", Set.of("ctx1"));
        MockedStatic<StringUtil> staticMockUtil = Mockito.mockStatic(StringUtil.class, Mockito.CALLS_REAL_METHODS);
        String v2 = "ctx2";
        staticMockUtil.when(() -> StringUtil.trimToEmpty(Mockito.<String>any())).thenReturn(v2);
        boolean v3 = v.matches(v1);
        Assertions.assertFalse(v3);
        staticMockUtil.close();
    }

    @Test
    void matchesReproducedTest23() {
        ContextExpression v = new ContextExpression();
        ReflectionTestUtils.setField(v, "contexts", Set.of("ctx2"));
        ReflectionTestUtils.setField(v, "originalString", "ctx2");
        Contexts v1 = new Contexts();
        ReflectionTestUtils.setField(v1, "contextStore", Set.of("ctx2"));
        MockedStatic<StringUtil> staticMockUtil = Mockito.mockStatic(StringUtil.class, Mockito.CALLS_REAL_METHODS);
        String v2 = "ctx2";
        staticMockUtil.when(() -> StringUtil.trimToEmpty(Mockito.<String>any())).thenReturn(v2);
        boolean v3 = v.matches(v1);
        Assertions.assertTrue(v3);
        staticMockUtil.close();
    }

}

package com.puppycrawl.tools.checkstyle.checks.whitespace;

import org.junit.jupiter.api.Test;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import com.puppycrawl.tools.checkstyle.ThreadModeSettings;
import com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAfterCheck;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import java.util.Set;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import java.lang.*;
import static java.lang.ThreadLocal.SuppliedThreadLocal;

class WhitespaceAfterCheckReproducedTests {

    @Test
    void visitTokenReproducedTest() {
        DefaultConfiguration v = new DefaultConfiguration("com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAfterCheck");
        ReflectionTestUtils.setField(v, "name", "com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAfterCheck");
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        WhitespaceAfterCheck v1 = new WhitespaceAfterCheck();
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of());
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.ERROR);
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(45);
        Mockito.when(mocked1.getLineNo()).thenReturn(26);
        Mockito.when(mocked1.getColumnNo()).thenReturn(50);
        Mockito.when(mocked1.getText()).thenReturn(";");
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest1() {
        DefaultConfiguration v = new DefaultConfiguration("com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAfterCheck");
        ReflectionTestUtils.setField(v, "name", "com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAfterCheck");
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        WhitespaceAfterCheck v1 = new WhitespaceAfterCheck();
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of());
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.ERROR);
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        Mockito.when(mocked1.getLineNo()).thenReturn(29);
        Mockito.when(mocked1.getColumnNo()).thenReturn(8);
        Mockito.when(mocked1.getText()).thenReturn("if");
        v1.visitToken(mocked1);
    }

}

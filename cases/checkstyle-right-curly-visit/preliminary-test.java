package com.puppycrawl.tools.checkstyle.checks.blocks;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Map;
import com.puppycrawl.tools.checkstyle.ThreadModeSettings;
import com.puppycrawl.tools.checkstyle.checks.blocks.RightCurlyCheck;
import com.puppycrawl.tools.checkstyle.checks.blocks.RightCurlyOption;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import java.util.Set;
import java.lang.*;
import static java.lang.ThreadLocal.SuppliedThreadLocal;

class RightCurlyCheckReproducedTests {

    @Test
    void visitTokenReproducedTest() {
        HashSet<Object> v = new HashSet<Object>();
        v.add("COMPACT_CTOR_DEF");
        v.add("METHOD_DEF");
        v.add("CTOR_DEF");
        v.add("LITERAL_SWITCH");
        v.add("CLASS_DEF");
        v.add("LITERAL_FINALLY");
        v.add("LITERAL_CATCH");
        v.add("ENUM_DEF");
        v.add("INTERFACE_DEF");
        v.add("ANNOTATION_DEF");
        v.add("INSTANCE_INIT");
        v.add("LITERAL_FOR");
        v.add("LITERAL_DEFAULT");
        v.add("STATIC_INIT");
        v.add("LITERAL_WHILE");
        v.add("RECORD_DEF");
        v.add("LITERAL_CASE");
        DefaultConfiguration v1 = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v1, "name", "RightCurly");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("tokens", "CLASS_DEF, METHOD_DEF, CTOR_DEF, LITERAL_FOR, LITERAL_WHILE, STATIC_INIT,                     INSTANCE_INIT, ANNOTATION_DEF, ENUM_DEF, INTERFACE_DEF, RECORD_DEF,                     COMPACT_CTOR_DEF, LITERAL_SWITCH, LITERAL_CASE, LITERAL_DEFAULT,                     LITERAL_FINALLY, LITERAL_CATCH", "id", "RightCurlyAlone", "option", "alone"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v2 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v2, "option", RightCurlyOption.ALONE);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v2, "context", mocked);
        ReflectionTestUtils.setField(v2, "tokens", v);
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "id", "RightCurlyAlone");
        ReflectionTestUtils.setField(v2, "configuration", v1);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(14);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getLineNo()).thenReturn(108);
        Mockito.when(mocked3.getColumnNo()).thenReturn(0);
        Mockito.when(mocked2.getLastChild()).thenReturn(mocked3);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        v2.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest1() {
        HashSet<Object> v = new HashSet<Object>();
        v.add("COMPACT_CTOR_DEF");
        v.add("METHOD_DEF");
        v.add("CTOR_DEF");
        v.add("LITERAL_SWITCH");
        v.add("CLASS_DEF");
        v.add("LITERAL_FINALLY");
        v.add("LITERAL_CATCH");
        v.add("ENUM_DEF");
        v.add("INTERFACE_DEF");
        v.add("ANNOTATION_DEF");
        v.add("INSTANCE_INIT");
        v.add("LITERAL_FOR");
        v.add("LITERAL_DEFAULT");
        v.add("STATIC_INIT");
        v.add("LITERAL_WHILE");
        v.add("RECORD_DEF");
        v.add("LITERAL_CASE");
        DefaultConfiguration v1 = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v1, "name", "RightCurly");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("tokens", "CLASS_DEF, METHOD_DEF, CTOR_DEF, LITERAL_FOR, LITERAL_WHILE, STATIC_INIT,                     INSTANCE_INIT, ANNOTATION_DEF, ENUM_DEF, INTERFACE_DEF, RECORD_DEF,                     COMPACT_CTOR_DEF, LITERAL_SWITCH, LITERAL_CASE, LITERAL_DEFAULT,                     LITERAL_FINALLY, LITERAL_CATCH", "id", "RightCurlyAlone", "option", "alone"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v2 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v2, "option", RightCurlyOption.ALONE);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v2, "context", mocked);
        ReflectionTestUtils.setField(v2, "tokens", v);
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "id", "RightCurlyAlone");
        ReflectionTestUtils.setField(v2, "configuration", v1);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(9);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getLineNo()).thenReturn(107);
        Mockito.when(mocked3.getColumnNo()).thenReturn(4);
        Mockito.when(mocked2.getLastChild()).thenReturn(mocked3);
        Mockito.when(mocked1.findFirstToken(Mockito.anyInt())).thenReturn(mocked2);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked4.getLineNo()).thenReturn(108);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked4);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        v2.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest2() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(27);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(12);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(26);
        Mockito.when(mocked4.getColumnNo()).thenReturn(6);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest3() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(26);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(13);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(25);
        Mockito.when(mocked4.getColumnNo()).thenReturn(8);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest4() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(25);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(14);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(24);
        Mockito.when(mocked4.getColumnNo()).thenReturn(10);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest5() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(24);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(15);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(23);
        Mockito.when(mocked4.getColumnNo()).thenReturn(12);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest6() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(23);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(16);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(22);
        Mockito.when(mocked4.getColumnNo()).thenReturn(14);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest7() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(22);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(17);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(21);
        Mockito.when(mocked4.getColumnNo()).thenReturn(16);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest8() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(21);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(18);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(20);
        Mockito.when(mocked4.getColumnNo()).thenReturn(18);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest9() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(20);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked4.getParent()).thenReturn(mocked3);
        Mockito.when(mocked4.getLineNo()).thenReturn(19);
        Mockito.when(mocked4.getColumnNo()).thenReturn(20);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked3.getLineNo()).thenReturn(18);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest10() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(43);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(28);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(42);
        Mockito.when(mocked4.getColumnNo()).thenReturn(6);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest11() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(42);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(29);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(41);
        Mockito.when(mocked4.getColumnNo()).thenReturn(8);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest12() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(41);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(30);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(40);
        Mockito.when(mocked4.getColumnNo()).thenReturn(10);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest13() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(40);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(31);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(39);
        Mockito.when(mocked4.getColumnNo()).thenReturn(12);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest14() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(39);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(32);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(38);
        Mockito.when(mocked4.getColumnNo()).thenReturn(14);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest15() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(38);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(33);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(37);
        Mockito.when(mocked4.getColumnNo()).thenReturn(16);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest16() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(37);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(34);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(36);
        Mockito.when(mocked4.getColumnNo()).thenReturn(18);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest17() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(36);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked4.getParent()).thenReturn(mocked3);
        Mockito.when(mocked4.getLineNo()).thenReturn(35);
        Mockito.when(mocked4.getColumnNo()).thenReturn(20);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked3.getLineNo()).thenReturn(34);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest18() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(59);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(44);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(58);
        Mockito.when(mocked4.getColumnNo()).thenReturn(6);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest19() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(58);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(45);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(57);
        Mockito.when(mocked4.getColumnNo()).thenReturn(8);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest20() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(57);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(46);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(56);
        Mockito.when(mocked4.getColumnNo()).thenReturn(10);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest21() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(56);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(47);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(55);
        Mockito.when(mocked4.getColumnNo()).thenReturn(12);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest22() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(55);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(48);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(54);
        Mockito.when(mocked4.getColumnNo()).thenReturn(14);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest23() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(54);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(49);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(53);
        Mockito.when(mocked4.getColumnNo()).thenReturn(16);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest24() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(53);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(50);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(52);
        Mockito.when(mocked4.getColumnNo()).thenReturn(18);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest25() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(52);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked4.getParent()).thenReturn(mocked3);
        Mockito.when(mocked4.getLineNo()).thenReturn(51);
        Mockito.when(mocked4.getColumnNo()).thenReturn(20);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked3.getLineNo()).thenReturn(50);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest26() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(75);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(60);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(74);
        Mockito.when(mocked4.getColumnNo()).thenReturn(6);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest27() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(74);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(61);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(73);
        Mockito.when(mocked4.getColumnNo()).thenReturn(8);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest28() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(73);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(62);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(72);
        Mockito.when(mocked4.getColumnNo()).thenReturn(10);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest29() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(72);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(63);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(71);
        Mockito.when(mocked4.getColumnNo()).thenReturn(12);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest30() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(71);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(64);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(70);
        Mockito.when(mocked4.getColumnNo()).thenReturn(14);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest31() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(70);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(65);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(69);
        Mockito.when(mocked4.getColumnNo()).thenReturn(16);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest32() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(69);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(66);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(68);
        Mockito.when(mocked4.getColumnNo()).thenReturn(18);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest33() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(68);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked4.getParent()).thenReturn(mocked3);
        Mockito.when(mocked4.getLineNo()).thenReturn(67);
        Mockito.when(mocked4.getColumnNo()).thenReturn(20);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked3.getLineNo()).thenReturn(66);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest34() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(91);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(76);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(90);
        Mockito.when(mocked4.getColumnNo()).thenReturn(6);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest35() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(90);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(77);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(89);
        Mockito.when(mocked4.getColumnNo()).thenReturn(8);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest36() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(89);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(78);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(88);
        Mockito.when(mocked4.getColumnNo()).thenReturn(10);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest37() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(88);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(79);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(87);
        Mockito.when(mocked4.getColumnNo()).thenReturn(12);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest38() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(87);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(80);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(86);
        Mockito.when(mocked4.getColumnNo()).thenReturn(14);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest39() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(86);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(81);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(85);
        Mockito.when(mocked4.getColumnNo()).thenReturn(16);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest40() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(85);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(82);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(84);
        Mockito.when(mocked4.getColumnNo()).thenReturn(18);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest41() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(84);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked4.getParent()).thenReturn(mocked3);
        Mockito.when(mocked4.getLineNo()).thenReturn(83);
        Mockito.when(mocked4.getColumnNo()).thenReturn(20);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked3.getLineNo()).thenReturn(82);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest42() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(107);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(92);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(106);
        Mockito.when(mocked4.getColumnNo()).thenReturn(6);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest43() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(106);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(93);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(105);
        Mockito.when(mocked4.getColumnNo()).thenReturn(8);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest44() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(105);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(94);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(104);
        Mockito.when(mocked4.getColumnNo()).thenReturn(10);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest45() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(104);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(95);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(103);
        Mockito.when(mocked4.getColumnNo()).thenReturn(12);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest46() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(103);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(96);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(102);
        Mockito.when(mocked4.getColumnNo()).thenReturn(14);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest47() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(102);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(97);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(101);
        Mockito.when(mocked4.getColumnNo()).thenReturn(16);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest48() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(101);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        DetailAstImpl mocked5 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked5.getLineNo()).thenReturn(98);
        Mockito.when(mocked4.getPreviousSibling()).thenReturn(mocked5);
        Mockito.when(mocked4.getLineNo()).thenReturn(100);
        Mockito.when(mocked4.getColumnNo()).thenReturn(18);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }

    @Test
    void visitTokenReproducedTest49() {
        DefaultConfiguration v = new DefaultConfiguration("RightCurly");
        ReflectionTestUtils.setField(v, "name", "RightCurly");
        ReflectionTestUtils.setField(v, "propertyMap", Map.of("tokens", "LITERAL_TRY, LITERAL_CATCH, LITERAL_IF, LITERAL_ELSE,                     LITERAL_DO", "id", "RightCurlySame"));
        ReflectionTestUtils.setField(v, "threadModeSettings", new ThreadModeSettings(1, 1));
        RightCurlyCheck v1 = new RightCurlyCheck();
        ReflectionTestUtils.setField(v1, "option", RightCurlyOption.SAME);
        SuppliedThreadLocal<Object> mocked = Mockito.mock(SuppliedThreadLocal.class);
        Mockito.when(mocked.get()).thenReturn(Mockito.mock(Object.class));
        ReflectionTestUtils.setField(v1, "context", mocked);
        ReflectionTestUtils.setField(v1, "tokens", Set.of("LITERAL_ELSE", "LITERAL_TRY", "LITERAL_CATCH", "LITERAL_IF", "LITERAL_DO"));
        ReflectionTestUtils.setField(v1, "tabWidth", 8);
        ReflectionTestUtils.setField(v1, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v1, "id", "RightCurlySame");
        ReflectionTestUtils.setField(v1, "configuration", v);
        DetailAstImpl mocked1 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked1.getType()).thenReturn(83);
        DetailAstImpl mocked2 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked2.getLineNo()).thenReturn(100);
        Mockito.when(mocked1.getNextSibling()).thenReturn(mocked2);
        Mockito.when(mocked1.getParent()).thenReturn(Mockito.mock(DetailAstImpl.class));
        DetailAstImpl mocked3 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked3.getType()).thenReturn(7);
        DetailAstImpl mocked4 = Mockito.mock(DetailAstImpl.class);
        Mockito.when(mocked4.getParent()).thenReturn(mocked3);
        Mockito.when(mocked4.getLineNo()).thenReturn(99);
        Mockito.when(mocked4.getColumnNo()).thenReturn(20);
        Mockito.when(mocked3.getLastChild()).thenReturn(mocked4);
        Mockito.when(mocked3.getLineNo()).thenReturn(98);
        Mockito.when(mocked1.getLastChild()).thenReturn(mocked3);
        v1.visitToken(mocked1);
    }
}

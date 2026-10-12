package com.puppycrawl.tools.checkstyle.checks.indentation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.puppycrawl.tools.checkstyle.checks.indentation.PrimordialHandler;
import java.util.ArrayDeque;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Map;
import com.puppycrawl.tools.checkstyle.ThreadModeSettings;
import com.puppycrawl.tools.checkstyle.checks.indentation.IndentationCheck;
import com.puppycrawl.tools.checkstyle.checks.indentation.LineWrappingHandler;
import com.puppycrawl.tools.checkstyle.checks.indentation.HandlerFactory;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.checks.indentation.AbstractExpressionHandler;
import com.puppycrawl.tools.checkstyle.checks.indentation.PackageDefHandler;
import org.mockito.invocation.InvocationOnMock;
import java.util.Set;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.checks.indentation.ClassDefHandler;
import com.puppycrawl.tools.checkstyle.checks.indentation.ObjectBlockHandler;
import com.puppycrawl.tools.checkstyle.checks.indentation.MethodDefHandler;
import com.puppycrawl.tools.checkstyle.checks.indentation.SlistHandler;
import com.puppycrawl.tools.checkstyle.checks.indentation.IfHandler;
import java.lang.*;
import static java.lang.ThreadLocal.SuppliedThreadLocal;

class IndentationCheckReproducedTests {

    @Test
    void visitTokenReproducedTest() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(PackageDefHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest1() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(ClassDefHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest2() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(ObjectBlockHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest3() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(MethodDefHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest4() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(SlistHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest5() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(IfHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest6() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(SlistHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest7() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(IfHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest8() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(SlistHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest9() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(IfHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest10() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(SlistHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest11() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(IfHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest12() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(SlistHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest13() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(IfHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest14() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(SlistHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest15() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(IfHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest16() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(SlistHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest17() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(IfHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest18() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(SlistHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest19() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(IfHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

    @Test
    void visitTokenReproducedTest20() {
        ArrayDeque<Object> v = new ArrayDeque<Object>();
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(IfHandler.class));
        v.addLast(Mockito.mock(SlistHandler.class));
        v.addLast(Mockito.mock(MethodDefHandler.class));
        v.addLast(Mockito.mock(ObjectBlockHandler.class));
        v.addLast(Mockito.mock(ClassDefHandler.class));
        v.addLast(Mockito.mock(PrimordialHandler.class));
        DefaultConfiguration v1 = new DefaultConfiguration("Indentation");
        ReflectionTestUtils.setField(v1, "name", "Indentation");
        ReflectionTestUtils.setField(v1, "propertyMap", Map.of("throwsIndent", "4", "arrayInitIndent", "2", "caseIndent", "2", "basicOffset", "2", "braceAdjustment", "2", "lineWrappingIndentation", "4"));
        ReflectionTestUtils.setField(v1, "threadModeSettings", new ThreadModeSettings(1, 1));
        IndentationCheck v2 = new IndentationCheck();
        ReflectionTestUtils.setField(v2, "handlers", v);
        ReflectionTestUtils.setField(v2, "lineWrappingHandler", Mockito.mock(LineWrappingHandler.class));
        HandlerFactory mocked = Mockito.mock(HandlerFactory.class);
        Mockito.when(mocked.getHandler(Mockito.<IndentationCheck>any(), Mockito.<DetailAST>any(), Mockito.<AbstractExpressionHandler>any())).thenReturn(Mockito.mock(SlistHandler.class));
        ReflectionTestUtils.setField(v2, "handlerFactory", mocked);
        ReflectionTestUtils.setField(v2, "incorrectIndentationLines", Set.of());
        ReflectionTestUtils.setField(v2, "basicOffset", 2);
        ReflectionTestUtils.setField(v2, "caseIndent", 2);
        ReflectionTestUtils.setField(v2, "braceAdjustment", 2);
        ReflectionTestUtils.setField(v2, "throwsIndent", 4);
        ReflectionTestUtils.setField(v2, "arrayInitIndent", 2);
        ReflectionTestUtils.setField(v2, "lineWrappingIndentation", 4);
        ReflectionTestUtils.setField(v2, "forceStrictCondition", false);
        ReflectionTestUtils.setField(v2, "context", Mockito.mock(SuppliedThreadLocal.class));
        ReflectionTestUtils.setField(v2, "tokens", Set.of());
        ReflectionTestUtils.setField(v2, "tabWidth", 8);
        ReflectionTestUtils.setField(v2, "severityLevel", SeverityLevel.WARNING);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        v2.visitToken(Mockito.mock(DetailAstImpl.class));
    }

}

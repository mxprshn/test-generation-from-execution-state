package com.puppycrawl.tools.checkstyle;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.puppycrawl.tools.checkstyle.JavaParser;
import org.mockito.MockedStatic;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import org.mockito.invocation.InvocationOnMock;
import java.io.File;
import java.io.IOException;
import java.lang.*;
import static com.puppycrawl.tools.checkstyle.JavaParser.Options;

class JavaParserReproducedTests {

    @Test
    void parseFileReproducedTest() throws CheckstyleException, IOException {
        MockedStatic<JavaParser> staticMockUtil = Mockito.mockStatic(JavaParser.class, Mockito.CALLS_REAL_METHODS);
        DetailAST v = Mockito.mock(DetailAstImpl.class);
        staticMockUtil.when(() -> JavaParser.parseFileText(Mockito.<FileText>any(), Mockito.<Options>any())).thenReturn(v);
        JavaParser.parseFile(new File(new File(JavaParserReproducedTests.class.getResource("/javaParserReproducedTests/java.io.File_1.java").getFile()).getAbsolutePath()), Options.WITH_COMMENTS);
        staticMockUtil.close();
    }
}

package org.jsoup.parser;

import org.junit.jupiter.api.Test;
import org.jsoup.parser.TokenQueue;
import org.mockito.Mockito;
import org.jsoup.parser.CharacterReader;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.invocation.InvocationOnMock;
import org.junit.jupiter.api.Assertions;
import java.lang.*;
import static org.jsoup.parser.CharacterReader.CharPredicate;

class TokenQueueReproducedTests {

    @Test
    void consumeCssIdentifierReproducedTest() throws Throwable {
        TokenQueue v = new TokenQueue("div.-\\34 a");
        CharacterReader mocked = Mockito.mock(CharacterReader.class);
        Mockito.when(mocked.isEmpty()).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);
        Mockito.when(ReflectionTestUtils.<String>invokeMethod(mocked, "consumeMatching", Mockito.<CharPredicate>any())).thenReturn("-");
        Mockito.when(mocked.current()).thenReturn('\\').thenReturn('\\').thenReturn('3').thenReturn(' ').thenReturn('a');
        Mockito.when(mocked.consume()).thenReturn('3').thenReturn('a');
        Mockito.when(ReflectionTestUtils.<String>invokeMethod(mocked, "consumeMatching", Mockito.<CharPredicate>any(), Mockito.anyInt())).thenReturn("34");
        ReflectionTestUtils.setField(v, "reader", mocked);
        String v1 = v.consumeCssIdentifier();
        Assertions.assertEquals("-4a", v1);
    }

    @Test
    void consumeCssIdentifierReproducedTest1() throws Throwable {
        TokenQueue v = new TokenQueue("#-\\34 a");
        CharacterReader mocked = Mockito.mock(CharacterReader.class);
        Mockito.when(mocked.isEmpty()).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);
        Mockito.when(ReflectionTestUtils.<String>invokeMethod(mocked, "consumeMatching", Mockito.<CharPredicate>any())).thenReturn("-");
        Mockito.when(mocked.current()).thenReturn('\\').thenReturn('\\').thenReturn('3').thenReturn(' ').thenReturn('a');
        Mockito.when(mocked.consume()).thenReturn('3').thenReturn('a');
        Mockito.when(ReflectionTestUtils.<String>invokeMethod(mocked, "consumeMatching", Mockito.<CharPredicate>any(), Mockito.anyInt())).thenReturn("34");
        ReflectionTestUtils.setField(v, "reader", mocked);
        String v1 = v.consumeCssIdentifier();
        Assertions.assertEquals("-4a", v1);
    }

    @Test
    void consumeCssIdentifierReproducedTest2() throws Throwable {
        TokenQueue v = new TokenQueue("> div.-\\34 a");
        CharacterReader mocked = Mockito.mock(CharacterReader.class);
        Mockito.when(mocked.isEmpty()).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);
        Mockito.when(ReflectionTestUtils.<String>invokeMethod(mocked, "consumeMatching", Mockito.<CharPredicate>any())).thenReturn("-");
        Mockito.when(mocked.current()).thenReturn('\\').thenReturn('\\').thenReturn('3').thenReturn(' ').thenReturn('a');
        Mockito.when(mocked.consume()).thenReturn('3').thenReturn('a');
        Mockito.when(ReflectionTestUtils.<String>invokeMethod(mocked, "consumeMatching", Mockito.<CharPredicate>any(), Mockito.anyInt())).thenReturn("34");
        ReflectionTestUtils.setField(v, "reader", mocked);
        String v1 = v.consumeCssIdentifier();
        Assertions.assertEquals("-4a", v1);
    }

    @Test
    void consumeCssIdentifierReproducedTest3() throws Throwable {
        TokenQueue v = new TokenQueue("html > body > div.-\\34 a");
        CharacterReader mocked = Mockito.mock(CharacterReader.class);
        Mockito.when(mocked.isEmpty()).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);
        Mockito.when(ReflectionTestUtils.<String>invokeMethod(mocked, "consumeMatching", Mockito.<CharPredicate>any())).thenReturn("-");
        Mockito.when(mocked.current()).thenReturn('\\').thenReturn('\\').thenReturn('3').thenReturn(' ').thenReturn('a');
        Mockito.when(mocked.consume()).thenReturn('3').thenReturn('a');
        Mockito.when(ReflectionTestUtils.<String>invokeMethod(mocked, "consumeMatching", Mockito.<CharPredicate>any(), Mockito.anyInt())).thenReturn("34");
        ReflectionTestUtils.setField(v, "reader", mocked);
        String v1 = v.consumeCssIdentifier();
        Assertions.assertEquals("-4a", v1);
    }

    @Test
    void consumeCssIdentifierReproducedTest5() throws Throwable {
        TokenQueue v = new TokenQueue("html > body > #-\\34 a");
        CharacterReader mocked = Mockito.mock(CharacterReader.class);
        Mockito.when(mocked.isEmpty()).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(true);
        Mockito.when(ReflectionTestUtils.<String>invokeMethod(mocked, "consumeMatching", Mockito.<CharPredicate>any())).thenReturn("-");
        Mockito.when(mocked.current()).thenReturn('\\').thenReturn('\\').thenReturn('3').thenReturn(' ').thenReturn('a');
        Mockito.when(mocked.consume()).thenReturn('3').thenReturn('a');
        Mockito.when(ReflectionTestUtils.<String>invokeMethod(mocked, "consumeMatching", Mockito.<CharPredicate>any(), Mockito.anyInt())).thenReturn("34");
        ReflectionTestUtils.setField(v, "reader", mocked);
        String v1 = v.consumeCssIdentifier();
        Assertions.assertEquals("-4a", v1);
    }

}

package org.jsoup.parser;

import org.junit.jupiter.api.Test;
import org.jsoup.parser.TokenQueue;
import org.mockito.Mockito;
import org.jsoup.parser.CharacterReader;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class TokenQueueReproducedTests {

    @Test
    void chompBalancedReproducedTest() {
        TokenQueue v = new TokenQueue("meta[http-equiv=content-type], meta[charset]");
        CharacterReader mocked = Mockito.mock(CharacterReader.class);
        Mockito.when(mocked.isEmpty()).thenReturn(false);
        Mockito.when(mocked.consume()).thenReturn('[').thenReturn('h').thenReturn('t').thenReturn('t').thenReturn('p').thenReturn('-').thenReturn('e').thenReturn('q').thenReturn('u').thenReturn('i');
        ReflectionTestUtils.setField(v, "reader", mocked);
        String v1 = v.chompBalanced('[', ']');
        Assertions.assertEquals("http-equiv=content-type", v1);
    }

    @Test
    void chompBalancedReproducedTest1() {
        TokenQueue v = new TokenQueue("meta[http-equiv=content-type], meta[charset]");
        CharacterReader mocked = Mockito.mock(CharacterReader.class);
        Mockito.when(mocked.isEmpty()).thenReturn(false);
        Mockito.when(mocked.consume()).thenReturn('[').thenReturn('c').thenReturn('h').thenReturn('a').thenReturn('r').thenReturn('s').thenReturn('e').thenReturn('t').thenReturn(']');
        ReflectionTestUtils.setField(v, "reader", mocked);
        String v1 = v.chompBalanced('[', ']');
        Assertions.assertEquals("charset", v1);
    }

    @Test
    void chompBalancedReproducedTest2() {
        TokenQueue v = new TokenQueue("div:has(p:matches((?i)two))");
        CharacterReader mocked = Mockito.mock(CharacterReader.class);
        Mockito.when(mocked.isEmpty()).thenReturn(false);
        Mockito.when(mocked.consume()).thenReturn('(').thenReturn('(').thenReturn('?').thenReturn('i').thenReturn(')').thenReturn('t').thenReturn('w').thenReturn('o').thenReturn(')');
        ReflectionTestUtils.setField(v, "reader", mocked);
        String v1 = v.chompBalanced('(', ')');
        Assertions.assertEquals("(?i)two", v1);
    }

    @Test
    void chompBalancedReproducedTest3() {
        TokenQueue v = new TokenQueue("div:has(p:contains(two))");
        CharacterReader mocked = Mockito.mock(CharacterReader.class);
        Mockito.when(mocked.isEmpty()).thenReturn(false);
        Mockito.when(mocked.consume()).thenReturn('(').thenReturn('t').thenReturn('w').thenReturn('o').thenReturn(')');
        ReflectionTestUtils.setField(v, "reader", mocked);
        String v1 = v.chompBalanced('(', ')');
        Assertions.assertEquals("two", v1);
    }
}

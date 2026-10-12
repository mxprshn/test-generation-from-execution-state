package org.jsoup.internal;

import org.junit.jupiter.api.Test;
import org.jsoup.internal.StringUtil;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class StringUtilReproducedTests {

    @Test
    void normaliseWhitespaceReproducedTest() {
        String v = StringUtil.normaliseWhitespace("Hello");
        Assertions.assertEquals("Hello", v);
    }

    @Test
    void normaliseWhitespaceReproducedTest1() {
        String v = StringUtil.normaliseWhitespace("First");
        Assertions.assertEquals("First", v);
    }

    @Test
    void normaliseWhitespaceReproducedTest3() {
        String v = StringUtil.normaliseWhitespace("Hello\nthere   \n   now");
        Assertions.assertEquals("Hello there now", v);
    }
}

package org.jsoup.internal;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.jsoup.internal.StringUtil;
import org.mockito.MockedStatic;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import org.mockito.invocation.InvocationOnMock;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class StringUtilReproducedTests {

    @Test
    void resolveReproducedTest() throws MalformedURLException {
        MockedStatic<StringUtil> staticMockUtil = Mockito.mockStatic(StringUtil.class, Mockito.CALLS_REAL_METHODS);
        URL v = URI.create("https://jsoup.org/foo").toURL();
        staticMockUtil.when(() -> StringUtil.resolve(Mockito.<URL>any(), Mockito.<String>any())).thenReturn(v);
        String v1 = StringUtil.resolve("https://jsoup.org/", "/foo");
        Assertions.assertEquals("https://jsoup.org/foo", v1);
        staticMockUtil.close();
    }

}

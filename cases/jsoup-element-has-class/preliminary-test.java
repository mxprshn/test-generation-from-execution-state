package org.jsoup.nodes;

import org.junit.jupiter.api.Test;
import org.jsoup.parser.Tag;
import org.springframework.test.util.ReflectionTestUtils;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import java.util.ArrayList;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.jsoup.nodes.Document;
import java.lang.*;

class ElementReproducedTests {

    @Test
    void hasClassReproducedTest() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("p");
        ReflectionTestUtils.setField(v, "tagName", "p");
        ReflectionTestUtils.setField(v, "parserOptions", 224);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "p");
        ReflectionTestUtils.setField(v, "options", 13);
        Attributes v1 = new Attributes();
        ReflectionTestUtils.setField(v1, "size", 2);
        String[] v2 = new String[3];
        v2[0] = "id";
        v2[1] = "class";
        v2[2] = null;
        ReflectionTestUtils.setField(v1, "keys", v2);
        Object[] v3 = new Object[3];
        v3[0] = "0";
        v3[1] = "ONE two";
        v3[2] = null;
        ReflectionTestUtils.setField(v1, "vals", v3);
        Element v4 = new Element(v, null, v1);
        ReflectionTestUtils.setField(v4, "tag", v);
        ReflectionTestUtils.setField(v4, "childNodes", new ArrayList<Object>());
        ReflectionTestUtils.setField(v4, "attributes", v1);
        ReflectionTestUtils.setField(v4, "parentNode", Mockito.mock(Element.class));
        ReflectionTestUtils.setField(v4, "siblingIndex", 0);
        boolean v5 = v4.hasClass("One");
        Assertions.assertTrue(v5);
    }

    @Test
    void hasClassReproducedTest1() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("p");
        ReflectionTestUtils.setField(v, "tagName", "p");
        ReflectionTestUtils.setField(v, "parserOptions", 224);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "p");
        ReflectionTestUtils.setField(v, "options", 13);
        Attributes v1 = new Attributes();
        ReflectionTestUtils.setField(v1, "size", 2);
        String[] v2 = new String[3];
        v2[0] = "id";
        v2[1] = "class";
        v2[2] = null;
        ReflectionTestUtils.setField(v1, "keys", v2);
        Object[] v3 = new Object[3];
        v3[0] = "1";
        v3[1] = "one";
        v3[2] = null;
        ReflectionTestUtils.setField(v1, "vals", v3);
        Element v4 = new Element(v, null, v1);
        ReflectionTestUtils.setField(v4, "tag", v);
        ReflectionTestUtils.setField(v4, "childNodes", new ArrayList<Object>());
        ReflectionTestUtils.setField(v4, "attributes", v1);
        ReflectionTestUtils.setField(v4, "parentNode", Mockito.mock(Element.class));
        ReflectionTestUtils.setField(v4, "siblingIndex", 1);
        boolean v5 = v4.hasClass("One");
        Assertions.assertTrue(v5);
    }

    @Test
    void hasClassReproducedTest2() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("p");
        ReflectionTestUtils.setField(v, "tagName", "p");
        ReflectionTestUtils.setField(v, "parserOptions", 224);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "p");
        ReflectionTestUtils.setField(v, "options", 13);
        Attributes v1 = new Attributes();
        ReflectionTestUtils.setField(v1, "size", 2);
        String[] v2 = new String[3];
        v2[0] = "id";
        v2[1] = "class";
        v2[2] = null;
        ReflectionTestUtils.setField(v1, "keys", v2);
        Object[] v3 = new Object[3];
        v3[0] = "2";
        v3[1] = "two";
        v3[2] = null;
        ReflectionTestUtils.setField(v1, "vals", v3);
        Element v4 = new Element(v, null, v1);
        ReflectionTestUtils.setField(v4, "tag", v);
        ReflectionTestUtils.setField(v4, "childNodes", new ArrayList<Object>());
        ReflectionTestUtils.setField(v4, "attributes", v1);
        ReflectionTestUtils.setField(v4, "parentNode", Mockito.mock(Element.class));
        ReflectionTestUtils.setField(v4, "siblingIndex", 2);
        boolean v5 = v4.hasClass("One");
        Assertions.assertFalse(v5);
    }

    @Test
    void hasClassReproducedTest3() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("html");
        ReflectionTestUtils.setField(v, "tagName", "html");
        ReflectionTestUtils.setField(v, "parserOptions", 137);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "html");
        ReflectionTestUtils.setField(v, "options", 5);
        ArrayList<Object> v1 = new ArrayList<Object>();
        v1.add(Mockito.mock(Element.class));
        v1.add(Mockito.mock(Element.class));
        Element v2 = new Element(v, null, null);
        ReflectionTestUtils.setField(v2, "tag", v);
        ReflectionTestUtils.setField(v2, "childNodes", v1);
        ReflectionTestUtils.setField(v2, "attributes", null);
        ReflectionTestUtils.setField(v2, "parentNode", Mockito.mock(Document.class));
        ReflectionTestUtils.setField(v2, "siblingIndex", 0);
        boolean v3 = v2.hasClass("foo");
        Assertions.assertFalse(v3);
    }

    @Test
    void hasClassReproducedTest4() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("head");
        ReflectionTestUtils.setField(v, "tagName", "head");
        ReflectionTestUtils.setField(v, "parserOptions", 128);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "head");
        ReflectionTestUtils.setField(v, "options", 5);
        Element v1 = new Element(v, null, null);
        ReflectionTestUtils.setField(v1, "tag", v);
        ReflectionTestUtils.setField(v1, "childNodes", new ArrayList<Object>());
        ReflectionTestUtils.setField(v1, "attributes", null);
        ReflectionTestUtils.setField(v1, "parentNode", Mockito.mock(Element.class));
        ReflectionTestUtils.setField(v1, "siblingIndex", 0);
        boolean v2 = v1.hasClass("foo");
        Assertions.assertFalse(v2);
    }

    @Test
    void hasClassReproducedTest5() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("body");
        ReflectionTestUtils.setField(v, "tagName", "body");
        ReflectionTestUtils.setField(v, "parserOptions", 128);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "body");
        ReflectionTestUtils.setField(v, "options", 5);
        ArrayList<Object> v1 = new ArrayList<Object>();
        v1.add(Mockito.mock(Element.class));
        Element v2 = new Element(v, null, null);
        ReflectionTestUtils.setField(v2, "tag", v);
        ReflectionTestUtils.setField(v2, "childNodes", v1);
        ReflectionTestUtils.setField(v2, "attributes", null);
        ReflectionTestUtils.setField(v2, "parentNode", Mockito.mock(Element.class));
        ReflectionTestUtils.setField(v2, "siblingIndex", 1);
        boolean v3 = v2.hasClass("foo");
        Assertions.assertFalse(v3);
    }

    @Test
    void hasClassReproducedTest6() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("div");
        ReflectionTestUtils.setField(v, "tagName", "div");
        ReflectionTestUtils.setField(v, "parserOptions", 128);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "div");
        ReflectionTestUtils.setField(v, "options", 5);
        Attributes v1 = new Attributes();
        ReflectionTestUtils.setField(v1, "size", 1);
        String[] v2 = new String[3];
        v2[0] = "class";
        v2[1] = null;
        v2[2] = null;
        ReflectionTestUtils.setField(v1, "keys", v2);
        Object[] v3 = new Object[3];
        v3[0] = "one";
        v3[1] = null;
        v3[2] = null;
        ReflectionTestUtils.setField(v1, "vals", v3);
        Element v4 = new Element(v, null, v1);
        ReflectionTestUtils.setField(v4, "tag", v);
        ReflectionTestUtils.setField(v4, "childNodes", new ArrayList<Object>());
        ReflectionTestUtils.setField(v4, "attributes", v1);
        ReflectionTestUtils.setField(v4, "parentNode", Mockito.mock(Element.class));
        ReflectionTestUtils.setField(v4, "siblingIndex", 0);
        boolean v5 = v4.hasClass("foo");
        Assertions.assertFalse(v5);
    }

    @Test
    void hasClassReproducedTest7() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("html");
        ReflectionTestUtils.setField(v, "tagName", "html");
        ReflectionTestUtils.setField(v, "parserOptions", 137);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "html");
        ReflectionTestUtils.setField(v, "options", 5);
        ArrayList<Object> v1 = new ArrayList<Object>();
        v1.add(Mockito.mock(Element.class));
        v1.add(Mockito.mock(Element.class));
        Element v2 = new Element(v, null, null);
        ReflectionTestUtils.setField(v2, "tag", v);
        ReflectionTestUtils.setField(v2, "childNodes", v1);
        ReflectionTestUtils.setField(v2, "attributes", null);
        ReflectionTestUtils.setField(v2, "parentNode", Mockito.mock(Document.class));
        ReflectionTestUtils.setField(v2, "siblingIndex", 0);
        boolean v3 = v2.hasClass("one-two");
        Assertions.assertFalse(v3);
    }

    @Test
    void hasClassReproducedTest8() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("head");
        ReflectionTestUtils.setField(v, "tagName", "head");
        ReflectionTestUtils.setField(v, "parserOptions", 128);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "head");
        ReflectionTestUtils.setField(v, "options", 5);
        Element v1 = new Element(v, null, null);
        ReflectionTestUtils.setField(v1, "tag", v);
        ReflectionTestUtils.setField(v1, "childNodes", new ArrayList<Object>());
        ReflectionTestUtils.setField(v1, "attributes", null);
        ReflectionTestUtils.setField(v1, "parentNode", Mockito.mock(Element.class));
        ReflectionTestUtils.setField(v1, "siblingIndex", 0);
        boolean v2 = v1.hasClass("one-two");
        Assertions.assertFalse(v2);
    }

    @Test
    void hasClassReproducedTest9() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("body");
        ReflectionTestUtils.setField(v, "tagName", "body");
        ReflectionTestUtils.setField(v, "parserOptions", 128);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "body");
        ReflectionTestUtils.setField(v, "options", 5);
        ArrayList<Object> v1 = new ArrayList<Object>();
        v1.add(Mockito.mock(Element.class));
        Element v2 = new Element(v, null, null);
        ReflectionTestUtils.setField(v2, "tag", v);
        ReflectionTestUtils.setField(v2, "childNodes", v1);
        ReflectionTestUtils.setField(v2, "attributes", null);
        ReflectionTestUtils.setField(v2, "parentNode", Mockito.mock(Element.class));
        ReflectionTestUtils.setField(v2, "siblingIndex", 1);
        boolean v3 = v2.hasClass("one-two");
        Assertions.assertFalse(v3);
    }

    @Test
    void hasClassReproducedTest10() {
        Tag.RcData = 128;
        Tag.Data = 256;
        Tag.TextBoundary = 1024;
        Tag v = new Tag("div");
        ReflectionTestUtils.setField(v, "tagName", "div");
        ReflectionTestUtils.setField(v, "parserOptions", 128);
        Tag.Known = 1;
        Tag.Void = 2;
        Tag.Block = 4;
        Tag.InlineContainer = 8;
        Tag.SelfClose = 16;
        Tag.SeenSelfClose = 32;
        Tag.PreserveWhitespace = 64;
        Tag.FormSubmittable = 512;
        ReflectionTestUtils.setField(v, "namespace", "http://www.w3.org/1999/xhtml");
        ReflectionTestUtils.setField(v, "normalName", "div");
        ReflectionTestUtils.setField(v, "options", 5);
        Attributes v1 = new Attributes();
        ReflectionTestUtils.setField(v1, "size", 2);
        String[] v2 = new String[3];
        v2[0] = "class";
        v2[1] = "id";
        v2[2] = null;
        ReflectionTestUtils.setField(v1, "keys", v2);
        Object[] v3 = new Object[3];
        v3[0] = "One-Two";
        v3[1] = "1";
        v3[2] = null;
        ReflectionTestUtils.setField(v1, "vals", v3);
        Element v4 = new Element(v, null, v1);
        ReflectionTestUtils.setField(v4, "tag", v);
        ReflectionTestUtils.setField(v4, "childNodes", new ArrayList<Object>());
        ReflectionTestUtils.setField(v4, "attributes", v1);
        ReflectionTestUtils.setField(v4, "parentNode", Mockito.mock(Element.class));
        ReflectionTestUtils.setField(v4, "siblingIndex", 0);
        boolean v5 = v4.hasClass("one-two");
        Assertions.assertTrue(v5);
    }
}

package org.apache.ibatis.builder.annotation;

import org.junit.jupiter.api.Test;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.mockito.Mockito;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import java.lang.reflect.Method;
import org.mockito.invocation.InvocationOnMock;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.mapping.BoundSql;
import org.junit.jupiter.api.Assertions;
import java.util.Map;
import java.lang.*;
import static org.apache.ibatis.submitted.sqlprovider.SqlProviderTest.DefaultSqlProviderMapper.SqlProvider;

class ProviderSqlSourceReproducedTests {

    @Test
    void getBoundSqlReproducedTest() {
        Class<Object>[] v = new Class[1];
        v[0] = ProviderContext.class;
        Configuration v1 = Mockito.mock(Configuration.class);
        ProviderSqlSource v2 = new ProviderSqlSource(v1, null);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        ReflectionTestUtils.setField(v2, "providerType", SqlProvider.class);
        ReflectionTestUtils.setField(v2, "languageDriver", new XMLLanguageDriver());
        ReflectionTestUtils.setField(v2, "mapperMethod", Mockito.mock(Method.class));
        Method mocked = Mockito.mock(Method.class);
        Mockito.when(mocked.getModifiers()).thenReturn(9);
        ReflectionTestUtils.setField(v2, "providerMethod", mocked);
        ReflectionTestUtils.setField(v2, "paramNameResolver", Mockito.mock(ParamNameResolver.class));
        ReflectionTestUtils.setField(v2, "providerMethodParameterTypes", v);
        ReflectionTestUtils.setField(v2, "providerContext", Mockito.mock(ProviderContext.class));
        ReflectionTestUtils.setField(v2, "providerContextIndex", 0);
        BoundSql v3 = v2.getBoundSql(1);
        Assertions.assertNotNull(v3);
        Assertions.assertEquals("select name from foo where id = ?", v3.getSql());
        Assertions.assertEquals(1, v3.getParameterObject());
        Assertions.assertEquals(0, v3.getAdditionalParameters().size());
    }

    @Test
    void getBoundSqlReproducedTest1() {
        Class<Object>[] v = new Class[1];
        v[0] = ProviderContext.class;
        Configuration v1 = Mockito.mock(Configuration.class);
        ProviderSqlSource v2 = new ProviderSqlSource(v1, null);
        ReflectionTestUtils.setField(v2, "configuration", v1);
        ReflectionTestUtils.setField(v2, "providerType", SqlProvider.class);
        ReflectionTestUtils.setField(v2, "languageDriver", new XMLLanguageDriver());
        ReflectionTestUtils.setField(v2, "mapperMethod", Mockito.mock(Method.class));
        Method mocked = Mockito.mock(Method.class);
        Mockito.when(mocked.getModifiers()).thenReturn(9);
        ReflectionTestUtils.setField(v2, "providerMethod", mocked);
        ReflectionTestUtils.setField(v2, "paramNameResolver", Mockito.mock(ParamNameResolver.class));
        ReflectionTestUtils.setField(v2, "providerMethodParameterTypes", v);
        ReflectionTestUtils.setField(v2, "providerContext", Mockito.mock(ProviderContext.class));
        ReflectionTestUtils.setField(v2, "providerContextIndex", 0);
        BoundSql v3 = v2.getBoundSql("Taro");
        Assertions.assertNotNull(v3);
        Assertions.assertEquals("insert into foo (name) values(?)", v3.getSql());
        Assertions.assertEquals("Taro", v3.getParameterObject());
        Assertions.assertEquals(0, v3.getAdditionalParameters().size());
    }

    @Test
    void getBoundSqlReproducedTest2() {
        String[] v = new String[1];
        v[0] = "c";
        Class<Object>[] v1 = new Class[1];
        v1[0] = ProviderContext.class;
        Configuration v2 = Mockito.mock(Configuration.class);
        ProviderSqlSource v3 = new ProviderSqlSource(v2, null);
        ReflectionTestUtils.setField(v3, "configuration", v2);
        ReflectionTestUtils.setField(v3, "providerType", SqlProvider.class);
        ReflectionTestUtils.setField(v3, "languageDriver", new XMLLanguageDriver());
        ReflectionTestUtils.setField(v3, "mapperMethod", Mockito.mock(Method.class));
        Method mocked = Mockito.mock(Method.class);
        Mockito.when(mocked.getModifiers()).thenReturn(9);
        ReflectionTestUtils.setField(v3, "providerMethod", mocked);
        ParamNameResolver mocked1 = Mockito.mock(ParamNameResolver.class);
        Mockito.when(mocked1.getNames()).thenReturn(v);
        ReflectionTestUtils.setField(v3, "paramNameResolver", mocked1);
        ReflectionTestUtils.setField(v3, "providerMethodParameterTypes", v1);
        ReflectionTestUtils.setField(v3, "providerContext", Mockito.mock(ProviderContext.class));
        ReflectionTestUtils.setField(v3, "providerContextIndex", 0);
        BoundSql v4 = v3.getBoundSql(Map.of());
        Assertions.assertNotNull(v4);
        Assertions.assertEquals("update foo set name = ? where id = ?", v4.getSql());
        Assertions.assertEquals(0, v4.getAdditionalParameters().size());
    }

    @Test
    void getBoundSqlReproducedTest3() {
        String[] v = new String[1];
        v[0] = "c";
        Class<Object>[] v1 = new Class[1];
        v1[0] = ProviderContext.class;
        Configuration v2 = Mockito.mock(Configuration.class);
        ProviderSqlSource v3 = new ProviderSqlSource(v2, null);
        ReflectionTestUtils.setField(v3, "configuration", v2);
        ReflectionTestUtils.setField(v3, "providerType", SqlProvider.class);
        ReflectionTestUtils.setField(v3, "languageDriver", new XMLLanguageDriver());
        ReflectionTestUtils.setField(v3, "mapperMethod", Mockito.mock(Method.class));
        Method mocked = Mockito.mock(Method.class);
        Mockito.when(mocked.getModifiers()).thenReturn(9);
        ReflectionTestUtils.setField(v3, "providerMethod", mocked);
        ParamNameResolver mocked1 = Mockito.mock(ParamNameResolver.class);
        Mockito.when(mocked1.getNames()).thenReturn(v);
        ReflectionTestUtils.setField(v3, "paramNameResolver", mocked1);
        ReflectionTestUtils.setField(v3, "providerMethodParameterTypes", v1);
        ReflectionTestUtils.setField(v3, "providerContext", Mockito.mock(ProviderContext.class));
        ReflectionTestUtils.setField(v3, "providerContextIndex", 0);
        BoundSql v4 = v3.getBoundSql(Map.of());
        Assertions.assertNotNull(v4);
        Assertions.assertEquals("delete from foo where id = ?", v4.getSql());
        Assertions.assertEquals(0, v4.getAdditionalParameters().size());
    }
}

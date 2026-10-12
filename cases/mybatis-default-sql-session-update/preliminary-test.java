package org.apache.ibatis.session.defaults;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.mapping.MappedStatement;
import org.mockito.invocation.InvocationOnMock;
import org.apache.ibatis.executor.CachingExecutor;
import java.sql.SQLException;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.apache.ibatis.submitted.complex_type.Item;
import org.apache.ibatis.submitted.complex_type.Property;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class DefaultSqlSessionReproducedTests {

    @Test
    void updateReproducedTest() throws SQLException {
        Configuration mocked = Mockito.mock(Configuration.class);
        Mockito.when(mocked.getMappedStatement(Mockito.<String>any())).thenReturn(Mockito.mock(MappedStatement.class));
        CachingExecutor mocked1 = Mockito.mock(CachingExecutor.class);
        Mockito.when(mocked1.update(Mockito.<MappedStatement>any(), Mockito.<Object>any())).thenReturn(2);
        DefaultSqlSession v = new DefaultSqlSession(mocked, mocked1, false);
        ReflectionTestUtils.setField(v, "configuration", mocked);
        ReflectionTestUtils.setField(v, "executor", mocked1);
        ReflectionTestUtils.setField(v, "autoCommit", false);
        ReflectionTestUtils.setField(v, "dirty", false);
        ReflectionTestUtils.setField(v, "cursorList", null);
        Item v1 = new Item();
        v1.id = 10;
        Property v2 = new Property();
        v2.id = 11;
        v2.value = "value11";
        Property v3 = new Property();
        v3.id = 12;
        v3.value = "value12";
        v1.properties = new ArrayList<Object>((Collection<Object>) (Object) List.of(v2, v3));
        int v4 = v.update("updateProps", v1);
        Assertions.assertEquals(2, v4);
    }
}

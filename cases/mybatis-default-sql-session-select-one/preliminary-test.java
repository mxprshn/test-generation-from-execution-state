package org.apache.ibatis.session.defaults;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.mapping.MappedStatement;
import org.mockito.invocation.InvocationOnMock;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.ResultHandler;
import java.sql.SQLException;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class DefaultSqlSessionReproducedTests {

    @Test
    void selectOneReproducedTest() throws SQLException {
        Configuration mocked = Mockito.mock(Configuration.class);
        MappedStatement mocked1 = Mockito.mock(MappedStatement.class);
        Mockito.when(mocked1.isDirtySelect()).thenReturn(false);
        Mockito.when(mocked.getMappedStatement(Mockito.<String>any())).thenReturn(mocked1);
        CachingExecutor mocked2 = Mockito.mock(CachingExecutor.class);
        Mockito.when(mocked2.query(Mockito.<MappedStatement>any(), Mockito.<Object>any(), Mockito.<RowBounds>any(), (ResultHandler<Object>) (Object) Mockito.<ResultHandler<Object>>any())).thenReturn((List<Object>) (Object) new ArrayList<Object>((Collection<Object>) (Object) List.of(1)));
        DefaultSqlSession v = new DefaultSqlSession(mocked, mocked2, false);
        ReflectionTestUtils.setField(v, "configuration", mocked);
        ReflectionTestUtils.setField(v, "executor", mocked2);
        ReflectionTestUtils.setField(v, "autoCommit", false);
        ReflectionTestUtils.setField(v, "dirty", false);
        ReflectionTestUtils.setField(v, "cursorList", null);
        Object v1 = v.selectOne("count", Map.of("column", "name", "value", "User1", "table", "users"));
        Assertions.assertEquals(1, v1);
    }
}

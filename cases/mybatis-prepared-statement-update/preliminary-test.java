package org.apache.ibatis.executor.statement;

import org.junit.jupiter.api.Test;
import org.apache.ibatis.session.RowBounds;
import org.springframework.test.util.ReflectionTestUtils;
import org.apache.ibatis.submitted.complex_property.User;
import org.apache.ibatis.submitted.complex_property.EncryptedString;
import org.mockito.Mockito;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.mockito.invocation.InvocationOnMock;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.hsqldb.jdbc.JDBCPreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class PreparedStatementHandlerReproducedTests {

    @Test
    void updateReproducedTest() throws SQLException {
        RowBounds v = new RowBounds();
        ReflectionTestUtils.setField(v, "limit", 2147483647);
        User v1 = new User();
        v1.setId(500000L);
        v1.setUsername("johnny1791729018608");
        EncryptedString v2 = new EncryptedString();
        v2.setEncrypted("secret");
        v1.setPassword(v2);
        v1.setAdministrator(true);
        SimpleExecutor v3 = Mockito.mock(SimpleExecutor.class);
        MappedStatement mocked = Mockito.mock(MappedStatement.class);
        Mockito.when(mocked.getKeyGenerator()).thenReturn(new NoKeyGenerator());
        BoundSql mocked1 = Mockito.mock(BoundSql.class);
        Mockito.when(mocked1.getParameterObject()).thenReturn(v1);
        PreparedStatementHandler v4 = new PreparedStatementHandler(v3, mocked, new Object(), v, (ResultHandler<Object>) (Object) null, mocked1);
        ReflectionTestUtils.setField(v4, "configuration", Mockito.mock(Configuration.class));
        ReflectionTestUtils.setField(v4, "objectFactory", Mockito.mock(DefaultObjectFactory.class));
        ReflectionTestUtils.setField(v4, "typeHandlerRegistry", Mockito.mock(TypeHandlerRegistry.class));
        ReflectionTestUtils.setField(v4, "resultSetHandler", Mockito.mock(DefaultResultSetHandler.class));
        ReflectionTestUtils.setField(v4, "parameterHandler", Mockito.mock(DefaultParameterHandler.class));
        ReflectionTestUtils.setField(v4, "executor", v3);
        ReflectionTestUtils.setField(v4, "mappedStatement", mocked);
        ReflectionTestUtils.setField(v4, "rowBounds", v);
        ReflectionTestUtils.setField(v4, "boundSql", mocked1);
        JDBCPreparedStatement mocked2 = Mockito.mock(JDBCPreparedStatement.class);
        Mockito.when(mocked2.execute()).thenReturn(false);
        Mockito.when(mocked2.getUpdateCount()).thenReturn(1);
        int v5 = v4.update(mocked2);
        Assertions.assertEquals(1, v5);
    }
}

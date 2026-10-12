package org.apache.ibatis.executor;

import org.junit.jupiter.api.Test;
import org.apache.ibatis.submitted.cache.Person;
import org.mockito.Mockito;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import java.sql.SQLException;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import org.mockito.invocation.InvocationOnMock;
import org.apache.ibatis.executor.CachingExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.SynchronizedCache;
import org.apache.ibatis.mapping.StatementType;
import org.junit.jupiter.api.Assertions;
import org.apache.ibatis.cache.decorators.TransactionalCache;
import java.util.Map;
import java.lang.*;

class CachingExecutorReproducedTests {

    @Test
    void queryReproducedTest() throws SQLException {
        Person v = new Person();
        v.setId(1);
        v.setFirstname("Jane");
        v.setLastname("Doe");
        Person v1 = new Person();
        v1.setId(2);
        v1.setFirstname("John");
        v1.setLastname("Smith");
        SimpleExecutor mocked = Mockito.mock(SimpleExecutor.class);
        Mockito.when(mocked.query(Mockito.<MappedStatement>any(), Mockito.<Object>any(), Mockito.<RowBounds>any(), (ResultHandler<Object>) (Object) Mockito.<ResultHandler<Object>>any(), Mockito.<CacheKey>any(), Mockito.<BoundSql>any())).thenReturn((List<Object>) (Object) new ArrayList<Object>((Collection<Object>) (Object) List.of(v, v1)));
        CachingExecutor v2 = new CachingExecutor(mocked);
        ReflectionTestUtils.setField(v2, "delegate", mocked);
        ReflectionTestUtils.setField(v2, "tcm", new TransactionalCacheManager());
        RowBounds v3 = new RowBounds();
        ReflectionTestUtils.setField(v3, "limit", 2147483647);
        CacheKey v4 = new CacheKey();
        ReflectionTestUtils.setField(v4, "multiplier", 37);
        ReflectionTestUtils.setField(v4, "hashcode", 2077283010);
        ReflectionTestUtils.setField(v4, "checksum", -277894317L);
        ReflectionTestUtils.setField(v4, "count", 5);
        ReflectionTestUtils.setField(v4, "updateList", new ArrayList<Object>((Collection<Object>) (Object) List.of("org.apache.ibatis.submitted.cache.PersonMapper.findAll", 0, 2147483647, "select id, firstname, lastname from person", "development")));
        BoundSql v5 = new BoundSql(null, "select id, firstname, lastname from person", (List<ParameterMapping>) (Object) List.of(), null);
        ReflectionTestUtils.setField(v5, "sql", "select id, firstname, lastname from person");
        ReflectionTestUtils.setField(v5, "metaParameters", Mockito.mock(MetaObject.class));
        MappedStatement mocked1 = Mockito.mock(MappedStatement.class);
        Mockito.when(mocked1.getCache()).thenReturn(Mockito.mock(SynchronizedCache.class));
        Mockito.when(mocked1.isFlushCacheRequired()).thenReturn(false);
        Mockito.when(mocked1.isUseCache()).thenReturn(true);
        Mockito.when(mocked1.getStatementType()).thenReturn(StatementType.PREPARED);
        List<Object> v6 = v2.query(mocked1, null, v3, (ResultHandler<Object>) (Object) null, v4, v5);
        Assertions.assertEquals(2, v6.size());
    }

    @Test
    void queryReproducedTest1() throws SQLException {
        Person v = new Person();
        v.setId(2);
        v.setFirstname("John");
        v.setLastname("Smith");
        TransactionalCacheManager v1 = new TransactionalCacheManager();
        SynchronizedCache v2 = Mockito.mock(SynchronizedCache.class);
        ReflectionTestUtils.setField(v1, "transactionalCaches", Map.of(v2, Mockito.mock(TransactionalCache.class)));
        SimpleExecutor mocked = Mockito.mock(SimpleExecutor.class);
        Mockito.when(mocked.query(Mockito.<MappedStatement>any(), Mockito.<Object>any(), Mockito.<RowBounds>any(), (ResultHandler<Object>) (Object) Mockito.<ResultHandler<Object>>any(), Mockito.<CacheKey>any(), Mockito.<BoundSql>any())).thenReturn((List<Object>) (Object) new ArrayList<Object>((Collection<Object>) (Object) List.of(v)));
        CachingExecutor v3 = new CachingExecutor(mocked);
        ReflectionTestUtils.setField(v3, "delegate", mocked);
        ReflectionTestUtils.setField(v3, "tcm", v1);
        RowBounds v4 = new RowBounds();
        ReflectionTestUtils.setField(v4, "limit", 2147483647);
        CacheKey v5 = new CacheKey();
        ReflectionTestUtils.setField(v5, "multiplier", 37);
        ReflectionTestUtils.setField(v5, "hashcode", 2077283010);
        ReflectionTestUtils.setField(v5, "checksum", -277894317L);
        ReflectionTestUtils.setField(v5, "count", 5);
        ReflectionTestUtils.setField(v5, "updateList", new ArrayList<Object>((Collection<Object>) (Object) List.of("org.apache.ibatis.submitted.cache.PersonMapper.findAll", 0, 2147483647, "select id, firstname, lastname from person", "development")));
        BoundSql v6 = new BoundSql(null, "select id, firstname, lastname from person", (List<ParameterMapping>) (Object) List.of(), null);
        ReflectionTestUtils.setField(v6, "sql", "select id, firstname, lastname from person");
        ReflectionTestUtils.setField(v6, "metaParameters", Mockito.mock(MetaObject.class));
        MappedStatement mocked1 = Mockito.mock(MappedStatement.class);
        Mockito.when(mocked1.getCache()).thenReturn(v2);
        Mockito.when(mocked1.isFlushCacheRequired()).thenReturn(false);
        Mockito.when(mocked1.isUseCache()).thenReturn(true);
        Mockito.when(mocked1.getStatementType()).thenReturn(StatementType.PREPARED);
        List<Object> v7 = v3.query(mocked1, null, v4, (ResultHandler<Object>) (Object) null, v5, v6);
        Assertions.assertEquals(1, v7.size());
    }
}

package org.apache.ibatis.executor.loader;

import org.junit.jupiter.api.Test;
import org.apache.ibatis.submitted.lazy_properties.User;
import org.apache.ibatis.cache.CacheKey;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import org.mockito.Mockito;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.mapping.BoundSql;
import java.sql.SQLException;
import org.mockito.invocation.InvocationOnMock;
import org.apache.ibatis.executor.loader.ResultLoader;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.executor.ResultExtractor;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class ResultLoaderReproducedTests {

    @Test
    void loadResultReproducedTest() throws SQLException {
        User v = new User();
        v.setId(11);
        v.setName("lazy1");
        CacheKey v1 = new CacheKey();
        ReflectionTestUtils.setField(v1, "multiplier", 37);
        ReflectionTestUtils.setField(v1, "hashcode", 843017580);
        ReflectionTestUtils.setField(v1, "checksum", -788039104L);
        ReflectionTestUtils.setField(v1, "count", 5);
        ReflectionTestUtils.setField(v1, "updateList", new ArrayList<Object>((Collection<Object>) (Object) List.of("org.apache.ibatis.submitted.lazy_properties.Mapper.getLazy1", 0, 2147483647, "select 11 id, 'lazy1' name from (values(0))", "development")));
        CachingExecutor mocked = Mockito.mock(CachingExecutor.class);
        Mockito.when(mocked.isClosed()).thenReturn(false);
        Mockito.when(mocked.query(Mockito.<MappedStatement>any(), Mockito.<Object>any(), Mockito.<RowBounds>any(), (ResultHandler<Object>) (Object) Mockito.<ResultHandler<Object>>any(), Mockito.<CacheKey>any(), Mockito.<BoundSql>any())).thenReturn((List<Object>) (Object) new ArrayList<Object>((Collection<Object>) (Object) List.of(v)));
        MappedStatement v2 = Mockito.mock(MappedStatement.class);
        User v3 = User.class;
        BoundSql v4 = Mockito.mock(BoundSql.class);
        ResultLoader v5 = new ResultLoader(Mockito.mock(Configuration.class), mocked, v2, 1, (Class<Object>) (Object) v3, v1, v4);
        ReflectionTestUtils.setField(v5, "configuration", Mockito.mock(Configuration.class));
        ReflectionTestUtils.setField(v5, "executor", mocked);
        ReflectionTestUtils.setField(v5, "mappedStatement", v2);
        ReflectionTestUtils.setField(v5, "parameterObject", 1);
        ReflectionTestUtils.setField(v5, "targetType", v3);
        ReflectionTestUtils.setField(v5, "objectFactory", Mockito.mock(DefaultObjectFactory.class));
        ReflectionTestUtils.setField(v5, "cacheKey", v1);
        ReflectionTestUtils.setField(v5, "boundSql", v4);
        ResultExtractor mocked1 = Mockito.mock(ResultExtractor.class);
        Mockito.when(mocked1.extractObjectFromList((List<Object>) (Object) Mockito.<List<Object>>any(), (Class<Object>) (Object) Mockito.<Class<Object>>any())).thenReturn(v);
        ReflectionTestUtils.setField(v5, "resultExtractor", mocked1);
        ReflectionTestUtils.setField(v5, "creatorThreadId", 1L);
        ReflectionTestUtils.setField(v5, "loaded", false);
        ReflectionTestUtils.setField(v5, "resultObject", null);
        Object v6 = v5.loadResult();
        Assertions.assertNotNull(v6);
        Assertions.assertEquals("lazy1", v6.getName());
        Assertions.assertEquals(0, v6.setterCounter);
    }
}

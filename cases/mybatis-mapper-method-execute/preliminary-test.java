package org.apache.ibatis.binding;

import org.junit.jupiter.api.Test;
import org.apache.ibatis.submitted.association_nested.FolderMapper;
import org.mockito.Mockito;
import java.util.Map;
import java.util.List;
import org.mockito.invocation.InvocationOnMock;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.test.util.ReflectionTestUtils;
import org.apache.ibatis.submitted.association_nested.Folder;
import org.apache.ibatis.submitted.association_nested.FolderFlatTree;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import java.util.Collection;
import java.util.ArrayList;
import java.lang.*;
import static org.apache.ibatis.binding.MapperMethod.MethodSignature;
import static org.apache.ibatis.binding.MapperMethod.SqlCommand;

class MapperMethodReproducedTests {

    @Test
    void executeReproducedTest() {
        MethodSignature mocked = Mockito.mock(MethodSignature.class);
        Mockito.when(mocked.returnsVoid()).thenReturn(false);
        Mockito.when(mocked.returnsMany()).thenReturn(true);
        Mockito.when(mocked.convertArgsToSqlCommandParam(Mockito.<Object[]>any())).thenReturn(Map.of("name", "Root", "param1", "Root"));
        Mockito.when(mocked.hasRowBounds()).thenReturn(false);
        Mockito.when(mocked.getReturnType()).thenReturn((Class<Object>) (Object) List.class);
        MapperMethod v = new MapperMethod((Class<Object>) (Object) FolderMapper.class, mocked, Mockito.mock(Configuration.class));
        SqlCommand mocked1 = Mockito.mock(SqlCommand.class);
        Mockito.when(mocked1.getType()).thenReturn(SqlCommandType.SELECT);
        Mockito.when(mocked1.getName()).thenReturn("org.apache.ibatis.submitted.association_nested.FolderMapper.findWithSubFolders");
        ReflectionTestUtils.setField(v, "command", mocked1);
        ReflectionTestUtils.setField(v, "method", mocked);
        Folder v1 = new Folder();
        v1.id = 1L;
        v1.name = "Root";
        FolderFlatTree v2 = new FolderFlatTree();
        v2.root = v1;
        Folder v3 = new Folder();
        v3.id = 2L;
        v3.name = "Folder 1";
        v2.level1 = v3;
        Folder v4 = new Folder();
        v4.id = 1L;
        v4.name = "Root";
        FolderFlatTree v5 = new FolderFlatTree();
        v5.root = v4;
        Folder v6 = new Folder();
        v6.id = 3L;
        v6.name = "Folder 2";
        v5.level1 = v6;
        Folder v7 = new Folder();
        v7.id = 4L;
        v7.name = "Folder 2_1";
        v5.level2 = v7;
        Folder v8 = new Folder();
        v8.id = 1L;
        v8.name = "Root";
        FolderFlatTree v9 = new FolderFlatTree();
        v9.root = v8;
        Folder v10 = new Folder();
        v10.id = 3L;
        v10.name = "Folder 2";
        v9.level1 = v10;
        Folder v11 = new Folder();
        v11.id = 5L;
        v11.name = "Folder 2_2";
        v9.level2 = v11;
        String[] v12 = new String[1];
        v12[0] = "Root";
        DefaultSqlSession mocked2 = Mockito.mock(DefaultSqlSession.class);
        Mockito.when(mocked2.selectList(Mockito.<String>any(), Mockito.<Object>any())).thenReturn((List<Object>) (Object) new ArrayList<Object>((Collection<Object>) (Object) List.of(v2, v5, v9)));
        v.execute(mocked2, v12);
    }
}

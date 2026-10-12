package com.ctrip.framework.apollo.biz.service;

import org.junit.jupiter.api.Test;
import com.ctrip.framework.apollo.biz.entity.Commit;
import java.util.Date;
import org.mockito.Mockito;
import com.ctrip.framework.apollo.biz.repository.CommitRepository;
import org.mockito.invocation.InvocationOnMock;
import com.ctrip.framework.apollo.biz.service.CommitService;
import org.springframework.test.util.ReflectionTestUtils;
import java.lang.*;

class CommitServiceReproducedTests {

    @Test
    void createCommitReproducedTest() {
        Commit v = new Commit();
        v.setChangeSets("{\"createItems\":[{\"namespaceId\":1,\"key\":\"k6\",\"type\":0,\"value\":\"v6\",\"comment\":\"test item num limit\",\"lineNum\":6,\"id\":9906,\"isDeleted\":false,\"deletedAt\":0,\"dataChangeCreatedTime\":\"2026-10-11 16:59:14\",\"dataChangeLastModifiedTime\":\"2026-10-11 16:59:14\"},{\"namespaceId\":1,\"key\":\"k7\",\"type\":0,\"value\":\"v7\",\"comment\":\"test item num limit\",\"lineNum\":7,\"id\":9907,\"isDeleted\":false,\"deletedAt\":0,\"dataChangeCreatedTime\":\"2026-10-11 16:59:14\",\"dataChangeLastModifiedTime\":\"2026-10-11 16:59:14\"}],\"updateItems\":[],\"deleteItems\":[]}");
        v.setClusterName("default");
        v.setId(1L);
        v.setAppId("testApp");
        v.setNamespaceName("application");
        v.setDataChangeCreatedTime(new Date(1791727155222L));
        v.setDataChangeLastModifiedTime(new Date(1791727155222L));
        CommitRepository mocked = Mockito.mock(CommitRepository.class);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        CommitService v1 = new CommitService(mocked);
        ReflectionTestUtils.setField(v1, "commitRepository", mocked);
        v1.createCommit("testApp", "default", "application", "{\"createItems\":[{\"namespaceId\":1,\"key\":\"k6\",\"type\":0,\"value\":\"v6\",\"comment\":\"test item num limit\",\"lineNum\":6,\"id\":9906,\"isDeleted\":false,\"deletedAt\":0,\"dataChangeCreatedTime\":\"2026-10-11 16:59:14\",\"dataChangeLastModifiedTime\":\"2026-10-11 16:59:14\"},{\"namespaceId\":1,\"key\":\"k7\",\"type\":0,\"value\":\"v7\",\"comment\":\"test item num limit\",\"lineNum\":7,\"id\":9907,\"isDeleted\":false,\"deletedAt\":0,\"dataChangeCreatedTime\":\"2026-10-11 16:59:14\",\"dataChangeLastModifiedTime\":\"2026-10-11 16:59:14\"}],\"updateItems\":[],\"deleteItems\":[]}", null);
    }

    @Test
    void createCommitReproducedTest1() {
        Commit v = new Commit();
        v.setChangeSets("{\"createItems\":[{\"namespaceId\":1,\"key\":\"k6\",\"type\":0,\"value\":\"v6\",\"comment\":\"test item num limit\",\"lineNum\":6,\"id\":9908,\"isDeleted\":false,\"deletedAt\":0,\"dataChangeCreatedTime\":\"2026-10-11 16:59:15\",\"dataChangeLastModifiedTime\":\"2026-10-11 16:59:15\"}],\"updateItems\":[{\"oldItem\":{\"namespaceId\":1,\"key\":\"k1\",\"type\":0,\"value\":\"v1\",\"comment\":\"\",\"lineNum\":1,\"id\":9901,\"isDeleted\":false,\"deletedAt\":0},\"newItem\":{\"namespaceId\":1,\"key\":\"k1\",\"type\":0,\"value\":\"v1 update\",\"comment\":\"\",\"lineNum\":1,\"id\":9901,\"isDeleted\":false,\"deletedAt\":0,\"dataChangeLastModifiedTime\":\"2026-10-11 16:59:15\"}}],\"deleteItems\":[{\"namespaceId\":1,\"key\":\"k2\",\"type\":2,\"value\":\"v2\",\"comment\":\"\",\"lineNum\":2,\"id\":9902,\"isDeleted\":true,\"deletedAt\":1791727155487,\"dataChangeLastModifiedTime\":\"2026-10-11 16:59:15\"}]}");
        v.setClusterName("default");
        v.setId(2L);
        v.setAppId("testApp");
        v.setNamespaceName("application");
        v.setDataChangeCreatedTime(new Date(1791727155746L));
        v.setDataChangeLastModifiedTime(new Date(1791727155746L));
        CommitRepository mocked = Mockito.mock(CommitRepository.class);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        CommitService v1 = new CommitService(mocked);
        ReflectionTestUtils.setField(v1, "commitRepository", mocked);
        v1.createCommit("testApp", "default", "application", "{\"createItems\":[{\"namespaceId\":1,\"key\":\"k6\",\"type\":0,\"value\":\"v6\",\"comment\":\"test item num limit\",\"lineNum\":6,\"id\":9908,\"isDeleted\":false,\"deletedAt\":0,\"dataChangeCreatedTime\":\"2026-10-11 16:59:15\",\"dataChangeLastModifiedTime\":\"2026-10-11 16:59:15\"}],\"updateItems\":[{\"oldItem\":{\"namespaceId\":1,\"key\":\"k1\",\"type\":0,\"value\":\"v1\",\"comment\":\"\",\"lineNum\":1,\"id\":9901,\"isDeleted\":false,\"deletedAt\":0},\"newItem\":{\"namespaceId\":1,\"key\":\"k1\",\"type\":0,\"value\":\"v1 update\",\"comment\":\"\",\"lineNum\":1,\"id\":9901,\"isDeleted\":false,\"deletedAt\":0,\"dataChangeLastModifiedTime\":\"2026-10-11 16:59:15\"}}],\"deleteItems\":[{\"namespaceId\":1,\"key\":\"k2\",\"type\":2,\"value\":\"v2\",\"comment\":\"\",\"lineNum\":2,\"id\":9902,\"isDeleted\":true,\"deletedAt\":1791727155487,\"dataChangeLastModifiedTime\":\"2026-10-11 16:59:15\"}]}", null);
    }
}

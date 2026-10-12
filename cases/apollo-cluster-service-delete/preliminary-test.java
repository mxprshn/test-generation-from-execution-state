package com.ctrip.framework.apollo.biz.service;

import org.junit.jupiter.api.Test;
import com.ctrip.framework.apollo.biz.entity.Cluster;
import java.util.Date;
import org.mockito.Mockito;
import com.ctrip.framework.apollo.biz.repository.ClusterRepository;
import java.util.Optional;
import org.mockito.invocation.InvocationOnMock;
import com.ctrip.framework.apollo.biz.service.AuditService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.ClusterService;
import org.springframework.test.util.ReflectionTestUtils;
import java.lang.*;

class ClusterServiceReproducedTests {

    @Test
    void deleteReproducedTest() {
        Cluster v = new Cluster();
        v.setName("default");
        v.setId(1L);
        v.setDataChangeCreatedBy("someOwnerName");
        v.setDataChangeLastModifiedBy("someOwnerName");
        v.setAppId("someAppId");
        v.setDataChangeCreatedTime(new Date(1791751388569L));
        v.setDataChangeLastModifiedTime(new Date(1791751388569L));
        ClusterRepository mocked = Mockito.mock(ClusterRepository.class);
        Mockito.when(mocked.findById(Mockito.<Object>any())).thenReturn((Optional<Object>) (Object) Optional.of(v));
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        AuditService v1 = Mockito.mock(AuditService.class);
        NamespaceService v2 = Mockito.mock(NamespaceService.class);
        ClusterService v3 = new ClusterService(mocked, v1, v2);
        ReflectionTestUtils.setField(v3, "clusterRepository", mocked);
        ReflectionTestUtils.setField(v3, "auditService", v1);
        ReflectionTestUtils.setField(v3, "namespaceService", v2);
        v3.delete(1L, "someOwnerName");
    }
}

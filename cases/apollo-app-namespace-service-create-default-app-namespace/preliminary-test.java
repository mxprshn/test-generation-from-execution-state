package com.ctrip.framework.apollo.biz.service;

import org.junit.jupiter.api.Test;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import java.util.Date;
import org.mockito.Mockito;
import com.ctrip.framework.apollo.biz.repository.AppNamespaceRepository;
import org.mockito.invocation.InvocationOnMock;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.ClusterService;
import com.ctrip.framework.apollo.biz.service.AuditService;
import com.ctrip.framework.apollo.biz.service.AppNamespaceService;
import org.springframework.test.util.ReflectionTestUtils;
import java.lang.*;

class AppNamespaceServiceReproducedTests {

    @Test
    void createDefaultAppNamespaceReproducedTest() {
        AppNamespace v = new AppNamespace();
        v.setName("application");
        v.setFormat("properties");
        v.setId(8L);
        v.setDataChangeCreatedBy("someOwnerName");
        v.setDataChangeLastModifiedBy("someOwnerName");
        v.setAppId("someAppId");
        v.setComment("default app namespace");
        v.setDataChangeCreatedTime(new Date(1791751375052L));
        v.setDataChangeLastModifiedTime(new Date(1791751375052L));
        AppNamespaceRepository mocked = Mockito.mock(AppNamespaceRepository.class);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        NamespaceService v1 = Mockito.mock(NamespaceService.class);
        ClusterService v2 = Mockito.mock(ClusterService.class);
        AuditService v3 = Mockito.mock(AuditService.class);
        AppNamespaceService v4 = new AppNamespaceService(mocked, v1, v2, v3);
        ReflectionTestUtils.setField(v4, "appNamespaceRepository", mocked);
        ReflectionTestUtils.setField(v4, "namespaceService", v1);
        ReflectionTestUtils.setField(v4, "clusterService", v2);
        ReflectionTestUtils.setField(v4, "auditService", v3);
        v4.createDefaultAppNamespace("someAppId", "someOwnerName");
    }
}

package com.ctrip.framework.apollo.biz.service;

import org.junit.jupiter.api.Test;
import com.ctrip.framework.apollo.biz.entity.GrayReleaseRule;
import java.util.Date;
import com.ctrip.framework.apollo.biz.entity.Cluster;
import com.ctrip.framework.apollo.biz.entity.ReleaseHistory;
import org.mockito.Mockito;
import com.ctrip.framework.apollo.biz.service.AuditService;
import com.ctrip.framework.apollo.biz.repository.GrayReleaseRuleRepository;
import org.mockito.invocation.InvocationOnMock;
import com.ctrip.framework.apollo.biz.service.ClusterService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.ReleaseHistoryService;
import java.util.Map;
import com.ctrip.framework.apollo.biz.service.NamespaceBranchService;
import org.springframework.test.util.ReflectionTestUtils;
import java.lang.*;

class NamespaceBranchServiceReproducedTests {

    @Test
    void deleteBranchReproducedTest() {
        GrayReleaseRule v = new GrayReleaseRule();
        v.setClusterName("default");
        v.setBranchName("child-cluster");
        v.setId(1L);
        v.setDataChangeCreatedBy("apollo");
        v.setDataChangeLastModifiedBy("apollo");
        v.setAppId("testApp");
        v.setNamespaceName("application");
        v.setRules("[]");
        v.setDataChangeCreatedTime(new Date(1791751403782L));
        v.setDataChangeLastModifiedTime(new Date(1791751403782L));
        Cluster v1 = new Cluster();
        v1.setName("child-cluster");
        v1.setParentClusterId(1L);
        v1.setId(2L);
        v1.setDataChangeCreatedBy("default");
        v1.setDataChangeLastModifiedBy("default");
        v1.setAppId("testApp");
        ReleaseHistory v2 = new ReleaseHistory();
        v2.setBranchName("child-cluster");
        v2.setOperation(7);
        v2.setId(2L);
        v2.setDataChangeCreatedBy("apollo");
        v2.setAppId("testApp");
        v2.setOperationContext("{}");
        v2.setDataChangeCreatedTime(new Date(1791751404099L));
        v2.setClusterName("default");
        v2.setDataChangeLastModifiedBy("apollo");
        v2.setNamespaceName("application");
        v2.setDataChangeLastModifiedTime(new Date(1791751404100L));
        AuditService v3 = Mockito.mock(AuditService.class);
        GrayReleaseRuleRepository mocked = Mockito.mock(GrayReleaseRuleRepository.class);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        ClusterService mocked1 = Mockito.mock(ClusterService.class);
        Mockito.when(mocked1.findOne(Mockito.<String>any(), Mockito.<String>any())).thenReturn(v1);
        ReleaseService mocked2 = Mockito.mock(ReleaseService.class);
        Mockito.when(mocked2.findLatestActiveRelease(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any())).thenReturn(null).thenReturn(null);
        NamespaceService v4 = Mockito.mock(NamespaceService.class);
        ReleaseHistoryService mocked3 = Mockito.mock(ReleaseHistoryService.class);
        Mockito.when(mocked3.createReleaseHistory(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), (Map<String, Object>) (Object) Mockito.<Map<String, Object>>any(), Mockito.<String>any())).thenReturn(v2);
        NamespaceBranchService v5 = new NamespaceBranchService(v3, mocked, mocked1, mocked2, v4, mocked3);
        ReflectionTestUtils.setField(v5, "auditService", v3);
        ReflectionTestUtils.setField(v5, "grayReleaseRuleRepository", mocked);
        ReflectionTestUtils.setField(v5, "clusterService", mocked1);
        ReflectionTestUtils.setField(v5, "releaseService", mocked2);
        ReflectionTestUtils.setField(v5, "namespaceService", v4);
        ReflectionTestUtils.setField(v5, "releaseHistoryService", mocked3);
        v5.deleteBranch("testApp", "default", "application", "child-cluster", 0, "apollo");
    }
}

package com.ctrip.framework.apollo.biz.service;

import org.junit.jupiter.api.Test;
import com.ctrip.framework.apollo.biz.entity.GrayReleaseRule;
import java.util.Date;
import org.mockito.Mockito;
import com.ctrip.framework.apollo.biz.service.AuditService;
import com.ctrip.framework.apollo.biz.repository.GrayReleaseRuleRepository;
import org.mockito.invocation.InvocationOnMock;
import com.ctrip.framework.apollo.biz.service.ClusterService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.ReleaseHistoryService;
import com.ctrip.framework.apollo.biz.service.NamespaceBranchService;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class NamespaceBranchServiceReproducedTests {

    @Test
    void updateRulesReleaseIdReproducedTest() {
        GrayReleaseRule v = new GrayReleaseRule();
        v.setClusterName("default5");
        v.setBranchName("child-cluster5");
        v.setReleaseId(1155L);
        v.setBranchStatus(1);
        v.setId(5L);
        v.setAppId("test");
        v.setNamespaceName("application");
        v.setRules("[]");
        GrayReleaseRule v1 = new GrayReleaseRule();
        v1.setClusterName("default5");
        v1.setBranchName("child-cluster5");
        v1.setReleaseId(9L);
        v1.setBranchStatus(1);
        v1.setId(6L);
        v1.setDataChangeCreatedBy("apollo");
        v1.setDataChangeLastModifiedBy("apollo");
        v1.setAppId("test");
        v1.setNamespaceName("application");
        v1.setRules("[]");
        v1.setDataChangeCreatedTime(new Date(1791751413783L));
        v1.setDataChangeLastModifiedTime(new Date(1791751413783L));
        AuditService v2 = Mockito.mock(AuditService.class);
        GrayReleaseRuleRepository mocked = Mockito.mock(GrayReleaseRuleRepository.class);
        Mockito.when(mocked.findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any())).thenReturn(v);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v1);
        ClusterService v3 = Mockito.mock(ClusterService.class);
        ReleaseService v4 = Mockito.mock(ReleaseService.class);
        NamespaceService v5 = Mockito.mock(NamespaceService.class);
        ReleaseHistoryService v6 = Mockito.mock(ReleaseHistoryService.class);
        NamespaceBranchService v7 = new NamespaceBranchService(v2, mocked, v3, v4, v5, v6);
        ReflectionTestUtils.setField(v7, "auditService", v2);
        ReflectionTestUtils.setField(v7, "grayReleaseRuleRepository", mocked);
        ReflectionTestUtils.setField(v7, "clusterService", v3);
        ReflectionTestUtils.setField(v7, "releaseService", v4);
        ReflectionTestUtils.setField(v7, "namespaceService", v5);
        ReflectionTestUtils.setField(v7, "releaseHistoryService", v6);
        GrayReleaseRule v8 = v7.updateRulesReleaseId("test", "default5", "application", "child-cluster5", 9L, "apollo");
        Assertions.assertNotNull(v8);
        Assertions.assertEquals("default5", v8.getClusterName());
        Assertions.assertEquals("child-cluster5", v8.getBranchName());
        Assertions.assertEquals(9L, v8.getReleaseId());
        Assertions.assertEquals(1, v8.getBranchStatus());
        Assertions.assertEquals(0L, v8.getDeletedAt());
        Assertions.assertEquals("apollo", v8.getDataChangeCreatedBy());
        Assertions.assertEquals("apollo", v8.getDataChangeLastModifiedBy());
        Assertions.assertEquals("test", v8.getAppId());
        Assertions.assertEquals("application", v8.getNamespaceName());
        Assertions.assertEquals("[]", v8.getRules());
        Assertions.assertFalse(v8.isDeleted());
        Assertions.assertNotNull(v8.getDataChangeCreatedTime());
        Assertions.assertNotNull(v8.getDataChangeLastModifiedTime());
    }
}

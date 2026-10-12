package com.ctrip.framework.apollo.biz.service;

import org.junit.jupiter.api.Test;
import com.ctrip.framework.apollo.biz.entity.GrayReleaseRule;
import com.ctrip.framework.apollo.biz.entity.ReleaseHistory;
import java.util.Date;
import org.mockito.Mockito;
import com.ctrip.framework.apollo.biz.service.AuditService;
import com.ctrip.framework.apollo.biz.repository.GrayReleaseRuleRepository;
import org.mockito.invocation.InvocationOnMock;
import com.ctrip.framework.apollo.biz.service.ClusterService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.ReleaseHistoryService;
import java.util.Map;
import com.ctrip.framework.apollo.biz.service.NamespaceBranchService;
import org.springframework.test.util.ReflectionTestUtils;
import java.lang.*;

class NamespaceBranchServiceReproducedTests {

    @Test
    void updateBranchGrayRulesReproducedTest() {
        GrayReleaseRule v = new GrayReleaseRule();
        v.setClusterName("default");
        v.setBranchName("child-cluster");
        v.setBranchStatus(1);
        v.setAppId("test");
        v.setNamespaceName("application");
        v.setRules("[{\"clientAppId\":\"test\",\"clientIpList\":[\"1.0.0.4\"],\"clientLabelList\":[]}]");
        ReleaseHistory v1 = new ReleaseHistory();
        v1.setBranchName("child-cluster");
        v1.setOperation(3);
        v1.setId(1L);
        v1.setAppId("test");
        v1.setOperationContext("{\"rules\":[{\"clientAppId\":\"test\",\"clientIpList\":[\"1.0.0.4\"],\"clientLabelList\":[]}]}");
        v1.setDataChangeCreatedTime(new Date(1791727268277L));
        v1.setClusterName("default");
        v1.setNamespaceName("application");
        v1.setDataChangeLastModifiedTime(new Date(1791727268278L));
        AuditService v2 = Mockito.mock(AuditService.class);
        GrayReleaseRuleRepository mocked = Mockito.mock(GrayReleaseRuleRepository.class);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        ClusterService v3 = Mockito.mock(ClusterService.class);
        ReleaseService v4 = Mockito.mock(ReleaseService.class);
        NamespaceService v5 = Mockito.mock(NamespaceService.class);
        ReleaseHistoryService mocked1 = Mockito.mock(ReleaseHistoryService.class);
        Mockito.when(mocked1.createReleaseHistory(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), (Map<String, Object>) (Object) Mockito.<Map<String, Object>>any(), Mockito.<String>any())).thenReturn(v1);
        NamespaceBranchService v6 = new NamespaceBranchService(v2, mocked, v3, v4, v5, mocked1);
        ReflectionTestUtils.setField(v6, "auditService", v2);
        ReflectionTestUtils.setField(v6, "grayReleaseRuleRepository", mocked);
        ReflectionTestUtils.setField(v6, "clusterService", v3);
        ReflectionTestUtils.setField(v6, "releaseService", v4);
        ReflectionTestUtils.setField(v6, "namespaceService", v5);
        ReflectionTestUtils.setField(v6, "releaseHistoryService", mocked1);
        v6.updateBranchGrayRules("test", "default", "application", "child-cluster", v);
    }

    @Test
    void updateBranchGrayRulesReproducedTest1() {
        GrayReleaseRule v = new GrayReleaseRule();
        v.setClusterName("default");
        v.setBranchName("child-cluster");
        v.setBranchStatus(1);
        v.setAppId("test");
        v.setNamespaceName("application");
        v.setRules("[{\"clientAppId\":\"test\",\"clientIpList\":[\"1.0.0.4\"],\"clientLabelList\":[]}]");
        ReleaseHistory v1 = new ReleaseHistory();
        v1.setBranchName("child-cluster");
        v1.setOperation(3);
        v1.setId(2L);
        v1.setAppId("test");
        v1.setOperationContext("{\"rules\":[{\"clientAppId\":\"test\",\"clientIpList\":[\"1.0.0.4\"],\"clientLabelList\":[]}]}");
        v1.setDataChangeCreatedTime(new Date(1791727269461L));
        v1.setClusterName("default");
        v1.setNamespaceName("application");
        v1.setDataChangeLastModifiedTime(new Date(1791727269462L));
        AuditService v2 = Mockito.mock(AuditService.class);
        GrayReleaseRuleRepository mocked = Mockito.mock(GrayReleaseRuleRepository.class);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        ClusterService v3 = Mockito.mock(ClusterService.class);
        ReleaseService v4 = Mockito.mock(ReleaseService.class);
        NamespaceService v5 = Mockito.mock(NamespaceService.class);
        ReleaseHistoryService mocked1 = Mockito.mock(ReleaseHistoryService.class);
        Mockito.when(mocked1.createReleaseHistory(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), (Map<String, Object>) (Object) Mockito.<Map<String, Object>>any(), Mockito.<String>any())).thenReturn(v1);
        NamespaceBranchService v6 = new NamespaceBranchService(v2, mocked, v3, v4, v5, mocked1);
        ReflectionTestUtils.setField(v6, "auditService", v2);
        ReflectionTestUtils.setField(v6, "grayReleaseRuleRepository", mocked);
        ReflectionTestUtils.setField(v6, "clusterService", v3);
        ReflectionTestUtils.setField(v6, "releaseService", v4);
        ReflectionTestUtils.setField(v6, "namespaceService", v5);
        ReflectionTestUtils.setField(v6, "releaseHistoryService", mocked1);
        v6.updateBranchGrayRules("test", "default", "application", "child-cluster", v);
    }

    @Test
    void updateBranchGrayRulesReproducedTest2() {
        GrayReleaseRule v = new GrayReleaseRule();
        v.setClusterName("default");
        v.setBranchName("child-cluster");
        v.setBranchStatus(1);
        v.setAppId("test");
        v.setNamespaceName("application");
        v.setRules("[{\"clientAppId\":\"test\",\"clientIpList\":[\"1.0.0.4\"],\"clientLabelList\":[]}]");
        ReleaseHistory v1 = new ReleaseHistory();
        v1.setBranchName("child-cluster");
        v1.setOperation(3);
        v1.setId(4L);
        v1.setAppId("test");
        v1.setOperationContext("{\"rules\":[{\"clientAppId\":\"test\",\"clientIpList\":[\"1.0.0.4\"],\"clientLabelList\":[]}]}");
        v1.setDataChangeCreatedTime(new Date(1791727270673L));
        v1.setClusterName("default");
        v1.setNamespaceName("application");
        v1.setDataChangeLastModifiedTime(new Date(1791727270673L));
        AuditService v2 = Mockito.mock(AuditService.class);
        GrayReleaseRuleRepository mocked = Mockito.mock(GrayReleaseRuleRepository.class);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        ClusterService v3 = Mockito.mock(ClusterService.class);
        ReleaseService v4 = Mockito.mock(ReleaseService.class);
        NamespaceService v5 = Mockito.mock(NamespaceService.class);
        ReleaseHistoryService mocked1 = Mockito.mock(ReleaseHistoryService.class);
        Mockito.when(mocked1.createReleaseHistory(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), (Map<String, Object>) (Object) Mockito.<Map<String, Object>>any(), Mockito.<String>any())).thenReturn(v1);
        NamespaceBranchService v6 = new NamespaceBranchService(v2, mocked, v3, v4, v5, mocked1);
        ReflectionTestUtils.setField(v6, "auditService", v2);
        ReflectionTestUtils.setField(v6, "grayReleaseRuleRepository", mocked);
        ReflectionTestUtils.setField(v6, "clusterService", v3);
        ReflectionTestUtils.setField(v6, "releaseService", v4);
        ReflectionTestUtils.setField(v6, "namespaceService", v5);
        ReflectionTestUtils.setField(v6, "releaseHistoryService", mocked1);
        v6.updateBranchGrayRules("test", "default", "application", "child-cluster", v);
    }

    @Test
    void updateBranchGrayRulesReproducedTest3() {
        GrayReleaseRule v = new GrayReleaseRule();
        v.setClusterName("default");
        v.setBranchName("child-cluster");
        v.setBranchStatus(1);
        v.setAppId("test");
        v.setNamespaceName("application");
        v.setRules("[{\"clientAppId\":\"test\",\"clientIpList\":[\"1.0.0.4\"],\"clientLabelList\":[]}]");
        ReleaseHistory v1 = new ReleaseHistory();
        v1.setBranchName("child-cluster");
        v1.setOperation(3);
        v1.setId(5L);
        v1.setAppId("test");
        v1.setOperationContext("{\"rules\":[{\"clientAppId\":\"test\",\"clientIpList\":[\"1.0.0.4\"],\"clientLabelList\":[]}]}");
        v1.setDataChangeCreatedTime(new Date(1791727271794L));
        v1.setClusterName("default");
        v1.setNamespaceName("application");
        v1.setDataChangeLastModifiedTime(new Date(1791727271794L));
        AuditService v2 = Mockito.mock(AuditService.class);
        GrayReleaseRuleRepository mocked = Mockito.mock(GrayReleaseRuleRepository.class);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        ClusterService v3 = Mockito.mock(ClusterService.class);
        ReleaseService v4 = Mockito.mock(ReleaseService.class);
        NamespaceService v5 = Mockito.mock(NamespaceService.class);
        ReleaseHistoryService mocked1 = Mockito.mock(ReleaseHistoryService.class);
        Mockito.when(mocked1.createReleaseHistory(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), (Map<String, Object>) (Object) Mockito.<Map<String, Object>>any(), Mockito.<String>any())).thenReturn(v1);
        NamespaceBranchService v6 = new NamespaceBranchService(v2, mocked, v3, v4, v5, mocked1);
        ReflectionTestUtils.setField(v6, "auditService", v2);
        ReflectionTestUtils.setField(v6, "grayReleaseRuleRepository", mocked);
        ReflectionTestUtils.setField(v6, "clusterService", v3);
        ReflectionTestUtils.setField(v6, "releaseService", v4);
        ReflectionTestUtils.setField(v6, "namespaceService", v5);
        ReflectionTestUtils.setField(v6, "releaseHistoryService", mocked1);
        v6.updateBranchGrayRules("test", "default", "application", "child-cluster", v);
    }

    @Test
    void updateBranchGrayRulesReproducedTest4() {
        GrayReleaseRule v = new GrayReleaseRule();
        v.setClusterName("default");
        v.setBranchName("child-cluster");
        v.setBranchStatus(1);
        v.setId(6L);
        v.setAppId("test");
        v.setNamespaceName("application");
        v.setRules("[{\"clientAppId\":\"test\",\"clientIpList\":[\"1.0.0.4\"],\"clientLabelList\":[]}]");
        v.setDataChangeCreatedTime(new Date(1791727271767L));
        v.setDataChangeLastModifiedTime(new Date(1791727271767L));
        GrayReleaseRule v1 = new GrayReleaseRule();
        v1.setClusterName("default");
        v1.setBranchName("child-cluster");
        v1.setBranchStatus(1);
        v1.setAppId("test");
        v1.setNamespaceName("application");
        v1.setRules("[{\"clientAppId\":\"branch-test\",\"clientIpList\":[\"10.38.57.112\"],\"clientLabelList\":[\"branch-test\"]}]");
        ReleaseHistory v2 = new ReleaseHistory();
        v2.setBranchName("child-cluster");
        v2.setOperation(3);
        v2.setId(6L);
        v2.setAppId("test");
        v2.setOperationContext("{\"oldRules\":[{\"clientAppId\":\"test\",\"clientIpList\":[\"1.0.0.4\"],\"clientLabelList\":[]}],\"rules\":[{\"clientAppId\":\"branch-test\",\"clientIpList\":[\"10.38.57.112\"],\"clientLabelList\":[\"branch-test\"]}]}");
        v2.setDataChangeCreatedTime(new Date(1791727273118L));
        v2.setClusterName("default");
        v2.setNamespaceName("application");
        v2.setDataChangeLastModifiedTime(new Date(1791727273119L));
        AuditService v3 = Mockito.mock(AuditService.class);
        GrayReleaseRuleRepository mocked = Mockito.mock(GrayReleaseRuleRepository.class);
        Mockito.when(mocked.findTopByAppIdAndClusterNameAndNamespaceNameAndBranchNameOrderByIdDesc(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any())).thenReturn(v);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v1);
        ClusterService v4 = Mockito.mock(ClusterService.class);
        ReleaseService v5 = Mockito.mock(ReleaseService.class);
        NamespaceService v6 = Mockito.mock(NamespaceService.class);
        ReleaseHistoryService mocked1 = Mockito.mock(ReleaseHistoryService.class);
        Mockito.when(mocked1.createReleaseHistory(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), (Map<String, Object>) (Object) Mockito.<Map<String, Object>>any(), Mockito.<String>any())).thenReturn(v2);
        NamespaceBranchService v7 = new NamespaceBranchService(v3, mocked, v4, v5, v6, mocked1);
        ReflectionTestUtils.setField(v7, "auditService", v3);
        ReflectionTestUtils.setField(v7, "grayReleaseRuleRepository", mocked);
        ReflectionTestUtils.setField(v7, "clusterService", v4);
        ReflectionTestUtils.setField(v7, "releaseService", v5);
        ReflectionTestUtils.setField(v7, "namespaceService", v6);
        ReflectionTestUtils.setField(v7, "releaseHistoryService", mocked1);
        v7.updateBranchGrayRules("test", "default", "application", "child-cluster", v1);
    }
}

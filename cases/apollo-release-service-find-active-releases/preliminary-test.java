package com.ctrip.framework.apollo.biz.service;

import org.junit.jupiter.api.Test;
import com.ctrip.framework.apollo.biz.entity.Release;
import org.mockito.Mockito;
import com.ctrip.framework.apollo.biz.repository.ReleaseRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import org.mockito.invocation.InvocationOnMock;
import com.ctrip.framework.apollo.biz.service.ItemService;
import com.ctrip.framework.apollo.biz.service.AuditService;
import com.ctrip.framework.apollo.biz.service.NamespaceLockService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.NamespaceBranchService;
import com.ctrip.framework.apollo.biz.service.ReleaseHistoryService;
import com.ctrip.framework.apollo.biz.service.ItemSetService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class ReleaseServiceReproducedTests {

    @Test
    void findActiveReleasesReproducedTest() {
        Release v = new Release();
        v.setReleaseKey("20160823102253-fc0071ddf9fd3260");
        v.setAppId("test");
        v.setNamespaceName("application");
        v.setComment("");
        v.setId(7L);
        v.setName("20160823101703-release");
        v.setClusterName("default6");
        v.setConfigurations("{\"k1\":\"v1\",\"k2\":\"v2\"}");
        Release v1 = new Release();
        v1.setReleaseKey("20160823102253-fc0071ddf9fd3260");
        v1.setAppId("test");
        v1.setNamespaceName("application");
        v1.setComment("");
        v1.setId(6L);
        v1.setName("20160823101703-release");
        v1.setClusterName("default6");
        v1.setConfigurations("{\"k1\":\"v1-1\",\"k2\":\"v2-1\",\"k3\":\"v3\"}");
        ReleaseRepository mocked = Mockito.mock(ReleaseRepository.class);
        Mockito.when(mocked.findByAppIdAndClusterNameAndNamespaceNameAndIsAbandonedFalseOrderByIdDesc(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any(), Mockito.<Pageable>any())).thenReturn((List<Release>) (Object) new ArrayList<Object>((Collection<Object>) (Object) List.of(v, v1)));
        ItemService v2 = Mockito.mock(ItemService.class);
        AuditService v3 = Mockito.mock(AuditService.class);
        NamespaceLockService v4 = Mockito.mock(NamespaceLockService.class);
        NamespaceService v5 = Mockito.mock(NamespaceService.class);
        NamespaceBranchService v6 = Mockito.mock(NamespaceBranchService.class);
        ReleaseHistoryService v7 = Mockito.mock(ReleaseHistoryService.class);
        ItemSetService v8 = Mockito.mock(ItemSetService.class);
        ReleaseService v9 = new ReleaseService(mocked, v2, v3, v4, v5, v6, v7, v8);
        ReflectionTestUtils.setField(v9, "releaseRepository", mocked);
        ReflectionTestUtils.setField(v9, "itemService", v2);
        ReflectionTestUtils.setField(v9, "auditService", v3);
        ReflectionTestUtils.setField(v9, "namespaceLockService", v4);
        ReflectionTestUtils.setField(v9, "namespaceService", v5);
        ReflectionTestUtils.setField(v9, "namespaceBranchService", v6);
        ReflectionTestUtils.setField(v9, "releaseHistoryService", v7);
        ReflectionTestUtils.setField(v9, "itemSetService", v8);
        List<Object> v10 = v9.findActiveReleases("test", "default6", "application", Mockito.mock(PageRequest.class));
        Assertions.assertEquals(2, v10.size());
    }
}

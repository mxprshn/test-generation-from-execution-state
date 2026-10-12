package com.ctrip.framework.apollo.biz.service;

import org.junit.jupiter.api.Test;
import com.ctrip.framework.apollo.biz.entity.InstanceConfig;
import java.util.Date;
import org.mockito.Mockito;
import com.ctrip.framework.apollo.biz.repository.InstanceRepository;
import com.ctrip.framework.apollo.biz.repository.InstanceConfigRepository;
import java.util.Optional;
import org.mockito.invocation.InvocationOnMock;
import com.ctrip.framework.apollo.biz.service.InstanceService;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class InstanceServiceReproducedTests {

    @Test
    void updateInstanceConfigReproducedTest() {
        InstanceConfig v = new InstanceConfig();
        v.setId(8L);
        v.setInstanceId(1L);
        v.setConfigClusterName("someConfigClusterName");
        v.setReleaseKey("anotherReleaseKey");
        Date v1 = new Date(1791727201772L);
        v.setDataChangeCreatedTime(v1);
        v.setConfigAppId("someConfigAppId");
        v.setConfigNamespaceName("someConfigNamespaceName");
        v.setDataChangeLastModifiedTime(v1);
        InstanceRepository v2 = Mockito.mock(InstanceRepository.class);
        InstanceConfigRepository mocked = Mockito.mock(InstanceConfigRepository.class);
        Mockito.when(mocked.findById(Mockito.<Object>any())).thenReturn((Optional<Object>) (Object) Optional.of(v));
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        InstanceService v3 = new InstanceService(v2, mocked);
        ReflectionTestUtils.setField(v3, "instanceRepository", v2);
        ReflectionTestUtils.setField(v3, "instanceConfigRepository", mocked);
        InstanceConfig v4 = v3.updateInstanceConfig(v);
        Assertions.assertNotNull(v4);
        Assertions.assertEquals(1L, v4.getInstanceId());
        Assertions.assertEquals("someConfigClusterName", v4.getConfigClusterName());
        Assertions.assertEquals("anotherReleaseKey", v4.getReleaseKey());
        Assertions.assertNotNull(v4.getDataChangeCreatedTime());
        Assertions.assertEquals("someConfigAppId", v4.getConfigAppId());
        Assertions.assertEquals("someConfigNamespaceName", v4.getConfigNamespaceName());
    }
}

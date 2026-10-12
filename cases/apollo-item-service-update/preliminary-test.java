package com.ctrip.framework.apollo.biz.service;

import org.junit.jupiter.api.Test;
import com.ctrip.framework.apollo.biz.entity.Item;
import com.ctrip.framework.apollo.biz.entity.Namespace;
import java.util.regex.Pattern;
import org.springframework.test.util.ReflectionTestUtils;
import com.ctrip.framework.apollo.biz.service.ItemService;
import org.mockito.Mockito;
import com.ctrip.framework.apollo.biz.repository.ItemRepository;
import java.util.Optional;
import org.mockito.invocation.InvocationOnMock;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.AuditService;
import com.ctrip.framework.apollo.biz.config.BizConfig;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class ItemServiceReproducedTests {

    @Test
    void updateReproducedTest() {
        Item v = new Item();
        v.setNamespaceId(1L);
        v.setValue("v1 update");
        v.setLineNum(1);
        v.setId(9901L);
        v.setKey("k1");
        v.setComment("");
        Namespace v1 = new Namespace();
        v1.setAppId("testApp");
        v1.setNamespaceName("application");
        v1.setId(1L);
        v1.setDataChangeCreatedBy("apollo");
        v1.setDataChangeLastModifiedBy("apollo");
        v1.setClusterName("default");
        ReflectionTestUtils.setField(ItemService.class, "clusterPattern", Pattern.compile("[0-9]{14}-[a-zA-Z0-9]{16}", 0));
        ItemRepository mocked = Mockito.mock(ItemRepository.class);
        Mockito.when(mocked.findById(Mockito.<Object>any())).thenReturn((Optional<Object>) (Object) Optional.of(v));
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        NamespaceService mocked1 = Mockito.mock(NamespaceService.class);
        Mockito.when(mocked1.findOne(Mockito.<Long>any())).thenReturn(v1);
        AuditService v2 = Mockito.mock(AuditService.class);
        BizConfig mocked2 = Mockito.mock(BizConfig.class);
        Mockito.when(mocked2.namespaceValueLengthLimitOverride()).thenReturn((Map<Long, Integer>) (Object) Map.of());
        Mockito.when(mocked2.appIdValueLengthLimitOverride()).thenReturn((Map<String, Integer>) (Object) Map.of());
        Mockito.when(mocked2.itemValueLengthLimit()).thenReturn(20000);
        ItemService v3 = new ItemService(mocked, mocked1, v2, mocked2);
        ReflectionTestUtils.setField(v3, "itemRepository", mocked);
        ReflectionTestUtils.setField(v3, "namespaceService", mocked1);
        ReflectionTestUtils.setField(v3, "auditService", v2);
        ReflectionTestUtils.setField(v3, "bizConfig", mocked2);
        Item v4 = v3.update(v);
        Assertions.assertNotNull(v4);
        Assertions.assertEquals(1L, v4.getNamespaceId());
        Assertions.assertEquals(0, v4.getType());
        Assertions.assertEquals("v1 update", v4.getValue());
        Assertions.assertEquals(1, v4.getLineNum());
        Assertions.assertEquals(0L, v4.getDeletedAt());
        Assertions.assertEquals("k1", v4.getKey());
        Assertions.assertEquals("", v4.getComment());
        Assertions.assertFalse(v4.isDeleted());
    }
}

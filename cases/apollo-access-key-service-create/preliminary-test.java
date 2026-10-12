package com.ctrip.framework.apollo.biz.service;

import org.junit.jupiter.api.Test;
import com.ctrip.framework.apollo.biz.entity.AccessKey;
import org.mockito.Mockito;
import com.ctrip.framework.apollo.biz.repository.AccessKeyRepository;
import org.mockito.invocation.InvocationOnMock;
import com.ctrip.framework.apollo.biz.service.AuditService;
import com.ctrip.framework.apollo.biz.service.AccessKeyService;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Assertions;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import java.lang.*;

class AccessKeyServiceReproducedTests {

    @Test
    void createReproducedTest() {
        AccessKey v = new AccessKey();
        v.setSecret("someSecret");
        v.setAppId("someAppId");
        AccessKeyRepository mocked = Mockito.mock(AccessKeyRepository.class);
        Mockito.when(mocked.countByAppId(Mockito.<String>any())).thenReturn(0L);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        AuditService v1 = Mockito.mock(AuditService.class);
        AccessKeyService v2 = new AccessKeyService(mocked, v1);
        ReflectionTestUtils.setField(v2, "accessKeyRepository", mocked);
        ReflectionTestUtils.setField(v2, "auditService", v1);
        AccessKey v3 = v2.create("someAppId", v);
        Assertions.assertNotNull(v3);
        Assertions.assertEquals("someSecret", v3.getSecret());
        Assertions.assertEquals(0, v3.getMode());
        Assertions.assertEquals(0L, v3.getDeletedAt());
        Assertions.assertEquals("someAppId", v3.getAppId());
        Assertions.assertFalse(v3.isDeleted());
        Assertions.assertNotNull(v3.getDataChangeCreatedTime());
        Assertions.assertNotNull(v3.getDataChangeLastModifiedTime());
        Assertions.assertFalse(v3.isEnabled());
    }

    @Test
    void createReproducedTest1() {
        AccessKey v = new AccessKey();
        v.setSecret("someSecret");
        v.setAppId("someAppId");
        AccessKeyRepository mocked = Mockito.mock(AccessKeyRepository.class);
        Mockito.when(mocked.countByAppId(Mockito.<String>any())).thenReturn(1L);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        AuditService v1 = Mockito.mock(AuditService.class);
        AccessKeyService v2 = new AccessKeyService(mocked, v1);
        ReflectionTestUtils.setField(v2, "accessKeyRepository", mocked);
        ReflectionTestUtils.setField(v2, "auditService", v1);
        AccessKey v3 = v2.create("someAppId", v);
        Assertions.assertNotNull(v3);
        Assertions.assertEquals("someSecret", v3.getSecret());
        Assertions.assertEquals(0, v3.getMode());
        Assertions.assertEquals(0L, v3.getDeletedAt());
        Assertions.assertEquals("someAppId", v3.getAppId());
        Assertions.assertFalse(v3.isDeleted());
        Assertions.assertNotNull(v3.getDataChangeCreatedTime());
        Assertions.assertNotNull(v3.getDataChangeLastModifiedTime());
        Assertions.assertFalse(v3.isEnabled());
    }

    @Test
    void createReproducedTest2() {
        AccessKey v = new AccessKey();
        v.setSecret("someSecret");
        v.setAppId("someAppId");
        AccessKeyRepository mocked = Mockito.mock(AccessKeyRepository.class);
        Mockito.when(mocked.countByAppId(Mockito.<String>any())).thenReturn(2L);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        AuditService v1 = Mockito.mock(AuditService.class);
        AccessKeyService v2 = new AccessKeyService(mocked, v1);
        ReflectionTestUtils.setField(v2, "accessKeyRepository", mocked);
        ReflectionTestUtils.setField(v2, "auditService", v1);
        AccessKey v3 = v2.create("someAppId", v);
        Assertions.assertNotNull(v3);
        Assertions.assertEquals("someSecret", v3.getSecret());
        Assertions.assertEquals(0, v3.getMode());
        Assertions.assertEquals(0L, v3.getDeletedAt());
        Assertions.assertEquals("someAppId", v3.getAppId());
        Assertions.assertFalse(v3.isDeleted());
        Assertions.assertNotNull(v3.getDataChangeCreatedTime());
        Assertions.assertNotNull(v3.getDataChangeLastModifiedTime());
        Assertions.assertFalse(v3.isEnabled());
    }

    @Test
    void createReproducedTest3() {
        AccessKey v = new AccessKey();
        v.setSecret("someSecret");
        v.setAppId("someAppId");
        AccessKeyRepository mocked = Mockito.mock(AccessKeyRepository.class);
        Mockito.when(mocked.countByAppId(Mockito.<String>any())).thenReturn(3L);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        AuditService v1 = Mockito.mock(AuditService.class);
        AccessKeyService v2 = new AccessKeyService(mocked, v1);
        ReflectionTestUtils.setField(v2, "accessKeyRepository", mocked);
        ReflectionTestUtils.setField(v2, "auditService", v1);
        AccessKey v3 = v2.create("someAppId", v);
        Assertions.assertNotNull(v3);
        Assertions.assertEquals("someSecret", v3.getSecret());
        Assertions.assertEquals(0, v3.getMode());
        Assertions.assertEquals(0L, v3.getDeletedAt());
        Assertions.assertEquals("someAppId", v3.getAppId());
        Assertions.assertFalse(v3.isDeleted());
        Assertions.assertNotNull(v3.getDataChangeCreatedTime());
        Assertions.assertNotNull(v3.getDataChangeLastModifiedTime());
        Assertions.assertFalse(v3.isEnabled());
    }

    @Test
    void createReproducedTest4() {
        AccessKey v = new AccessKey();
        v.setSecret("someSecret");
        v.setAppId("someAppId");
        AccessKeyRepository mocked = Mockito.mock(AccessKeyRepository.class);
        Mockito.when(mocked.countByAppId(Mockito.<String>any())).thenReturn(4L);
        Mockito.when(mocked.save(Mockito.<Object>any())).thenReturn(v);
        AuditService v1 = Mockito.mock(AuditService.class);
        AccessKeyService v2 = new AccessKeyService(mocked, v1);
        ReflectionTestUtils.setField(v2, "accessKeyRepository", mocked);
        ReflectionTestUtils.setField(v2, "auditService", v1);
        AccessKey v3 = v2.create("someAppId", v);
        Assertions.assertNotNull(v3);
        Assertions.assertEquals("someSecret", v3.getSecret());
        Assertions.assertEquals(0, v3.getMode());
        Assertions.assertEquals(0L, v3.getDeletedAt());
        Assertions.assertEquals("someAppId", v3.getAppId());
        Assertions.assertFalse(v3.isDeleted());
        Assertions.assertNotNull(v3.getDataChangeCreatedTime());
        Assertions.assertNotNull(v3.getDataChangeLastModifiedTime());
        Assertions.assertFalse(v3.isEnabled());
    }

    @Test
    void createReproducedTest5() {
        AccessKeyRepository mocked = Mockito.mock(AccessKeyRepository.class);
        Mockito.when(mocked.countByAppId(Mockito.<String>any())).thenReturn(5L);
        AuditService v = Mockito.mock(AuditService.class);
        AccessKeyService v1 = new AccessKeyService(mocked, v);
        ReflectionTestUtils.setField(v1, "accessKeyRepository", mocked);
        ReflectionTestUtils.setField(v1, "auditService", v);
        AccessKey v2 = new AccessKey();
        v2.setSecret("someSecret");
        v2.setAppId("someAppId");
        Assertions.assertThrows(BadRequestException.class, () -> {
            v1.create("someAppId", v2);
        });
    }

}

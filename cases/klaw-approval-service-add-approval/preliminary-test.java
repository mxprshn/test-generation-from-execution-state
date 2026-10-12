package io.aiven.klaw.service;

import org.junit.jupiter.api.Test;
import io.aiven.klaw.dao.UserInfo;
import java.util.Map;
import java.util.List;
import io.aiven.klaw.dao.Approval;
import io.aiven.klaw.service.ApprovalService;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mockito;
import io.aiven.klaw.service.MailUtils;
import io.aiven.klaw.service.CommonUtilsService;
import io.aiven.klaw.config.ManageDatabase;
import org.mockito.invocation.InvocationOnMock;
import io.aiven.klaw.model.enums.ApprovalType;
import io.aiven.klaw.error.KlawBadRequestException;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class ApprovalServiceReproducedTests {

    @Test
    void addApprovalReproducedTest() throws KlawBadRequestException {
        UserInfo v = new UserInfo();
        v.setRole("USER");
        v.setMailid("John@klaw.mailid");
        v.setUsername("John");
        v.setTenantId(101);
        v.setTeamId(10);
        UserInfo v1 = new UserInfo();
        v1.setRole("USER");
        v1.setMailid("James@klaw.mailid");
        v1.setUsername("James");
        v1.setTenantId(101);
        v1.setTeamId(11);
        UserInfo v2 = new UserInfo();
        v2.setRole("USER");
        v2.setMailid("Jackie@klaw.mailid");
        v2.setUsername("Jackie");
        v2.setTenantId(101);
        v2.setTeamId(13);
        ApprovalService v3 = new ApprovalService((Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null);
        ReflectionTestUtils.setField(v3, "topicApprovals", null);
        ReflectionTestUtils.setField(v3, "aclApprovals", null);
        ReflectionTestUtils.setField(v3, "schemaApprovals", null);
        ReflectionTestUtils.setField(v3, "connectApprovals", null);
        ReflectionTestUtils.setField(v3, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v3, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked.selectAllCachedUserInfo()).thenReturn((List<UserInfo>) (Object) List.of(v, v1, v2));
        Mockito.when(mocked.getTeamNameFromTeamId(Mockito.anyInt(), Mockito.anyInt())).thenReturn("SUPPORT_TEAM");
        ReflectionTestUtils.setField(v3, "manageDatabase", mocked);
        ReflectionTestUtils.setField(v3, "allowMultiApproval", true);
        Approval v4 = new Approval();
        v4.setApprovalId(1);
        v4.setApprovalType(ApprovalType.TEAM);
        v4.setRequiredApprover("SUPPORT_TEAM");
        Approval v5 = new Approval();
        v5.setApprovalId(2);
        v5.setApprovalType(ApprovalType.TOPIC_TEAM_OWNER);
        v5.setRequiredApprover("Octopus");
        List<Object> v6 = v3.addApproval((List<Approval>) (Object) List.of(v4, v5), "Jackie", 10, null);
        Assertions.assertEquals(2, v6.size());
    }

    @Test
    void addApprovalReproducedTest1() throws KlawBadRequestException {
        UserInfo v = new UserInfo();
        v.setRole("USER");
        v.setMailid("John@klaw.mailid");
        v.setUsername("John");
        v.setTenantId(101);
        v.setTeamId(10);
        UserInfo v1 = new UserInfo();
        v1.setRole("USER");
        v1.setMailid("James@klaw.mailid");
        v1.setUsername("James");
        v1.setTenantId(101);
        v1.setTeamId(11);
        UserInfo v2 = new UserInfo();
        v2.setRole("USER");
        v2.setMailid("Jackie@klaw.mailid");
        v2.setUsername("Jackie");
        v2.setTenantId(101);
        v2.setTeamId(13);
        ApprovalService v3 = new ApprovalService((Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null);
        ReflectionTestUtils.setField(v3, "topicApprovals", null);
        ReflectionTestUtils.setField(v3, "aclApprovals", null);
        ReflectionTestUtils.setField(v3, "schemaApprovals", null);
        ReflectionTestUtils.setField(v3, "connectApprovals", null);
        ReflectionTestUtils.setField(v3, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v3, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked.selectAllCachedUserInfo()).thenReturn((List<UserInfo>) (Object) List.of(v, v1, v2));
        ReflectionTestUtils.setField(v3, "manageDatabase", mocked);
        ReflectionTestUtils.setField(v3, "allowMultiApproval", true);
        Approval v4 = new Approval();
        v4.setApprovalId(1);
        ApprovalType v5 = ApprovalType.TOPIC_TEAM_OWNER;
        v4.setApprovalType(v5);
        v4.setRequiredApprover("Octopus");
        Approval v6 = new Approval();
        v6.setApprovalId(2);
        v6.setApprovalType(v5);
        v6.setRequiredApprover("Octopus");
        List<Object> v7 = v3.addApproval((List<Approval>) (Object) List.of(v4, v6), "John", 11, null);
        Assertions.assertEquals(2, v7.size());
    }

    @Test
    void addApprovalReproducedTest2() throws KlawBadRequestException {
        ApprovalService v = new ApprovalService((Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null);
        ReflectionTestUtils.setField(v, "topicApprovals", null);
        ReflectionTestUtils.setField(v, "aclApprovals", null);
        ReflectionTestUtils.setField(v, "schemaApprovals", null);
        ReflectionTestUtils.setField(v, "connectApprovals", null);
        ReflectionTestUtils.setField(v, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ReflectionTestUtils.setField(v, "manageDatabase", Mockito.mock(ManageDatabase.class));
        ReflectionTestUtils.setField(v, "allowMultiApproval", true);
        Approval v1 = new Approval();
        v1.setApprovalId(1);
        ApprovalType v2 = ApprovalType.TOPIC_TEAM_OWNER;
        v1.setApprovalType(v2);
        v1.setRequiredApprover("Octopus");
        v1.setApproverName("James");
        v1.setApproverTeamId(11);
        v1.setApproverTeamName("Octopus");
        Approval v3 = new Approval();
        v3.setApprovalId(2);
        v3.setApprovalType(v2);
        v3.setRequiredApprover("Octopus");
        Assertions.assertThrows(KlawBadRequestException.class, () -> {
            v.addApproval((List<Approval>) (Object) List.of(v1, v3), "James", 11, null);
        });
    }

    @Test
    void addApprovalReproducedTest3() throws KlawBadRequestException {
        UserInfo v = new UserInfo();
        v.setRole("USER");
        v.setMailid("John@klaw.mailid");
        v.setUsername("John");
        v.setTenantId(101);
        v.setTeamId(10);
        UserInfo v1 = new UserInfo();
        v1.setRole("USER");
        v1.setMailid("James@klaw.mailid");
        v1.setUsername("James");
        v1.setTenantId(101);
        v1.setTeamId(11);
        UserInfo v2 = new UserInfo();
        v2.setRole("USER");
        v2.setMailid("Jackie@klaw.mailid");
        v2.setUsername("Jackie");
        v2.setTenantId(101);
        v2.setTeamId(13);
        ApprovalService v3 = new ApprovalService((Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null);
        ReflectionTestUtils.setField(v3, "topicApprovals", null);
        ReflectionTestUtils.setField(v3, "aclApprovals", null);
        ReflectionTestUtils.setField(v3, "schemaApprovals", null);
        ReflectionTestUtils.setField(v3, "connectApprovals", null);
        ReflectionTestUtils.setField(v3, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v3, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked.selectAllCachedUserInfo()).thenReturn((List<UserInfo>) (Object) List.of(v, v1, v2));
        Mockito.when(mocked.getTeamNameFromTeamId(Mockito.anyInt(), Mockito.anyInt())).thenReturn("Octopus");
        ReflectionTestUtils.setField(v3, "manageDatabase", mocked);
        ReflectionTestUtils.setField(v3, "allowMultiApproval", true);
        Approval v4 = new Approval();
        v4.setApprovalId(1);
        ApprovalType v5 = ApprovalType.TOPIC_TEAM_OWNER;
        v4.setApprovalType(v5);
        v4.setRequiredApprover("Octopus");
        Approval v6 = new Approval();
        v6.setApprovalId(2);
        v6.setApprovalType(v5);
        v6.setRequiredApprover("Octopus");
        List<Object> v7 = v3.addApproval((List<Approval>) (Object) List.of(v4, v6), "James", 11, null);
        Assertions.assertEquals(2, v7.size());
    }

    @Test
    void addApprovalReproducedTest4() throws KlawBadRequestException {
        UserInfo v = new UserInfo();
        v.setRole("USER");
        v.setMailid("John@klaw.mailid");
        v.setUsername("John");
        v.setTenantId(101);
        v.setTeamId(10);
        UserInfo v1 = new UserInfo();
        v1.setRole("USER");
        v1.setMailid("James@klaw.mailid");
        v1.setUsername("James");
        v1.setTenantId(101);
        v1.setTeamId(11);
        UserInfo v2 = new UserInfo();
        v2.setRole("USER");
        v2.setMailid("Jackie@klaw.mailid");
        v2.setUsername("Jackie");
        v2.setTenantId(101);
        v2.setTeamId(13);
        ApprovalService v3 = new ApprovalService((Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null);
        ReflectionTestUtils.setField(v3, "topicApprovals", null);
        ReflectionTestUtils.setField(v3, "aclApprovals", null);
        ReflectionTestUtils.setField(v3, "schemaApprovals", null);
        ReflectionTestUtils.setField(v3, "connectApprovals", null);
        ReflectionTestUtils.setField(v3, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v3, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked.selectAllCachedUserInfo()).thenReturn((List<UserInfo>) (Object) List.of(v, v1, v2));
        Mockito.when(mocked.getTeamNameFromTeamId(Mockito.anyInt(), Mockito.anyInt())).thenReturn("ALICE");
        ReflectionTestUtils.setField(v3, "manageDatabase", mocked);
        ReflectionTestUtils.setField(v3, "allowMultiApproval", true);
        Approval v4 = new Approval();
        v4.setApprovalId(1);
        v4.setApprovalType(ApprovalType.TOPIC_TEAM_OWNER);
        v4.setRequiredApprover("ALICE");
        Approval v5 = new Approval();
        v5.setApprovalId(2);
        v5.setApprovalType(ApprovalType.ACL_TEAM_OWNER);
        v5.setRequiredApprover("Octopus");
        List<Object> v6 = v3.addApproval((List<Approval>) (Object) List.of(v4, v5), "John", 11, 10);
        Assertions.assertEquals(2, v6.size());
    }

    @Test
    void addApprovalReproducedTest5() throws KlawBadRequestException {
        UserInfo v = new UserInfo();
        v.setRole("USER");
        v.setMailid("John@klaw.mailid");
        v.setUsername("John");
        v.setTenantId(101);
        v.setTeamId(10);
        UserInfo v1 = new UserInfo();
        v1.setRole("USER");
        v1.setMailid("James@klaw.mailid");
        v1.setUsername("James");
        v1.setTenantId(101);
        v1.setTeamId(11);
        UserInfo v2 = new UserInfo();
        v2.setRole("USER");
        v2.setMailid("Jackie@klaw.mailid");
        v2.setUsername("Jackie");
        v2.setTenantId(101);
        v2.setTeamId(13);
        ApprovalService v3 = new ApprovalService((Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null);
        ReflectionTestUtils.setField(v3, "topicApprovals", null);
        ReflectionTestUtils.setField(v3, "aclApprovals", null);
        ReflectionTestUtils.setField(v3, "schemaApprovals", null);
        ReflectionTestUtils.setField(v3, "connectApprovals", null);
        ReflectionTestUtils.setField(v3, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v3, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked.selectAllCachedUserInfo()).thenReturn((List<UserInfo>) (Object) List.of(v, v1, v2));
        Mockito.when(mocked.getTeamNameFromTeamId(Mockito.anyInt(), Mockito.anyInt())).thenReturn("SUPPORT_TEAM");
        ReflectionTestUtils.setField(v3, "manageDatabase", mocked);
        ReflectionTestUtils.setField(v3, "allowMultiApproval", true);
        Approval v4 = new Approval();
        v4.setApprovalId(1);
        v4.setApprovalType(ApprovalType.TEAM);
        v4.setRequiredApprover("SUPPORT_TEAM");
        Approval v5 = new Approval();
        v5.setApprovalId(2);
        v5.setApprovalType(ApprovalType.ACL_TEAM_OWNER);
        v5.setRequiredApprover("Octopus");
        List<Object> v6 = v3.addApproval((List<Approval>) (Object) List.of(v4, v5), "Jackie", 10, 11);
        Assertions.assertEquals(2, v6.size());
    }

    @Test
    void addApprovalReproducedTest6() throws KlawBadRequestException {
        UserInfo v = new UserInfo();
        v.setRole("USER");
        v.setMailid("John@klaw.mailid");
        v.setUsername("John");
        v.setTenantId(101);
        v.setTeamId(10);
        UserInfo v1 = new UserInfo();
        v1.setRole("USER");
        v1.setMailid("James@klaw.mailid");
        v1.setUsername("James");
        v1.setTenantId(101);
        v1.setTeamId(11);
        UserInfo v2 = new UserInfo();
        v2.setRole("USER");
        v2.setMailid("Jackie@klaw.mailid");
        v2.setUsername("Jackie");
        v2.setTenantId(101);
        v2.setTeamId(13);
        ApprovalService v3 = new ApprovalService((Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null);
        ReflectionTestUtils.setField(v3, "topicApprovals", null);
        ReflectionTestUtils.setField(v3, "aclApprovals", null);
        ReflectionTestUtils.setField(v3, "schemaApprovals", null);
        ReflectionTestUtils.setField(v3, "connectApprovals", null);
        ReflectionTestUtils.setField(v3, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v3, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked.selectAllCachedUserInfo()).thenReturn((List<UserInfo>) (Object) List.of(v, v1, v2));
        Mockito.when(mocked.getTeamNameFromTeamId(Mockito.anyInt(), Mockito.anyInt())).thenReturn("ALICE");
        ReflectionTestUtils.setField(v3, "manageDatabase", mocked);
        ReflectionTestUtils.setField(v3, "allowMultiApproval", true);
        Approval v4 = new Approval();
        v4.setApprovalId(1);
        v4.setApprovalType(ApprovalType.TOPIC_TEAM_OWNER);
        v4.setRequiredApprover("ALICE");
        Approval v5 = new Approval();
        v5.setApprovalId(2);
        v5.setApprovalType(ApprovalType.ACL_TEAM_OWNER);
        v5.setRequiredApprover("Octopus");
        List<Object> v6 = v3.addApproval((List<Approval>) (Object) List.of(v4, v5), "John", 10, 10);
        Assertions.assertEquals(2, v6.size());
    }

    @Test
    void addApprovalReproducedTest9() throws KlawBadRequestException {
        UserInfo v = new UserInfo();
        v.setRole("USER");
        v.setMailid("John@klaw.mailid");
        v.setUsername("John");
        v.setTenantId(101);
        v.setTeamId(10);
        UserInfo v1 = new UserInfo();
        v1.setRole("USER");
        v1.setMailid("James@klaw.mailid");
        v1.setUsername("James");
        v1.setTenantId(101);
        v1.setTeamId(11);
        UserInfo v2 = new UserInfo();
        v2.setRole("USER");
        v2.setMailid("Jackie@klaw.mailid");
        v2.setUsername("Jackie");
        v2.setTenantId(101);
        v2.setTeamId(13);
        ApprovalService v3 = new ApprovalService((Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null);
        ReflectionTestUtils.setField(v3, "topicApprovals", null);
        ReflectionTestUtils.setField(v3, "aclApprovals", null);
        ReflectionTestUtils.setField(v3, "schemaApprovals", null);
        ReflectionTestUtils.setField(v3, "connectApprovals", null);
        ReflectionTestUtils.setField(v3, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v3, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked.selectAllCachedUserInfo()).thenReturn((List<UserInfo>) (Object) List.of(v, v1, v2));
        Mockito.when(mocked.getTeamNameFromTeamId(Mockito.anyInt(), Mockito.anyInt())).thenReturn("SUPPORT_TEAM");
        ReflectionTestUtils.setField(v3, "manageDatabase", mocked);
        ReflectionTestUtils.setField(v3, "allowMultiApproval", true);
        Approval v4 = new Approval();
        v4.setApprovalId(1);
        v4.setApprovalType(ApprovalType.TEAM);
        v4.setRequiredApprover("ALICE");
        Approval v5 = new Approval();
        v5.setApprovalId(2);
        v5.setApprovalType(ApprovalType.ACL_TEAM_OWNER);
        v5.setRequiredApprover("Octopus");
        List<Object> v6 = v3.addApproval((List<Approval>) (Object) List.of(v4, v5), "Jackie", 10, 10);
        Assertions.assertEquals(2, v6.size());
    }

    @Test
    void addApprovalReproducedTest10() throws KlawBadRequestException {
        UserInfo v = new UserInfo();
        v.setRole("USER");
        v.setMailid("John@klaw.mailid");
        v.setUsername("John");
        v.setTenantId(101);
        v.setTeamId(10);
        UserInfo v1 = new UserInfo();
        v1.setRole("USER");
        v1.setMailid("James@klaw.mailid");
        v1.setUsername("James");
        v1.setTenantId(101);
        v1.setTeamId(11);
        UserInfo v2 = new UserInfo();
        v2.setRole("USER");
        v2.setMailid("Jackie@klaw.mailid");
        v2.setUsername("Jackie");
        v2.setTenantId(101);
        v2.setTeamId(13);
        ApprovalService v3 = new ApprovalService((Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null, (Map<String, List<Approval>>) (Object) null);
        ReflectionTestUtils.setField(v3, "topicApprovals", null);
        ReflectionTestUtils.setField(v3, "aclApprovals", null);
        ReflectionTestUtils.setField(v3, "schemaApprovals", null);
        ReflectionTestUtils.setField(v3, "connectApprovals", null);
        ReflectionTestUtils.setField(v3, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v3, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked.selectAllCachedUserInfo()).thenReturn((List<UserInfo>) (Object) List.of(v, v1, v2));
        ReflectionTestUtils.setField(v3, "manageDatabase", mocked);
        ReflectionTestUtils.setField(v3, "allowMultiApproval", true);
        Approval v4 = new Approval();
        v4.setApprovalId(1);
        v4.setApprovalType(ApprovalType.TOPIC_TEAM_OWNER);
        v4.setRequiredApprover("ALICE");
        Approval v5 = new Approval();
        v5.setApprovalId(2);
        v5.setApprovalType(ApprovalType.ACL_TEAM_OWNER);
        v5.setRequiredApprover("Octopus");
        List<Object> v6 = v3.addApproval((List<Approval>) (Object) List.of(v4, v5), "James", 10, 10);
        Assertions.assertEquals(2, v6.size());
    }
}

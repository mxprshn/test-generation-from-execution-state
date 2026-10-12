package io.aiven.klaw.service;

import org.junit.jupiter.api.Test;
import io.aiven.klaw.model.enums.ApprovalType;
import io.aiven.klaw.dao.Approval;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import io.aiven.klaw.service.ApprovalService;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mockito;
import io.aiven.klaw.service.MailUtils;
import io.aiven.klaw.service.CommonUtilsService;
import io.aiven.klaw.config.ManageDatabase;
import org.mockito.invocation.InvocationOnMock;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.error.KlawException;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class ApprovalServiceReproducedTests {

    @Test
    void getApprovalsForRequestReproducedTest() throws KlawException {
        ApprovalType v = ApprovalType.TOPIC_TEAM_OWNER;
        Approval v1 = new Approval();
        v1.setApprovalType(v);
        Approval v2 = new Approval();
        v2.setApprovalType(v);
        ApprovalType v3 = ApprovalType.ACL_TEAM_OWNER;
        Approval v4 = new Approval();
        v4.setApprovalType(v3);
        Approval v5 = new Approval();
        v5.setApprovalType(v3);
        Approval v6 = new Approval();
        v6.setApprovalType(v);
        Approval v7 = new Approval();
        v7.setApprovalType(ApprovalType.CONNECTOR_TEAM_OWNER);
        Map<Object, Object> v8 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)));
        Map<Object, Object> v9 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v4)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2, v5)));
        Map<Object, Object> v10 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)));
        Map<Object, Object> v11 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)));
        ApprovalService v12 = new ApprovalService((Map<String, List<Approval>>) (Object) v8, (Map<String, List<Approval>>) (Object) v9, (Map<String, List<Approval>>) (Object) v10, (Map<String, List<Approval>>) (Object) v11);
        ReflectionTestUtils.setField(v12, "topicApprovals", v8);
        ReflectionTestUtils.setField(v12, "aclApprovals", v9);
        ReflectionTestUtils.setField(v12, "schemaApprovals", v10);
        ReflectionTestUtils.setField(v12, "connectApprovals", v11);
        ReflectionTestUtils.setField(v12, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v12, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked.getTeamNameFromTeamId(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null).thenReturn(null);
        ReflectionTestUtils.setField(v12, "manageDatabase", mocked);
        ReflectionTestUtils.setField(v12, "allowMultiApproval", false);
        List<Object> v13 = v12.getApprovalsForRequest(RequestEntityType.ACL, RequestOperationType.CLAIM, null, 10, 10, 101);
        Assertions.assertEquals(2, v13.size());
    }

    @Test
    void getApprovalsForRequestReproducedTest3() throws KlawException {
        ApprovalType v = ApprovalType.TOPIC_TEAM_OWNER;
        Approval v1 = new Approval();
        v1.setApprovalType(v);
        Approval v2 = new Approval();
        v2.setApprovalType(v);
        ApprovalType v3 = ApprovalType.ACL_TEAM_OWNER;
        Approval v4 = new Approval();
        v4.setApprovalType(v3);
        Approval v5 = new Approval();
        v5.setApprovalType(v3);
        Approval v6 = new Approval();
        v6.setApprovalType(v);
        Approval v7 = new Approval();
        v7.setApprovalType(ApprovalType.CONNECTOR_TEAM_OWNER);
        Map<Object, Object> v8 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)));
        Map<Object, Object> v9 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v4)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2, v5)));
        Map<Object, Object> v10 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)));
        Map<Object, Object> v11 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)));
        ApprovalService v12 = new ApprovalService((Map<String, List<Approval>>) (Object) v8, (Map<String, List<Approval>>) (Object) v9, (Map<String, List<Approval>>) (Object) v10, (Map<String, List<Approval>>) (Object) v11);
        ReflectionTestUtils.setField(v12, "topicApprovals", v8);
        ReflectionTestUtils.setField(v12, "aclApprovals", v9);
        ReflectionTestUtils.setField(v12, "schemaApprovals", v10);
        ReflectionTestUtils.setField(v12, "connectApprovals", v11);
        ReflectionTestUtils.setField(v12, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v12, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ReflectionTestUtils.setField(v12, "manageDatabase", Mockito.mock(ManageDatabase.class));
        ReflectionTestUtils.setField(v12, "allowMultiApproval", false);
        List<Object> v13 = v12.getApprovalsForRequest(RequestEntityType.TOPIC, RequestOperationType.CREATE, null, 10, null, 101);
        Assertions.assertEquals(1, v13.size());
    }

    @Test
    void getApprovalsForRequestReproducedTest4() throws KlawException {
        ApprovalType v = ApprovalType.TOPIC_TEAM_OWNER;
        Approval v1 = new Approval();
        v1.setApprovalType(v);
        Approval v2 = new Approval();
        v2.setApprovalType(v);
        ApprovalType v3 = ApprovalType.ACL_TEAM_OWNER;
        Approval v4 = new Approval();
        v4.setApprovalType(v3);
        Approval v5 = new Approval();
        v5.setApprovalType(v3);
        Approval v6 = new Approval();
        v6.setApprovalType(v);
        Approval v7 = new Approval();
        v7.setApprovalType(ApprovalType.CONNECTOR_TEAM_OWNER);
        Map<Object, Object> v8 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)));
        Map<Object, Object> v9 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v4)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2, v5)));
        Map<Object, Object> v10 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)));
        Map<Object, Object> v11 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)));
        ApprovalService v12 = new ApprovalService((Map<String, List<Approval>>) (Object) v8, (Map<String, List<Approval>>) (Object) v9, (Map<String, List<Approval>>) (Object) v10, (Map<String, List<Approval>>) (Object) v11);
        ReflectionTestUtils.setField(v12, "topicApprovals", v8);
        ReflectionTestUtils.setField(v12, "aclApprovals", v9);
        ReflectionTestUtils.setField(v12, "schemaApprovals", v10);
        ReflectionTestUtils.setField(v12, "connectApprovals", v11);
        ReflectionTestUtils.setField(v12, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v12, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ReflectionTestUtils.setField(v12, "manageDatabase", Mockito.mock(ManageDatabase.class));
        ReflectionTestUtils.setField(v12, "allowMultiApproval", false);
        List<Object> v13 = v12.getApprovalsForRequest(RequestEntityType.SCHEMA, RequestOperationType.DELETE, null, 10, null, 101);
        Assertions.assertEquals(1, v13.size());
    }

    @Test
    void getApprovalsForRequestReproducedTest5() throws KlawException {
        ApprovalType v = ApprovalType.TOPIC_TEAM_OWNER;
        Approval v1 = new Approval();
        v1.setApprovalType(v);
        Approval v2 = new Approval();
        v2.setApprovalType(v);
        ApprovalType v3 = ApprovalType.ACL_TEAM_OWNER;
        Approval v4 = new Approval();
        v4.setApprovalType(v3);
        Approval v5 = new Approval();
        v5.setApprovalType(v3);
        Approval v6 = new Approval();
        v6.setApprovalType(v);
        Approval v7 = new Approval();
        v7.setApprovalType(ApprovalType.CONNECTOR_TEAM_OWNER);
        Map<Object, Object> v8 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)));
        Map<Object, Object> v9 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v4)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2, v5)));
        Map<Object, Object> v10 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)));
        Map<Object, Object> v11 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)));
        ApprovalService v12 = new ApprovalService((Map<String, List<Approval>>) (Object) v8, (Map<String, List<Approval>>) (Object) v9, (Map<String, List<Approval>>) (Object) v10, (Map<String, List<Approval>>) (Object) v11);
        ReflectionTestUtils.setField(v12, "topicApprovals", v8);
        ReflectionTestUtils.setField(v12, "aclApprovals", v9);
        ReflectionTestUtils.setField(v12, "schemaApprovals", v10);
        ReflectionTestUtils.setField(v12, "connectApprovals", v11);
        ReflectionTestUtils.setField(v12, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v12, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ReflectionTestUtils.setField(v12, "manageDatabase", Mockito.mock(ManageDatabase.class));
        ReflectionTestUtils.setField(v12, "allowMultiApproval", false);
        List<Object> v13 = v12.getApprovalsForRequest(RequestEntityType.SCHEMA, RequestOperationType.UPDATE, null, 10, null, 101);
        Assertions.assertEquals(1, v13.size());
    }

    @Test
    void getApprovalsForRequestReproducedTest6() throws KlawException {
        ApprovalType v = ApprovalType.TOPIC_TEAM_OWNER;
        Approval v1 = new Approval();
        v1.setApprovalType(v);
        Approval v2 = new Approval();
        v2.setApprovalType(v);
        ApprovalType v3 = ApprovalType.ACL_TEAM_OWNER;
        Approval v4 = new Approval();
        v4.setApprovalType(v3);
        Approval v5 = new Approval();
        v5.setApprovalType(v3);
        Approval v6 = new Approval();
        v6.setApprovalType(v);
        Approval v7 = new Approval();
        v7.setApprovalType(ApprovalType.CONNECTOR_TEAM_OWNER);
        Map<Object, Object> v8 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)));
        Map<Object, Object> v9 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v4)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2, v5)));
        Map<Object, Object> v10 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)));
        Map<Object, Object> v11 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)));
        ApprovalService v12 = new ApprovalService((Map<String, List<Approval>>) (Object) v8, (Map<String, List<Approval>>) (Object) v9, (Map<String, List<Approval>>) (Object) v10, (Map<String, List<Approval>>) (Object) v11);
        ReflectionTestUtils.setField(v12, "topicApprovals", v8);
        ReflectionTestUtils.setField(v12, "aclApprovals", v9);
        ReflectionTestUtils.setField(v12, "schemaApprovals", v10);
        ReflectionTestUtils.setField(v12, "connectApprovals", v11);
        ReflectionTestUtils.setField(v12, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v12, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked.getTeamNameFromTeamId(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null).thenReturn(null);
        ReflectionTestUtils.setField(v12, "manageDatabase", mocked);
        ReflectionTestUtils.setField(v12, "allowMultiApproval", false);
        List<Object> v13 = v12.getApprovalsForRequest(RequestEntityType.ACL, RequestOperationType.CLAIM, "PRD", 10, 10, 101);
        Assertions.assertEquals(2, v13.size());
    }

    @Test
    void getApprovalsForRequestReproducedTest7() throws KlawException {
        ApprovalType v = ApprovalType.TOPIC_TEAM_OWNER;
        Approval v1 = new Approval();
        v1.setApprovalType(v);
        Approval v2 = new Approval();
        v2.setApprovalType(v);
        ApprovalType v3 = ApprovalType.ACL_TEAM_OWNER;
        Approval v4 = new Approval();
        v4.setApprovalType(v3);
        Approval v5 = new Approval();
        v5.setApprovalType(v3);
        Approval v6 = new Approval();
        v6.setApprovalType(v);
        Approval v7 = new Approval();
        v7.setApprovalType(ApprovalType.CONNECTOR_TEAM_OWNER);
        Map<Object, Object> v8 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)));
        Map<Object, Object> v9 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v4)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2, v5)));
        Map<Object, Object> v10 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)));
        Map<Object, Object> v11 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)));
        ApprovalService v12 = new ApprovalService((Map<String, List<Approval>>) (Object) v8, (Map<String, List<Approval>>) (Object) v9, (Map<String, List<Approval>>) (Object) v10, (Map<String, List<Approval>>) (Object) v11);
        ReflectionTestUtils.setField(v12, "topicApprovals", v8);
        ReflectionTestUtils.setField(v12, "aclApprovals", v9);
        ReflectionTestUtils.setField(v12, "schemaApprovals", v10);
        ReflectionTestUtils.setField(v12, "connectApprovals", v11);
        ReflectionTestUtils.setField(v12, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v12, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ReflectionTestUtils.setField(v12, "manageDatabase", Mockito.mock(ManageDatabase.class));
        ReflectionTestUtils.setField(v12, "allowMultiApproval", false);
        List<Object> v13 = v12.getApprovalsForRequest(RequestEntityType.TOPIC, RequestOperationType.CREATE, "PRD", 10, null, 101);
        Assertions.assertEquals(1, v13.size());
    }

    @Test
    void getApprovalsForRequestReproducedTest8() throws KlawException {
        ApprovalType v = ApprovalType.TOPIC_TEAM_OWNER;
        Approval v1 = new Approval();
        v1.setApprovalType(v);
        Approval v2 = new Approval();
        v2.setApprovalType(v);
        ApprovalType v3 = ApprovalType.ACL_TEAM_OWNER;
        Approval v4 = new Approval();
        v4.setApprovalType(v3);
        Approval v5 = new Approval();
        v5.setApprovalType(v3);
        Approval v6 = new Approval();
        v6.setApprovalType(v);
        Approval v7 = new Approval();
        v7.setApprovalType(ApprovalType.CONNECTOR_TEAM_OWNER);
        Map<Object, Object> v8 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)));
        Map<Object, Object> v9 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v4)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2, v5)));
        Map<Object, Object> v10 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)));
        Map<Object, Object> v11 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)));
        ApprovalService v12 = new ApprovalService((Map<String, List<Approval>>) (Object) v8, (Map<String, List<Approval>>) (Object) v9, (Map<String, List<Approval>>) (Object) v10, (Map<String, List<Approval>>) (Object) v11);
        ReflectionTestUtils.setField(v12, "topicApprovals", v8);
        ReflectionTestUtils.setField(v12, "aclApprovals", v9);
        ReflectionTestUtils.setField(v12, "schemaApprovals", v10);
        ReflectionTestUtils.setField(v12, "connectApprovals", v11);
        ReflectionTestUtils.setField(v12, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v12, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ReflectionTestUtils.setField(v12, "manageDatabase", Mockito.mock(ManageDatabase.class));
        ReflectionTestUtils.setField(v12, "allowMultiApproval", false);
        List<Object> v13 = v12.getApprovalsForRequest(RequestEntityType.SCHEMA, RequestOperationType.DELETE, "PRD", 10, null, 101);
        Assertions.assertEquals(1, v13.size());
    }

    @Test
    void getApprovalsForRequestReproducedTest9() throws KlawException {
        ApprovalType v = ApprovalType.TOPIC_TEAM_OWNER;
        Approval v1 = new Approval();
        v1.setApprovalType(v);
        Approval v2 = new Approval();
        v2.setApprovalType(v);
        ApprovalType v3 = ApprovalType.ACL_TEAM_OWNER;
        Approval v4 = new Approval();
        v4.setApprovalType(v3);
        Approval v5 = new Approval();
        v5.setApprovalType(v3);
        Approval v6 = new Approval();
        v6.setApprovalType(v);
        Approval v7 = new Approval();
        v7.setApprovalType(ApprovalType.CONNECTOR_TEAM_OWNER);
        Map<Object, Object> v8 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v1)));
        Map<Object, Object> v9 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v4)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v2, v5)));
        Map<Object, Object> v10 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v6)));
        Map<Object, Object> v11 = Map.of("ALL", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "DELETE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CREATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "UPDATE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "PROMOTE", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)), "CLAIM", new ArrayList<Object>((Collection<Object>) (Object) List.of(v7)));
        ApprovalService v12 = new ApprovalService((Map<String, List<Approval>>) (Object) v8, (Map<String, List<Approval>>) (Object) v9, (Map<String, List<Approval>>) (Object) v10, (Map<String, List<Approval>>) (Object) v11);
        ReflectionTestUtils.setField(v12, "topicApprovals", v8);
        ReflectionTestUtils.setField(v12, "aclApprovals", v9);
        ReflectionTestUtils.setField(v12, "schemaApprovals", v10);
        ReflectionTestUtils.setField(v12, "connectApprovals", v11);
        ReflectionTestUtils.setField(v12, "mailService", Mockito.mock(MailUtils.class));
        ReflectionTestUtils.setField(v12, "commonUtilsService", Mockito.mock(CommonUtilsService.class));
        ReflectionTestUtils.setField(v12, "manageDatabase", Mockito.mock(ManageDatabase.class));
        ReflectionTestUtils.setField(v12, "allowMultiApproval", false);
        List<Object> v13 = v12.getApprovalsForRequest(RequestEntityType.CONNECTOR, RequestOperationType.UPDATE, "PRD", 10, null, 101);
        Assertions.assertEquals(1, v13.size());
    }
}

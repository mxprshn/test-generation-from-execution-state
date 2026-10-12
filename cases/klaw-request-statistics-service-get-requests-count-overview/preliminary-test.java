package io.aiven.klaw.service;

import org.junit.jupiter.api.Test;
import io.aiven.klaw.service.RequestStatisticsService;
import org.mockito.Mockito;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.enums.RequestMode;
import java.util.Map;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.test.util.ReflectionTestUtils;
import io.aiven.klaw.service.CommonUtilsService;
import org.springframework.security.core.userdetails.UserDetails;
import io.aiven.klaw.service.MailUtils;
import io.aiven.klaw.model.enums.RequestStatus;
import org.mockito.MockedStatic;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.response.RequestsCountOverview;
import org.junit.jupiter.api.Assertions;
import java.lang.*;

class RequestStatisticsServiceReproducedTests {

    @Test
    void getRequestsCountOverviewReproducedTest() {
        RequestStatisticsService v = new RequestStatisticsService();
        ManageDatabase mocked = Mockito.mock(ManageDatabase.class);
        HandleDbRequestsJdbc mocked1 = Mockito.mock(HandleDbRequestsJdbc.class);
        Mockito.when(mocked1.getTopicRequestsCounts(Mockito.anyInt(), Mockito.<RequestMode>any(), Mockito.anyInt(), Mockito.<String>any())).thenReturn((Map<String, Map<String, Long>>) (Object) Map.of("STATUS_COUNTS", Map.of("approved", 3L, "created", 5L), "OPERATION_TYPE_COUNTS", Map.of("Delete", 2L, "Create", 4L)));
        Mockito.when(mocked1.getAclRequestsCounts(Mockito.anyInt(), Mockito.<RequestMode>any(), Mockito.anyInt(), Mockito.<String>any())).thenReturn((Map<String, Map<String, Long>>) (Object) Map.of("STATUS_COUNTS", Map.of("approved", 3L, "created", 5L), "OPERATION_TYPE_COUNTS", Map.of("Delete", 2L, "Create", 4L)));
        Mockito.when(mocked1.getSchemaRequestsCounts(Mockito.anyInt(), Mockito.<RequestMode>any(), Mockito.anyInt(), Mockito.<String>any())).thenReturn((Map<String, Map<String, Long>>) (Object) Map.of("STATUS_COUNTS", Map.of("approved", 3L, "created", 5L), "OPERATION_TYPE_COUNTS", Map.of("Delete", 2L, "Create", 4L)));
        Mockito.when(mocked1.getConnectorRequestsCounts(Mockito.anyInt(), Mockito.<RequestMode>any(), Mockito.anyInt(), Mockito.<String>any())).thenReturn((Map<String, Map<String, Long>>) (Object) Map.of("STATUS_COUNTS", Map.of("approved", 3L, "created", 5L), "OPERATION_TYPE_COUNTS", Map.of("Delete", 2L, "Create", 4L)));
        Mockito.when(mocked.getHandleDbRequests()).thenReturn(mocked1);
        ReflectionTestUtils.setField(v, "manageDatabase", mocked);
        CommonUtilsService mocked2 = Mockito.mock(CommonUtilsService.class);
        Mockito.when(mocked2.getPrincipal()).thenReturn(Mockito.mock(UserDetails.class));
        Mockito.when(mocked2.getTeamId(Mockito.<String>any())).thenReturn(0);
        Mockito.when(mocked2.getTenantId(Mockito.<String>any())).thenReturn(0);
        ReflectionTestUtils.setField(v, "commonUtilsService", mocked2);
        MailUtils mocked3 = Mockito.mock(MailUtils.class);
        Mockito.when(mocked3.getUserName(Mockito.<Object>any())).thenReturn("kwusera");
        ReflectionTestUtils.setField(v, "mailService", mocked3);
        MockedStatic<RequestStatus> staticMockUtil = Mockito.mockStatic(RequestStatus.class, Mockito.CALLS_REAL_METHODS);
        RequestStatus v1 = RequestStatus.APPROVED;
        RequestStatus v2 = RequestStatus.CREATED;
        staticMockUtil.when(() -> RequestStatus.of(Mockito.<String>any())).thenReturn(v1).thenReturn(v2).thenReturn(v1).thenReturn(v2).thenReturn(v1).thenReturn(v2).thenReturn(v1).thenReturn(v2);
        MockedStatic<RequestOperationType> staticMockUtil1 = Mockito.mockStatic(RequestOperationType.class, Mockito.CALLS_REAL_METHODS);
        RequestOperationType v3 = RequestOperationType.DELETE;
        RequestOperationType v4 = RequestOperationType.CREATE;
        staticMockUtil1.when(() -> RequestOperationType.of(Mockito.<String>any())).thenReturn(v3).thenReturn(v4).thenReturn(v3).thenReturn(v4).thenReturn(v3).thenReturn(v4).thenReturn(v3).thenReturn(v4);
        RequestsCountOverview v5 = v.getRequestsCountOverview(RequestMode.MY_REQUESTS);
        Assertions.assertNotNull(v5);
        staticMockUtil.close();
        staticMockUtil1.close();
    }
}

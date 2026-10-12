package io.aiven.klaw.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Set;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import io.aiven.klaw.service.EnvsClustersTenantsControllerService;
import org.mockito.Mockito;
import io.aiven.klaw.service.MailUtils;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.test.util.ReflectionTestUtils;
import io.aiven.klaw.service.CommonUtilsService;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.config.ManageDatabase;
import java.util.List;
import io.aiven.klaw.dao.Env;
import java.util.ArrayList;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.service.ClusterApiService;
import io.aiven.klaw.service.UsersTeamsControllerService;
import io.aiven.klaw.service.DefaultDataService;
import io.aiven.klaw.model.ApiResponse;
import org.mockito.MockedStatic;
import io.aiven.klaw.error.KlawException;
import org.junit.jupiter.api.Assertions;
import java.lang.*;
import static org.springframework.security.core.userdetails.User.UserBuilder;
import static io.aiven.klaw.model.ApiResponse.ApiResponseBuilder;

class EnvsClustersTenantsControllerServiceReproducedTests {

    @Test
    void deleteClusterReproducedTest() throws KlawException {
        UserBuilder v = User.builder();
        v.password("WelcomeToKlaw321@");
        v.username("superadmin");
        v.authorities((Collection<GrantedAuthority>) (Object) Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        EnvsClustersTenantsControllerService v1 = new EnvsClustersTenantsControllerService();
        MailUtils mocked = Mockito.mock(MailUtils.class);
        Mockito.when(mocked.getUserName(Mockito.<Object>any())).thenReturn("superadmin");
        ReflectionTestUtils.setField(v1, "mailService", mocked);
        CommonUtilsService mocked1 = Mockito.mock(CommonUtilsService.class);
        Mockito.when(mocked1.getPrincipal()).thenReturn(v.build());
        Mockito.when(mocked1.isNotAuthorizedUser(Mockito.<Object>any(), Mockito.<PermissionType>any())).thenReturn(false);
        Mockito.when(mocked1.getTenantId(Mockito.<String>any())).thenReturn(101);
        ReflectionTestUtils.setField(v1, "commonUtilsService", mocked1);
        ManageDatabase mocked2 = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked2.getAllEnvList(Mockito.anyInt())).thenReturn((List<Env>) (Object) new ArrayList<Object>((Collection<Object>) (Object) List.of()));
        HandleDbRequestsJdbc mocked3 = Mockito.mock(HandleDbRequestsJdbc.class);
        Mockito.when(mocked3.deleteCluster(Mockito.anyInt(), Mockito.anyInt())).thenReturn("success");
        Mockito.when(mocked2.getHandleDbRequests()).thenReturn(mocked3);
        ReflectionTestUtils.setField(v1, "manageDatabase", mocked2);
        ReflectionTestUtils.setField(v1, "clusterApiService", Mockito.mock(ClusterApiService.class));
        ReflectionTestUtils.setField(v1, "usersTeamsControllerService", Mockito.mock(UsersTeamsControllerService.class));
        ReflectionTestUtils.setField(v1, "kwInstallationType", "onpremise");
        ReflectionTestUtils.setField(v1, "maxNumberOfTenantsCanBeCreated", 200);
        ReflectionTestUtils.setField(v1, "defaultDataService", Mockito.mock(DefaultDataService.class));
        ApiResponseBuilder v2 = ApiResponse.builder();
        v2.success(true);
        v2.message("success");
        MockedStatic<ApiResponse> staticMockUtil = Mockito.mockStatic(ApiResponse.class, Mockito.CALLS_REAL_METHODS);
        ApiResponse v3 = v2.build();
        staticMockUtil.when(() -> ApiResponse.ok(Mockito.<String>any())).thenReturn(v3);
        ApiResponse v4 = v1.deleteCluster("1");
        Assertions.assertNotNull(v4);
        Assertions.assertTrue(v4.isSuccess());
        Assertions.assertEquals("success", v4.getMessage());
        staticMockUtil.close();
    }
}

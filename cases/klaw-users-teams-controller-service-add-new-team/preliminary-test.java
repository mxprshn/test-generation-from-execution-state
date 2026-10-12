package io.aiven.klaw.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Set;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import io.aiven.klaw.service.UsersTeamsControllerService;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mockito;
import io.aiven.klaw.service.MailUtils;
import org.mockito.invocation.InvocationOnMock;
import io.aiven.klaw.service.CommonUtilsService;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.config.ManageDatabase;
import java.util.List;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.dao.Team;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import io.aiven.klaw.service.PasswordService;
import io.aiven.klaw.model.requests.TeamModel;
import io.aiven.klaw.model.ApiResponse;
import org.mockito.MockedStatic;
import io.aiven.klaw.error.KlawException;
import org.junit.jupiter.api.Assertions;
import java.lang.*;
import static org.springframework.security.core.userdetails.User.UserBuilder;
import static io.aiven.klaw.model.ApiResponse.ApiResponseBuilder;

class UsersTeamsControllerServiceReproducedTests {

    @Test
    void addNewTeamReproducedTest() throws KlawException {
        UserBuilder v = User.builder();
        v.password("WelcomeToKlaw321@");
        v.username("superadmin");
        v.authorities((Collection<GrantedAuthority>) (Object) Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsersTeamsControllerService v1 = new UsersTeamsControllerService();
        ReflectionTestUtils.setField(v1, "authenticationType", "db");
        ReflectionTestUtils.setField(v1, "ssoEnabled", false);
        ReflectionTestUtils.setField(v1, "encryptorSecretKey", "kw2021secretkey");
        ReflectionTestUtils.setField(v1, "kwInstallationType", "onpremise");
        MailUtils mocked = Mockito.mock(MailUtils.class);
        Mockito.when(mocked.getUserName(Mockito.<Object>any())).thenReturn("superadmin");
        ReflectionTestUtils.setField(v1, "mailService", mocked);
        CommonUtilsService mocked1 = Mockito.mock(CommonUtilsService.class);
        Mockito.when(mocked1.getPrincipal()).thenReturn(v.build());
        Mockito.when(mocked1.isNotAuthorizedUser(Mockito.<Object>any(), Mockito.<PermissionType>any())).thenReturn(false);
        Mockito.when(mocked1.getTenantId(Mockito.<String>any())).thenReturn(101);
        ReflectionTestUtils.setField(v1, "commonUtilsService", mocked1);
        ManageDatabase mocked2 = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked2.getTeamNamesForTenant(Mockito.anyInt())).thenReturn((List<String>) (Object) List.of("INFRATEAM", "STAGINGTEAM"));
        HandleDbRequestsJdbc mocked3 = Mockito.mock(HandleDbRequestsJdbc.class);
        Mockito.when(mocked3.addNewTeam(Mockito.<Team>any())).thenReturn("success");
        Mockito.when(mocked2.getHandleDbRequests()).thenReturn(mocked3);
        ReflectionTestUtils.setField(v1, "manageDatabase", mocked2);
        ReflectionTestUtils.setField(v1, "inMemoryUserDetailsManager", Mockito.mock(InMemoryUserDetailsManager.class));
        ReflectionTestUtils.setField(v1, "passwordService", Mockito.mock(PasswordService.class));
        TeamModel v2 = new TeamModel();
        v2.setTeamname("Octopus");
        v2.setTeamphone("0031003423432");
        v2.setTenantId(101);
        v2.setTeammail("Octopus@Octopus.com");
        v2.setContactperson("Contact Person");
        ApiResponseBuilder v3 = ApiResponse.builder();
        v3.success(true);
        v3.message("success");
        MockedStatic<ApiResponse> staticMockUtil = Mockito.mockStatic(ApiResponse.class, Mockito.CALLS_REAL_METHODS);
        ApiResponse v4 = v3.build();
        staticMockUtil.when(() -> ApiResponse.ok(Mockito.<String>any())).thenReturn(v4);
        ApiResponse v5 = v1.addNewTeam(v2, true);
        Assertions.assertNotNull(v5);
        Assertions.assertTrue(v5.isSuccess());
        Assertions.assertEquals("success", v5.getMessage());
        staticMockUtil.close();
    }

    @Test
    void addNewTeamReproducedTest1() throws KlawException {
        UserBuilder v = User.builder();
        v.password("WelcomeToKlaw321@");
        v.username("superadmin");
        v.authorities((Collection<GrantedAuthority>) (Object) Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsersTeamsControllerService v1 = new UsersTeamsControllerService();
        ReflectionTestUtils.setField(v1, "authenticationType", "db");
        ReflectionTestUtils.setField(v1, "ssoEnabled", false);
        ReflectionTestUtils.setField(v1, "encryptorSecretKey", "kw2021secretkey");
        ReflectionTestUtils.setField(v1, "kwInstallationType", "onpremise");
        MailUtils mocked = Mockito.mock(MailUtils.class);
        Mockito.when(mocked.getUserName(Mockito.<Object>any())).thenReturn("superadmin");
        ReflectionTestUtils.setField(v1, "mailService", mocked);
        CommonUtilsService mocked1 = Mockito.mock(CommonUtilsService.class);
        Mockito.when(mocked1.getPrincipal()).thenReturn(v.build());
        Mockito.when(mocked1.isNotAuthorizedUser(Mockito.<Object>any(), Mockito.<PermissionType>any())).thenReturn(false);
        Mockito.when(mocked1.getTenantId(Mockito.<String>any())).thenReturn(101);
        ReflectionTestUtils.setField(v1, "commonUtilsService", mocked1);
        ManageDatabase mocked2 = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked2.getTeamNamesForTenant(Mockito.anyInt())).thenReturn((List<String>) (Object) List.of("INFRATEAM", "STAGINGTEAM", "Octopus"));
        ReflectionTestUtils.setField(v1, "manageDatabase", mocked2);
        ReflectionTestUtils.setField(v1, "inMemoryUserDetailsManager", Mockito.mock(InMemoryUserDetailsManager.class));
        ReflectionTestUtils.setField(v1, "passwordService", Mockito.mock(PasswordService.class));
        TeamModel v2 = new TeamModel();
        v2.setTeamname("Octopus");
        v2.setTeamphone("0031003423432");
        v2.setTenantId(101);
        v2.setTeammail("Octopus@Octopus.com");
        v2.setContactperson("Contact Person");
        ApiResponseBuilder v3 = ApiResponse.builder();
        v3.success(false);
        v3.message("Failure. Team already exists.");
        MockedStatic<ApiResponse> staticMockUtil = Mockito.mockStatic(ApiResponse.class, Mockito.CALLS_REAL_METHODS);
        ApiResponse v4 = v3.build();
        staticMockUtil.when(() -> ApiResponse.notOk(Mockito.<String>any())).thenReturn(v4);
        ApiResponse v5 = v1.addNewTeam(v2, true);
        Assertions.assertNotNull(v5);
        Assertions.assertFalse(v5.isSuccess());
        Assertions.assertEquals("Failure. Team already exists.", v5.getMessage());
        staticMockUtil.close();
    }

    @Test
    void addNewTeamReproducedTest2() throws KlawException {
        UserBuilder v = User.builder();
        v.password("WelcomeToKlaw321@");
        v.username("superadmin");
        v.authorities((Collection<GrantedAuthority>) (Object) Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsersTeamsControllerService v1 = new UsersTeamsControllerService();
        ReflectionTestUtils.setField(v1, "authenticationType", "db");
        ReflectionTestUtils.setField(v1, "ssoEnabled", false);
        ReflectionTestUtils.setField(v1, "encryptorSecretKey", "kw2021secretkey");
        ReflectionTestUtils.setField(v1, "kwInstallationType", "onpremise");
        MailUtils mocked = Mockito.mock(MailUtils.class);
        Mockito.when(mocked.getUserName(Mockito.<Object>any())).thenReturn("superadmin");
        ReflectionTestUtils.setField(v1, "mailService", mocked);
        CommonUtilsService mocked1 = Mockito.mock(CommonUtilsService.class);
        Mockito.when(mocked1.getPrincipal()).thenReturn(v.build());
        Mockito.when(mocked1.isNotAuthorizedUser(Mockito.<Object>any(), Mockito.<PermissionType>any())).thenReturn(false);
        Mockito.when(mocked1.getTenantId(Mockito.<String>any())).thenReturn(101);
        ReflectionTestUtils.setField(v1, "commonUtilsService", mocked1);
        ManageDatabase mocked2 = Mockito.mock(ManageDatabase.class);
        Mockito.when(mocked2.getTeamNamesForTenant(Mockito.anyInt())).thenReturn((List<String>) (Object) List.of("INFRATEAM", "STAGINGTEAM", "Octopus"));
        HandleDbRequestsJdbc mocked3 = Mockito.mock(HandleDbRequestsJdbc.class);
        Mockito.when(mocked3.addNewTeam(Mockito.<Team>any())).thenReturn("success");
        Mockito.when(mocked2.getHandleDbRequests()).thenReturn(mocked3);
        ReflectionTestUtils.setField(v1, "manageDatabase", mocked2);
        ReflectionTestUtils.setField(v1, "inMemoryUserDetailsManager", Mockito.mock(InMemoryUserDetailsManager.class));
        ReflectionTestUtils.setField(v1, "passwordService", Mockito.mock(PasswordService.class));
        TeamModel v2 = new TeamModel();
        v2.setTeamname("Testteam");
        v2.setTeamphone("0031003423432");
        v2.setTenantId(101);
        v2.setTeammail("Testteam@Testteam.com");
        v2.setContactperson("Contact Person");
        ApiResponseBuilder v3 = ApiResponse.builder();
        v3.success(true);
        v3.message("success");
        MockedStatic<ApiResponse> staticMockUtil = Mockito.mockStatic(ApiResponse.class, Mockito.CALLS_REAL_METHODS);
        ApiResponse v4 = v3.build();
        staticMockUtil.when(() -> ApiResponse.ok(Mockito.<String>any())).thenReturn(v4);
        ApiResponse v5 = v1.addNewTeam(v2, true);
        Assertions.assertNotNull(v5);
        Assertions.assertTrue(v5.isSuccess());
        Assertions.assertEquals("success", v5.getMessage());
        staticMockUtil.close();
    }
}

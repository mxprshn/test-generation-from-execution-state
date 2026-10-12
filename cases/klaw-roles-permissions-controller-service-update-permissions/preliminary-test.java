package io.aiven.klaw.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Set;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.mockito.Mockito;
import io.aiven.klaw.service.CommonUtilsService;
import io.aiven.klaw.model.enums.PermissionType;
import org.mockito.invocation.InvocationOnMock;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import java.util.List;
import io.aiven.klaw.dao.KwRolesPermissions;
import io.aiven.klaw.service.MailUtils;
import io.aiven.klaw.service.RolesPermissionsControllerService;
import org.springframework.test.util.ReflectionTestUtils;
import io.aiven.klaw.model.requests.KwRolesPermissionsModel;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import org.junit.jupiter.api.Assertions;
import java.lang.*;
import static org.springframework.security.core.userdetails.User.UserBuilder;

class RolesPermissionsControllerServiceReproducedTests {

    @Test
    void updatePermissionsReproducedTest() throws KlawException {
        UserBuilder v = User.builder();
        v.password("WelcomeToKlaw321@");
        v.username("superadmin");
        v.authorities((Collection<GrantedAuthority>) (Object) Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        CommonUtilsService mocked = Mockito.mock(CommonUtilsService.class);
        Mockito.when(mocked.getPrincipal()).thenReturn(v.build());
        Mockito.when(mocked.isNotAuthorizedUser(Mockito.<Object>any(), Mockito.<PermissionType>any())).thenReturn(false);
        Mockito.when(mocked.getTenantId(Mockito.<String>any())).thenReturn(101);
        ManageDatabase mocked1 = Mockito.mock(ManageDatabase.class);
        HandleDbRequestsJdbc mocked2 = Mockito.mock(HandleDbRequestsJdbc.class);
        Mockito.when(mocked2.updatePermissions((List<KwRolesPermissions>) (Object) Mockito.<List<KwRolesPermissions>>any(), Mockito.<String>any())).thenReturn("success");
        Mockito.when(mocked1.getHandleDbRequests()).thenReturn(mocked2);
        MailUtils mocked3 = Mockito.mock(MailUtils.class);
        Mockito.when(mocked3.getUserName(Mockito.<Object>any())).thenReturn("superadmin");
        RolesPermissionsControllerService v1 = new RolesPermissionsControllerService(mocked, mocked1, mocked3);
        ReflectionTestUtils.setField(v1, "commonUtilsService", mocked);
        ReflectionTestUtils.setField(v1, "manageDatabase", mocked1);
        ReflectionTestUtils.setField(v1, "mailService", mocked3);
        ReflectionTestUtils.setField(v1, "kwInstallationType", "onpremise");
        KwRolesPermissionsModel v2 = new KwRolesPermissionsModel();
        v2.setRolePermission("TESTROLE-----ADD_EDIT_DELETE_CLUSTERS");
        v2.setPermissionEnabled("true");
        KwRolesPermissionsModel v3 = new KwRolesPermissionsModel();
        v3.setRolePermission("TESTROLE-----ADD_EDIT_DELETE_ENVS");
        v3.setPermissionEnabled("true");
        KwRolesPermissionsModel v4 = new KwRolesPermissionsModel();
        v4.setRolePermission("TESTROLE-----VIEW_TOPICS");
        v4.setPermissionEnabled("false");
        KwRolesPermissionsModel[] v5 = new KwRolesPermissionsModel[3];
        v5[0] = v2;
        v5[1] = v3;
        v5[2] = v4;
        ApiResponse v6 = v1.updatePermissions(v5);
        Assertions.assertNotNull(v6);
        Assertions.assertTrue(v6.isSuccess());
        Assertions.assertEquals("success", v6.getMessage());
    }
}

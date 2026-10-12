package io.aiven.klaw.helpers;

import org.junit.jupiter.api.Test;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import java.util.Map;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import io.aiven.klaw.helpers.UtilMethods;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.mockito.invocation.InvocationOnMock;
import java.lang.*;

class UtilMethodsReproducedTests {

    @Test
    void getUserNameReproducedTest() {
        String v = UtilMethods.getUserName(new DefaultOAuth2User((Collection<GrantedAuthority>) (Object) null, (Map<String, Object>) (Object) null, null), "preferred_username", "email");
        Assertions.assertEquals("test@example.com", v);
    }

    @Test
    void getUserNameReproducedTest1() {
        UserDetails mocked = Mockito.mock(UserDetails.class);
        Mockito.when(mocked.getUsername()).thenReturn("testUser");
        String v = UtilMethods.getUserName(mocked, "preferred_username", "email");
        Assertions.assertEquals("testUser", v);
    }

    @Test
    void getUserNameReproducedTest2() {
        String v = UtilMethods.getUserName("testUser", "preferred_username", "email");
        Assertions.assertEquals("testUser", v);
    }

    @Test
    void getUserNameReproducedTest3() {
        String v = UtilMethods.getUserName(new DefaultOAuth2User((Collection<GrantedAuthority>) (Object) null, (Map<String, Object>) (Object) null, null), "preferred_username", "email");
        Assertions.assertEquals("testUser", v);
    }
}

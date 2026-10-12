package io.aiven.klaw.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.web.PortMapperImpl;
import java.util.Map;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.DefaultRedirectStrategy;
import io.aiven.klaw.auth.KwAuthenticationSuccessHandler;
import org.mockito.Mockito;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.mockito.invocation.InvocationOnMock;
import org.junit.jupiter.api.Assertions;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import org.springframework.security.core.Authentication;
import java.lang.*;

class KwAuthenticationSuccessHandlerReproducedTests {

    @Test
    void getRedirectPageReproducedTest() {
        PortMapperImpl v = new PortMapperImpl();
        ReflectionTestUtils.setField(v, "httpsPortMappings", Map.of(80, 443, 8080, 8443));
        PortResolverImpl v1 = new PortResolverImpl();
        v1.setPortMapper(v);
        HttpSessionRequestCache v2 = new HttpSessionRequestCache();
        v2.setPortResolver(v1);
        v2.setCreateSessionAllowed(true);
        v2.setRequestMatcher(AnyRequestMatcher.INSTANCE);
        v2.setSessionAttrName("SPRING_SECURITY_SAVED_REQUEST");
        v2.setMatchingRequestParameterName("continue");
        DefaultRedirectStrategy v3 = new DefaultRedirectStrategy();
        v3.setStatusCode(HttpStatus.FOUND);
        KwAuthenticationSuccessHandler v4 = new KwAuthenticationSuccessHandler();
        ReflectionTestUtils.setField(v4, "quickStartEnabled", false);
        ReflectionTestUtils.setField(v4, "coralEnabled", false);
        ReflectionTestUtils.setField(v4, "requestCache", v2);
        ReflectionTestUtils.setField(v4, "defaultTargetUrl", "/");
        ReflectionTestUtils.setField(v4, "alwaysUseDefaultTargetUrl", false);
        ReflectionTestUtils.setField(v4, "useReferer", false);
        ReflectionTestUtils.setField(v4, "redirectStrategy", v3);
        HttpServletRequest mocked = Mockito.mock(HttpServletRequest.class);
        HttpSession mocked1 = Mockito.mock(HttpSession.class);
        Mockito.when(mocked1.getAttribute(Mockito.<String>any())).thenReturn(new DefaultSavedRequest(null, null));
        Mockito.when(mocked.getSession()).thenReturn(mocked1);
        String v5 = v4.getRedirectPage(mocked, null);
        Assertions.assertEquals("index", v5);
    }

    @Test
    void getRedirectPageReproducedTest1() {
        PortMapperImpl v = new PortMapperImpl();
        ReflectionTestUtils.setField(v, "httpsPortMappings", Map.of(80, 443, 8080, 8443));
        PortResolverImpl v1 = new PortResolverImpl();
        v1.setPortMapper(v);
        HttpSessionRequestCache v2 = new HttpSessionRequestCache();
        v2.setPortResolver(v1);
        v2.setCreateSessionAllowed(true);
        v2.setRequestMatcher(AnyRequestMatcher.INSTANCE);
        v2.setSessionAttrName("SPRING_SECURITY_SAVED_REQUEST");
        v2.setMatchingRequestParameterName("continue");
        DefaultRedirectStrategy v3 = new DefaultRedirectStrategy();
        v3.setStatusCode(HttpStatus.FOUND);
        KwAuthenticationSuccessHandler v4 = new KwAuthenticationSuccessHandler();
        ReflectionTestUtils.setField(v4, "quickStartEnabled", false);
        ReflectionTestUtils.setField(v4, "coralEnabled", false);
        ReflectionTestUtils.setField(v4, "requestCache", v2);
        ReflectionTestUtils.setField(v4, "defaultTargetUrl", "/");
        ReflectionTestUtils.setField(v4, "alwaysUseDefaultTargetUrl", false);
        ReflectionTestUtils.setField(v4, "useReferer", false);
        ReflectionTestUtils.setField(v4, "redirectStrategy", v3);
        HttpServletRequest mocked = Mockito.mock(HttpServletRequest.class);
        HttpSession mocked1 = Mockito.mock(HttpSession.class);
        Mockito.when(mocked1.getAttribute(Mockito.<String>any())).thenReturn(new DefaultSavedRequest(null, null));
        Mockito.when(mocked.getSession()).thenReturn(mocked1);
        String v5 = v4.getRedirectPage(mocked, null);
        Assertions.assertEquals("/browseTopics?topicName=testtopic", v5);
    }

    @Test
    void getRedirectPageReproducedTest2() {
        PortMapperImpl v = new PortMapperImpl();
        ReflectionTestUtils.setField(v, "httpsPortMappings", Map.of(80, 443, 8080, 8443));
        PortResolverImpl v1 = new PortResolverImpl();
        v1.setPortMapper(v);
        HttpSessionRequestCache v2 = new HttpSessionRequestCache();
        v2.setPortResolver(v1);
        v2.setCreateSessionAllowed(true);
        v2.setRequestMatcher(AnyRequestMatcher.INSTANCE);
        v2.setSessionAttrName("SPRING_SECURITY_SAVED_REQUEST");
        v2.setMatchingRequestParameterName("continue");
        DefaultRedirectStrategy v3 = new DefaultRedirectStrategy();
        v3.setStatusCode(HttpStatus.FOUND);
        KwAuthenticationSuccessHandler v4 = new KwAuthenticationSuccessHandler();
        ReflectionTestUtils.setField(v4, "quickStartEnabled", false);
        ReflectionTestUtils.setField(v4, "coralEnabled", true);
        ReflectionTestUtils.setField(v4, "handleDbRequests", Mockito.mock(HandleDbRequestsJdbc.class));
        ReflectionTestUtils.setField(v4, "requestCache", v2);
        ReflectionTestUtils.setField(v4, "defaultTargetUrl", "/");
        ReflectionTestUtils.setField(v4, "alwaysUseDefaultTargetUrl", false);
        ReflectionTestUtils.setField(v4, "useReferer", false);
        ReflectionTestUtils.setField(v4, "redirectStrategy", v3);
        HttpServletRequest mocked = Mockito.mock(HttpServletRequest.class);
        HttpSession mocked1 = Mockito.mock(HttpSession.class);
        Mockito.when(mocked1.getAttribute(Mockito.<String>any())).thenReturn(new DefaultSavedRequest(null, null));
        Mockito.when(mocked.getSession()).thenReturn(mocked1);
        Authentication mocked2 = Mockito.mock(Authentication.class);
        Mockito.when(mocked2.getPrincipal()).thenReturn("userPrincipal");
        String v5 = v4.getRedirectPage(mocked, mocked2);
        Assertions.assertEquals("/index", v5);
    }

    @Test
    void getRedirectPageReproducedTest3() {
        PortMapperImpl v = new PortMapperImpl();
        ReflectionTestUtils.setField(v, "httpsPortMappings", Map.of(80, 443, 8080, 8443));
        PortResolverImpl v1 = new PortResolverImpl();
        v1.setPortMapper(v);
        HttpSessionRequestCache v2 = new HttpSessionRequestCache();
        v2.setPortResolver(v1);
        v2.setCreateSessionAllowed(true);
        v2.setRequestMatcher(AnyRequestMatcher.INSTANCE);
        v2.setSessionAttrName("SPRING_SECURITY_SAVED_REQUEST");
        v2.setMatchingRequestParameterName("continue");
        DefaultRedirectStrategy v3 = new DefaultRedirectStrategy();
        v3.setStatusCode(HttpStatus.FOUND);
        KwAuthenticationSuccessHandler v4 = new KwAuthenticationSuccessHandler();
        ReflectionTestUtils.setField(v4, "quickStartEnabled", false);
        ReflectionTestUtils.setField(v4, "coralEnabled", false);
        ReflectionTestUtils.setField(v4, "requestCache", v2);
        ReflectionTestUtils.setField(v4, "defaultTargetUrl", "/");
        ReflectionTestUtils.setField(v4, "alwaysUseDefaultTargetUrl", false);
        ReflectionTestUtils.setField(v4, "useReferer", false);
        ReflectionTestUtils.setField(v4, "redirectStrategy", v3);
        HttpServletRequest mocked = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mocked.getSession()).thenReturn(Mockito.mock(HttpSession.class));
        String v5 = v4.getRedirectPage(mocked, null);
        Assertions.assertEquals("index", v5);
    }

    @Test
    void getRedirectPageReproducedTest4() {
        PortMapperImpl v = new PortMapperImpl();
        ReflectionTestUtils.setField(v, "httpsPortMappings", Map.of(80, 443, 8080, 8443));
        PortResolverImpl v1 = new PortResolverImpl();
        v1.setPortMapper(v);
        HttpSessionRequestCache v2 = new HttpSessionRequestCache();
        v2.setPortResolver(v1);
        v2.setCreateSessionAllowed(true);
        v2.setRequestMatcher(AnyRequestMatcher.INSTANCE);
        v2.setSessionAttrName("SPRING_SECURITY_SAVED_REQUEST");
        v2.setMatchingRequestParameterName("continue");
        DefaultRedirectStrategy v3 = new DefaultRedirectStrategy();
        v3.setStatusCode(HttpStatus.FOUND);
        KwAuthenticationSuccessHandler v4 = new KwAuthenticationSuccessHandler();
        ReflectionTestUtils.setField(v4, "quickStartEnabled", false);
        ReflectionTestUtils.setField(v4, "coralEnabled", false);
        ReflectionTestUtils.setField(v4, "requestCache", v2);
        ReflectionTestUtils.setField(v4, "defaultTargetUrl", "/");
        ReflectionTestUtils.setField(v4, "alwaysUseDefaultTargetUrl", false);
        ReflectionTestUtils.setField(v4, "useReferer", false);
        ReflectionTestUtils.setField(v4, "redirectStrategy", v3);
        HttpServletRequest mocked = Mockito.mock(HttpServletRequest.class);
        HttpSession mocked1 = Mockito.mock(HttpSession.class);
        Mockito.when(mocked1.getAttribute(Mockito.<String>any())).thenReturn(new DefaultSavedRequest(null, null));
        Mockito.when(mocked.getSession()).thenReturn(mocked1);
        String v5 = v4.getRedirectPage(mocked, null);
        Assertions.assertEquals("/", v5);
    }
}

package com.zodiakk.employeeservice.security.filter;

import com.zodiakk.employeeservice.common.constants.Headers;
import com.zodiakk.employeeservice.common.constants.SecurityConstants;
import com.zodiakk.employeeservice.common.exception.ErrorMessages;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalApiKeyFilter")
class InternalApiKeyFilterTest {

    private static final String VALID_KEY = "test-internal-api-key";

    private static final String INTERNAL_URI = "/internal/employees";

    @Mock
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private FilterChain filterChain;

    private InternalApiKeyFilter filter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new InternalApiKeyFilter(VALID_KEY, authenticationEntryPoint);
        request = new MockHttpServletRequest("POST", INTERNAL_URI);
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("when the internal key is valid")
    class WhenKeyIsValid {

        @Test
        @DisplayName("authenticates an internal-service principal with the internal authority")
        void shouldAuthenticateInternalRequest() throws Exception {
            request.addHeader(Headers.INTERNAL_KEY, VALID_KEY);

            filter.doFilter(request, response, filterChain);

            Authentication authentication = currentAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.isAuthenticated()).isTrue();
            assertThat(authentication.getPrincipal()).isEqualTo("internal-service");
            assertThat(authentication.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly(SecurityConstants.INTERNAL_SERVICE_AUTHORITY);

            assertThat(response.getStatus()).isEqualTo(200);
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(authenticationEntryPoint);
        }

        @Test
        @DisplayName("uses the X-Internal-Key header")
        void shouldUseInternalKeyHeaderConstant() {
            assertThat(Headers.INTERNAL_KEY).isEqualTo("X-Internal-Key");

            request.addHeader(Headers.INTERNAL_KEY, VALID_KEY);

            assertThat(request.getHeader("X-Internal-Key")).isEqualTo(VALID_KEY);
        }

        @Test
        @DisplayName("replaces a pre-existing authentication")
        void shouldReplaceExistingAuthentication() throws Exception {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            "previous-user",
                            null,
                            List.of(new SimpleGrantedAuthority(SecurityConstants.EMPLOYEE_AUTHORITY))
                    )
            );
            request.addHeader(Headers.INTERNAL_KEY, VALID_KEY);

            filter.doFilter(request, response, filterChain);

            assertThat(currentAuthentication().getPrincipal()).isEqualTo("internal-service");
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("when the internal key is missing or invalid")
    class WhenKeyIsInvalid {

        @Test
        @DisplayName("rejects a wrong key through the entry point and does not continue the chain")
        void shouldRejectWrongKey() throws Exception {
            request.addHeader(Headers.INTERNAL_KEY, "wrong-key");

            filter.doFilter(request, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain, never()).doFilter(any(), any());
            assertInvalidKeyRejection();
        }

        @Test
        @DisplayName("rejects a missing key through the entry point and does not continue the chain")
        void shouldRejectMissingKey() throws Exception {
            filter.doFilter(request, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain, never()).doFilter(any(), any());
            assertInvalidKeyRejection();
        }

        @Test
        @DisplayName("rejects a blank key")
        void shouldRejectBlankKey() throws Exception {
            request.addHeader(Headers.INTERNAL_KEY, "   ");

            filter.doFilter(request, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain, never()).doFilter(any(), any());
            assertInvalidKeyRejection();
        }

        @Test
        @DisplayName("is case sensitive")
        void shouldTreatKeyAsCaseSensitive() throws Exception {
            request.addHeader(Headers.INTERNAL_KEY, VALID_KEY.toUpperCase());

            filter.doFilter(request, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain, never()).doFilter(any(), any());
            assertInvalidKeyRejection();
        }

        @Test
        @DisplayName("rejects a key with surrounding whitespace")
        void shouldRejectKeyWithSurroundingWhitespace() throws Exception {
            request.addHeader(Headers.INTERNAL_KEY, " " + VALID_KEY + " ");

            filter.doFilter(request, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain, never()).doFilter(any(), any());
            assertInvalidKeyRejection();
        }

        @Test
        @DisplayName("clears a pre-existing authentication before rejecting")
        void shouldClearExistingAuthentication() throws Exception {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            "internal-service",
                            null,
                            List.of(new SimpleGrantedAuthority(SecurityConstants.INTERNAL_SERVICE_AUTHORITY))
                    )
            );

            filter.doFilter(request, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain, never()).doFilter(any(), any());
            assertInvalidKeyRejection();
        }
    }

    @Nested
    @DisplayName("when the configured key is unusable")
    class WhenConfiguredKeyIsUnusable {

        @Test
        @DisplayName("rejects every request when the configured key is blank")
        void shouldRejectWhenConfiguredKeyIsBlank() throws Exception {
            InternalApiKeyFilter filterWithoutKey =
                    new InternalApiKeyFilter("   ", authenticationEntryPoint);
            request.addHeader(Headers.INTERNAL_KEY, "   ");

            filterWithoutKey.doFilter(request, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain, never()).doFilter(any(), any());
            assertInvalidKeyRejection();
        }

        @Test
        @DisplayName("rejects every request when the configured key is null")
        void shouldRejectWhenConfiguredKeyIsNull() throws Exception {
            InternalApiKeyFilter filterWithoutKey =
                    new InternalApiKeyFilter(null, authenticationEntryPoint);
            request.addHeader(Headers.INTERNAL_KEY, VALID_KEY);

            filterWithoutKey.doFilter(request, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain, never()).doFilter(any(), any());
            assertInvalidKeyRejection();
        }
    }

    @Nested
    @DisplayName("when the request is not for an internal path")
    class WhenRequestIsNotInternal {

        @Test
        @DisplayName("passes through a non-internal path without authenticating")
        void shouldPassThroughNonInternalPath() throws Exception {
            MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/api/employees");
            apiRequest.addHeader(Headers.INTERNAL_KEY, VALID_KEY);

            filter.doFilter(apiRequest, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain).doFilter(apiRequest, response);
            verifyNoInteractions(authenticationEntryPoint);
        }

        @Test
        @DisplayName("does not match a path that only contains the internal prefix without a slash")
        void shouldNotMatchPathWithoutTrailingSlash() throws Exception {
            MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/internal");
            apiRequest.addHeader(Headers.INTERNAL_KEY, "wrong-key");

            filter.doFilter(apiRequest, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain).doFilter(apiRequest, response);
            verifyNoInteractions(authenticationEntryPoint);
        }

        @Test
        @DisplayName("passes through OPTIONS preflight requests even on internal paths")
        void shouldPassThroughOptionsRequest() throws Exception {
            MockHttpServletRequest optionsRequest =
                    new MockHttpServletRequest("OPTIONS", INTERNAL_URI);
            optionsRequest.addHeader(Headers.INTERNAL_KEY, "wrong-key");

            filter.doFilter(optionsRequest, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain).doFilter(optionsRequest, response);
            verifyNoInteractions(authenticationEntryPoint);
        }

        @Test
        @DisplayName("treats the HTTP method case-insensitively for OPTIONS")
        void shouldPassThroughLowercaseOptionsRequest() throws Exception {
            MockHttpServletRequest optionsRequest =
                    new MockHttpServletRequest("options", INTERNAL_URI);

            filter.doFilter(optionsRequest, response, filterChain);

            assertThat(currentAuthentication()).isNull();
            verify(filterChain).doFilter(optionsRequest, response);
            verifyNoInteractions(authenticationEntryPoint);
        }
    }

    private void assertInvalidKeyRejection() throws Exception {
        ArgumentCaptor<AuthenticationException> captor =
                ArgumentCaptor.forClass(AuthenticationException.class);

        verify(authenticationEntryPoint).commence(eq(request), eq(response), captor.capture());

        assertThat(captor.getValue())
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage(ErrorMessages.INVALID_INTERNAL_API_KEY);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}

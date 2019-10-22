package com.vladcarcu.sociallogin;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SocialTokenGranter extends AbstractTokenGranter {
    private static final String GRANT_TYPE = "social";

    private List<SocialLoginAdapter> adapters;

    protected SocialTokenGranter(List<SocialLoginAdapter> adapters, AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.adapters = adapters;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap(tokenRequest.getRequestParameters());
        String type = parameters.get("type");
        String token = parameters.get("token");
        parameters.remove("token");

        Optional<SocialLoginAuthenticationToken> userAuth = Optional.empty();
        for (SocialLoginAdapter loginAdapter : adapters) {
            if (loginAdapter.isApplicable(type)) {
                userAuth = loginAdapter.validateLogin(token);
            }
        }

        if (userAuth.isPresent() && userAuth.get().isAuthenticated()) {
            OAuth2Request storedOAuth2Request = this.getRequestFactory().createOAuth2Request(client, tokenRequest);
            return new OAuth2Authentication(storedOAuth2Request, userAuth.get());
        } else {
            throw new InvalidGrantException("Could not validate token for type: " + type);
        }
    }

    public List<SocialLoginAdapter> getAdapters() {
        return adapters;
    }
}

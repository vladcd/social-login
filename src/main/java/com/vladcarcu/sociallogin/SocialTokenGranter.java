package com.vladcarcu.sociallogin;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.ObjectUtils.isEmpty;

public class SocialTokenGranter extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "social";

    private List<SocialLoginAdapter> adapters;

    protected SocialTokenGranter(List<SocialLoginAdapter> adapters, AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.adapters = adapters;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
        var type = getType(parameters);
        var token = getToken(parameters);
        parameters.remove("token");

        return adapters.stream()
                .filter(adapter -> adapter.isApplicable(type))
                .map(adapter -> adapter.validateLogin(token))
                .filter(AbstractAuthenticationToken::isAuthenticated)
                .findAny()
                .map(authToken -> new OAuth2Authentication(getRequestFactory().createOAuth2Request(client, tokenRequest), authToken))
                .orElseThrow(() ->  new InvalidGrantException("Could not validate token for type: " + type));
    }

    private String getType(Map<String, String> parameters){
        var type = parameters.get("type");
        if(isEmpty(type)){
            throw new InvalidGrantException("Type can't be null.");
        }
        return type;
    }

    private String getToken(Map<String, String> parameters){
        var token = parameters.get("token");
        if(isEmpty(token)){
            throw new BadCredentialsException("Token can't be null.");
        }
        return token;
    }

}

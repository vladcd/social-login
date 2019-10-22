package com.vladcarcu.sociallogin;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class SocialLoginAuthenticationToken extends AbstractAuthenticationToken {
    private String username;

    public SocialLoginAuthenticationToken(String username) {
        super(null);
        this.username = username;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return username;
    }
}

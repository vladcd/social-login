package com.vladcarcu.sociallogin;

public interface SocialLoginAdapter {

    boolean isApplicable(String type);

    SocialLoginAuthenticationToken validateLogin(String token);
}

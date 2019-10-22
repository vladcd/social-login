package com.vladcarcu.sociallogin;

import java.util.Optional;

public interface SocialLoginAdapter {

    boolean isApplicable(String type);

    Optional<SocialLoginAuthenticationToken> validateLogin(String token);
}

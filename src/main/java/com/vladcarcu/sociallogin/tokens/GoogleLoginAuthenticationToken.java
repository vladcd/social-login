package com.vladcarcu.sociallogin.tokens;

import com.vladcarcu.sociallogin.SocialLoginAuthenticationToken;

public class GoogleLoginAuthenticationToken extends SocialLoginAuthenticationToken {

	public GoogleLoginAuthenticationToken(String username) {
		super(username);
	}

}

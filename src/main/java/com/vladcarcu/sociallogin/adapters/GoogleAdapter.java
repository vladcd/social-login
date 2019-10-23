package com.vladcarcu.sociallogin.adapters;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.vladcarcu.sociallogin.SocialLoginAdapter;
import com.vladcarcu.sociallogin.SocialLoginAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

@Component
public class GoogleAdapter implements SocialLoginAdapter {

    @Value("#{'${social.login.google.client-ids}'.split(',')}")
    private List<String> allowedApps;

    @Override
    public boolean isApplicable(String type) {
        return "google".equals(type);
    }

    @Override
    public Optional<SocialLoginAuthenticationToken> validateLogin(String token) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport.Builder().build(), JacksonFactory.getDefaultInstance())
                .setAudience(allowedApps)
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                SocialLoginAuthenticationToken authenticationToken = new SocialLoginAuthenticationToken(payload.getSubject());
                authenticationToken.setAuthenticated(true);
                return Optional.of(authenticationToken);
            } else {
                throw new BadCredentialsException("Invalid ID token.");
            }
        } catch (IOException | GeneralSecurityException ioe) {
            throw new BadCredentialsException("Invalid ID token.", ioe);
        }
    }

}

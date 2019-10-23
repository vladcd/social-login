package com.vladcarcu.sociallogin.adapters;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.vladcarcu.sociallogin.SocialLoginAdapter;
import com.vladcarcu.sociallogin.tokens.GoogleLoginAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
@ConditionalOnProperty("social.login.google.client-ids")
public class GoogleAdapter implements SocialLoginAdapter {

    private static final String TYPE_NAME = "google";

    @Value("#{'${social.login.google.client-ids}'.split(',')}")
    private List<String> allowedApps;

    @Override
    public boolean isApplicable(String type) {
        return TYPE_NAME.equals(type);
    }

    @Override
    public GoogleLoginAuthenticationToken validateLogin(String token) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport.Builder().build(), JacksonFactory.getDefaultInstance())
                .setAudience(allowedApps)
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                GoogleLoginAuthenticationToken authenticationToken = new GoogleLoginAuthenticationToken(payload.getSubject());
                authenticationToken.setAuthenticated(true);
                return authenticationToken;
            } else {
                throw new BadCredentialsException("Invalid Google token.");
            }
        } catch (IOException | GeneralSecurityException ioe) {
            throw new RuntimeException("An unforeseen exception appeared while validating the Google token.", ioe);
        } catch (Exception e) {
            throw new RuntimeException("An unforeseen exception appeared while validating the Google token. Possible causes:\n" +
                    "1. The sent Google token is in an invalid format and could not be processed. Please use a proper one.\n" +
                    "2. Another unforeseen exception occurred. Please contact the developer.", e);
        }
    }

}

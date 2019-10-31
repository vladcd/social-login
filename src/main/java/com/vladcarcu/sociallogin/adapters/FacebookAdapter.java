package com.vladcarcu.sociallogin.adapters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladcarcu.sociallogin.SocialLoginAdapter;
import com.vladcarcu.sociallogin.SocialLoginAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty(value = {"social.login.facebook.app-id", "social.login.facebook.app-secret"})
@Import(RestTemplate.class)
public class FacebookAdapter implements SocialLoginAdapter {

    private static final String TYPE_NAME = "facebook";
    private static final String ACCESS_TOKEN_ENDPOINT = "https://graph.facebook.com/oauth/access_token";
    private static final String DEBUG_TOKEN_ENDPOINT = "https://graph.facebook.com/debug_token";

    @Value("${social.login.facebook.app-id}")
    private String appId;

    @Value("${social.login.facebook.app-secret}")
    private String appSecret;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public boolean isApplicable(String type) {
        return TYPE_NAME.equals(type);
    }

    @Override
    public SocialLoginAuthenticationToken validateLogin(String token) {
        FacebookResponse tokenInfo = null;
        try {
            // first we gen an app access token. For that, we need to provide the app id and the app secret
            var accessTokenURI = new StringBuilder()
                    .append(ACCESS_TOKEN_ENDPOINT)
                    .append("?client_id=").append(appId)
                    .append("&client_secret=").append(appSecret)
                    .append("&grant_type=client_credentials")
                    .toString();
            var accessToken = restTemplate.getForEntity(accessTokenURI, FacebookAccessTokenResponse.class).getBody();
            // then we use the access token to get the needed info for the Facebook token
            var debugTokenURI = new StringBuilder()
                    .append(DEBUG_TOKEN_ENDPOINT)
                    .append("?input_token=").append(token)
                    .append("&access_token=").append(accessToken.getAccessToken())
                    .toString();
            tokenInfo = restTemplate.getForEntity(debugTokenURI, FacebookResponse.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("An unforeseen exception appeared while validating the Facebook token.", e);
        }
        if (tokenInfo.getData() != null && tokenInfo.getData().isValid() && "USER".equalsIgnoreCase(tokenInfo.getData().getType())) {
            var authToken = new SocialLoginAuthenticationToken(tokenInfo.getData().getUserId());
            authToken.setAuthenticated(true);
            return authToken;
        } else if (tokenInfo.getData() != null && tokenInfo.getData().getError() != null) {
            throw new BadCredentialsException(new StringBuilder()
                    .append("Code:").append(tokenInfo.getData().getError().getCode())
                    .append("; SubCode:").append(tokenInfo.getData().getError().getErrorSubcode())
                    .append("; Message:").append(tokenInfo.getData().getError().getMessage())
                    .toString());
        }
        throw new BadCredentialsException("Invalid Facebook token.");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FacebookAccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FacebookResponse {
        private FacebookReponseData data;

        public FacebookReponseData getData() {
            return data;
        }

        public void setData(FacebookReponseData data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FacebookReponseData {
        private String type;
        @JsonProperty("is_valid")
        private boolean isValid;
        @JsonProperty("user_id")
        private String userId;

        private FacebookErrorMessage error;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public FacebookErrorMessage getError() {
            return error;
        }

        public void setError(FacebookErrorMessage error) {
            this.error = error;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FacebookErrorMessage {
        private String message;
        private int code;
        @JsonProperty("error_subcode")
        private int errorSubcode;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public int getErrorSubcode() {
            return errorSubcode;
        }

        public void setErrorSubcode(int errorSubcode) {
            this.errorSubcode = errorSubcode;
        }
    }

}

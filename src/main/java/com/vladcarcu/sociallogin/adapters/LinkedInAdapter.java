package com.vladcarcu.sociallogin.adapters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladcarcu.sociallogin.SocialLoginAdapter;
import com.vladcarcu.sociallogin.SocialLoginAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Component
@ConditionalOnProperty(value = {"social.login.linkedin.client-id", "social.login.linkedin.client-secret", "social.login.linkedin.redirect-uri"})
@Import({RestTemplate.class, ObjectMapper.class})
public class LinkedInAdapter implements SocialLoginAdapter {

    private static final String TYPE_NAME = "linkedin";
    private static final String ACCESS_TOKEN_ENDPOINT = "https://www.linkedin.com/oauth/v2/accessToken?redirect_uri={redirect_uri}&client_id={client_id}&client_secret={client_secret}&grant_type={grant_type}&code={code}";
    private static final String PROFILE_ENDPOINT = "https://api.linkedin.com/v2/me";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${social.login.linkedin.client-id}")
    private String clientId;

    @Value("${social.login.linkedin.client-secret}")
    private String clientSecret;

    @Value("${social.login.linkedin.redirect-uri}")
    private String redirectUri;

    @Override
    public boolean isApplicable(String type) {
        return TYPE_NAME.equals(type);
    }

    @Override
    public SocialLoginAuthenticationToken validateLogin(String token) {
        var parameters = new HashMap<String, String>();
        parameters.put("grant_type", "authorization_code");
        parameters.put("code", token);
        parameters.put("redirect_uri", redirectUri);
        parameters.put("client_id", clientId);
        parameters.put("client_secret", clientSecret);

        ResponseEntity<String> accessTokenResponse = null;
        LinkedInAuthCode authCode = null;
        try {
            accessTokenResponse = restTemplate.getForEntity(ACCESS_TOKEN_ENDPOINT, String.class, parameters);
            if (accessTokenResponse.getStatusCode() == HttpStatus.OK) {
                authCode = objectMapper.readValue(accessTokenResponse.getBody(), LinkedInAuthCode.class);
            } else {
                // assume an error
                LinkedInAuthCodeError authCodeError = objectMapper.readValue(accessTokenResponse.getBody(), LinkedInAuthCodeError.class);
                throw new RuntimeException(new StringBuilder()
                        .append("The token could not be validated. Details: \n")
                        .append(authCodeError)
                        .toString());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(new StringBuilder()
                    .append("Could not deserialize: ")
                    .append(accessTokenResponse.getBody())
                    .toString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, new StringBuilder()
                .append("Bearer ")
                .append(authCode.getAccessToken())
                .toString());

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(PROFILE_ENDPOINT, HttpMethod.GET, new HttpEntity(headers), String.class, parameters);

            if (response.getStatusCode() == HttpStatus.OK) {
                // validation ok, return authentication token
                LinkedInProfile profile = objectMapper.readValue(response.getBody(), LinkedInProfile.class);
                SocialLoginAuthenticationToken authenticationToken = new SocialLoginAuthenticationToken(profile.getId());
                authenticationToken.setAuthenticated(true);
                return authenticationToken;
            } else if (response.getStatusCodeValue() >= HttpStatus.BAD_REQUEST.value()) {
                // either a client or a server error
                LinkedInErrorMessage error = objectMapper.readValue(response.getBody(), LinkedInErrorMessage.class);
                throw new RuntimeException(new StringBuilder()
                        .append("An error has occurred. Details: \n")
                        .append(error)
                        .toString());
            }
            throw new RuntimeException(new StringBuilder()
                    .append("Unexpected response. Details: \n")
                    .append(response.getBody())
                    .toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(new StringBuilder()
                    .append("Could not deserialize: ")
                    .append(response.getBody())
                    .toString(), e);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException(new StringBuilder()
                    .append("An error has occurred. Details: ")
                    .append("\nStatus: ").append(e.getRawStatusCode())
                    .append("\nMessage: ").append(e.getLocalizedMessage())
                    .toString());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class LinkedInAuthCode {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private long exipresIn;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public long getExipresIn() {
            return exipresIn;
        }

        public void setExipresIn(long exipresIn) {
            this.exipresIn = exipresIn;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class LinkedInAuthCodeError {
        private String error;

        @JsonProperty("error_description")
        private String errorDescription;

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getErrorDescription() {
            return errorDescription;
        }

        public void setErrorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("Error: ").append(error)
                    .append("\nDescription: ").append(errorDescription)
                    .toString();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class LinkedInProfile {
        private String id;
        private String localizedLastName;
        private String localizedFirstName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLocalizedLastName() {
            return localizedLastName;
        }

        public void setLocalizedLastName(String localizedLastName) {
            this.localizedLastName = localizedLastName;
        }

        public String getLocalizedFirstName() {
            return localizedFirstName;
        }

        public void setLocalizedFirstName(String localizedFirstName) {
            this.localizedFirstName = localizedFirstName;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class LinkedInErrorMessage {
        private String message;
        private int serviceErrorCode;
        private int status;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getServiceErrorCode() {
            return serviceErrorCode;
        }

        public void setServiceErrorCode(int serviceErrorCode) {
            this.serviceErrorCode = serviceErrorCode;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("Status: ")
                    .append(status)
                    .append("\nService error code: ")
                    .append(serviceErrorCode)
                    .append("\nMessage: ")
                    .append(message)
                    .toString();
        }
    }
}

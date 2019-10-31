package com.vladcarcu.sociallogin;

import com.vladcarcu.sociallogin.adapters.FacebookAdapter;
import com.vladcarcu.sociallogin.adapters.GoogleAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;

import java.util.Arrays;
import java.util.List;

@Configuration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@Import({GoogleAdapter.class, FacebookAdapter.class})
public class SocialLoginConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private List<SocialLoginAdapter> loginAdapters;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        var tokenGranter = endpoints.getTokenGranter();
        var compositeTokenGranter = new CompositeTokenGranter(Arrays.asList(
                tokenGranter,
                new SocialTokenGranter(loginAdapters, endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory())
        ));
        endpoints.tokenGranter(compositeTokenGranter);
    }

}

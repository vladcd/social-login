# social-login
## Description
The purpose of this library is to accommodate the following use case:
- you have a stateless backend API, built using Spring Boot
- you're using Spring OAuth2 for securing your endpoints
- you wish to allow clients to connect to your API using social login (Google, Facebook, Github etc)
- the authentication with the social OpenId service has already been performed through another API
- you wish to allow those already logged in clients to access your API

For now, the following adapters have been implemented: Facebook, Google, LinkedIn
You can add your own adapters by implementing <code>SocialLoginAdapter</code>

## How to use
- download the latest code
- run a Maven build, with the goals <code>clean install</code><br>
OR<br>
run a Maven build, with the goals <code>clean package</code>, and then upload to a local maven repository (e.g. Nexus)
- import your library in your sample project
- depending on the social login service you wish to use, add specific parameters in your application.properties<br>
<code>social.login.google.client-ids=your Google client id</code><br><br>
<code>social.login.facebook.app-id=your Facebook app id</code><br>
<code>social.login.facebook.app-secret=your Facebook app secret</code><br><br>
<code>social.login.linkedin.client-id=your LinkedIn client id</code><br>
<code>social.login.linkedin.client-secret=your LinkedIn client secret</code><br>
<code>social.login.linkedin.redirect-uri=one of your LinkedIn redirect URIs</code><br>

An example project is fully accessible and runnable here: https://gitlab.softvision.ro/vlad.carcu/social-login-example

## How it works
The library is built as a Spring Boot starter project.
It defines a bean of type TokenGranter, which is injected at the end of the already existing list of TokenGranters.
This SocialTokenGranter works for <code>grant_type=social</code>. In addition to that, it expects two other parameters:
- <code>type</code>: the external service to validate tokens from (e.g. google). Possible values: <code>google</code>, <code>facebook</code>
- <code>token</code>: an authorization token issued from that service

In order for the login to be successful, the token must be valid, issued by the same service, for the same client id you configured. 

## What's next
- add support for Github
- move to Java 9 modules (once there's no split package conflict between Spring Security and Spring Security OAuth2)
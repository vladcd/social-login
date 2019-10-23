# social-login
## Description
The purpose of this library is to accommodate the following use case:
- you have a stateless backend API, built using Spring Boot
- you're using Spring OAuth2 for securing your endpoints
- you wish to allow clients to connect to your API using social login (Google, Facebook, Github etc)
- the authentication with the social OpenId service has already been performed through another API
- you wish to allow those already logged in clients to access your API

For now, only a Google adapter has been implemented.

## How to use
- download the latest code
- run a Maven build, with the goals <code>clean install</code><br>
OR<br>
run a Maven build, with the goals <code>clean package</code>, and then upload to a local maven repository (e.g. Nexus)
- import your library in your sample project
- depending on the social login service you wish to use, add specific parameters in your application.properties
<code>
social.login.google.client-ids=your Google client id
</code>
<br>
An example project is fully accessible and runnable here: https://gitlab.softvision.ro/vlad.carcu/social-login-example

## How it works
The library is built as a Spring Boot starter project.
It defines a bean of type TokenGranter, which is injected at the end of the already existing list of TokenGranters.
This SocialTokenGranter works for <code>grant_type=social</code>. In addition to that, it expects two other parameters:
- <code>type</code>: the external service to validate tokens from (e.g. google). Possible values: <code>google</code>
- <code>token</code>: an authorization token issued from that service

In order for the login to be successful, the token must be valid, issued by the same service, for the same client id you configured. 

## What's next
- add support for Facebook 
- add support for Github
- add support for custom providers
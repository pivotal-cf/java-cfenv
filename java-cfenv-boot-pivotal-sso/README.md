# Spring Cloud SSO support

The Spring Cloud SSO support is for use with the Pivotal Single Sign-On Service on Cloud Foundry.
This library uses the `VCAP_SERVICES` environment data to set Spring Security 5.x properties, thus removing much of manual Spring Security 5 boilerplate OAuth2/OIDC client configuration.

### Spring Applications

Spring Applications can use this connector to auto-configure Spring Security's OAuth 2.0 client which enables the application with the Pivotal Single Sign-On Service.

This service provides the following properties to your spring application:

Property Name                                                |  Value
--------------                                               | ------
ssoServiceUrl                                                | authDomain (from VCAP_SERVICES)
spring.security.oauth2.client.registration.sso.client-id     | clientId (from VCAP_SERVICES)
spring.security.oauth2.client.registration.sso.client-secret | clientSecret (from VCAP_SERVICES)
spring.security.oauth2.client.registration.sso.client-name   | "sso"
spring.security.oauth2.client.registration.sso.redirect-uri  | "{baseUrl}/login/oauth2/code/{registrationId}"
spring.security.oauth2.client.provider.sso.issuer-uri        | issuer (from the .well_known endpoint) + "/oauth/token"
spring.security.oauth2.client.provider.sso.authorization-uri | authDomain + "/oauth/authorize"

Note: ssoServiceUrl refers to the service uri corresponding to a Pivotal Single Sign-On service plan. For more information on configuring a service plan please refer to https://docs.pivotal.io/p-identity/index.html#create-plan

### Required Manual Configuration

You will very likely need to configure 2 addition Spring Security 5 properties in order to satisfy the full configuration of and OAuth2/OIDC client:

```
# The grant type used in the call by Spring Security 5
spring.security.oauth2.client.registration.sso.authorization-grant-type

# The scopes requested when obtaining a token by Spring Security 5
spring.security.oauth2.client.registration.sso.scope: openid, email, profile, roles, user_attributes, todo.read, todo.write
```

Note: You may also use the environment variables rather than Java properties directly that will undergo the standard Spring Boot property transformation (e.g. `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_AUTHORIZATIONGRANTTYPE`).

Please see the Sample Apps below for more information.

### Sample Apps

Sample apps using this library are available at https://github.com/pivotal-cf/identity-sample-apps/tree/spring-boot-2.1

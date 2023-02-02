### JWT Authorization flow using Spring Security

##### A reference proof-of-concept that leverages Spring-security to implement JWT based Authentication and Authorization.
##### 🛠 upgraded to Spring Boot 3 and Spring Security 6 🛠

### Important classes
* [JwtAuthenticationFilter.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/filter/JwtAuthenticationFilter.java)
* [SecurityConfiguration.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/configuration/SecurityConfiguration.java)
* [ApiPathExclusion.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/constant/ApiPathExclusion.java)
* [CORSFilter.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/filter/CORSFilter.java)
* [JwtUtility.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/utility/JwtUtility.java)
* [JwtConfigurationProperties.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/configuration/properties/JwtConfigurationProperties.java)

Any request to an endpoint that is specified to be authenticated will be intercepted by the [JwtAuthenticationFilter](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/filter/JwtAuthenticationFilter.java) which is configured in the [SecurityConfiguration](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/configuration/SecurityConfiguration.java), Any APIs that need to be made public can be specified under the appropriate HTTP method in [ApiPathExclusion](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/constant/ApiPathExclusion.java).

Inside JwtAuthenticationFilter, the token from request header `Authorization` is extracted and decrypted, the corresponding user in database is fetched and is loaded onto the SecurityContextHolder object which signifies the current logged-in user for that HTTP request. The user details are added as part of custom claims in generated JWT. [refer [JwtUtility](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/utility/JwtUtility.java)]

The secret key which is to be used for JWT generation and decryption, along with Access-token and Refresh-token validity can be specified in `application.properties` file corresponding to [JwtConfigurationProperties.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/configuration/properties/JwtConfigurationProperties.java). Below is a sample `.properties` file snippet that can be used to configure the required parameters.

```
# JWT Configuration
com.behl.cerberus.jwt.secret-key=093617ebfa4b9af9700db274ac204ffa34195494d97b9c26c23ad561de817926
com.behl.cerberus.jwt.access-token.validity=30
com.behl.cerberus.jwt.refresh-token.validity=5
```

----

### Local Setup

Run the below given commands in the projects base directory to create an image and start a container from the given `Dockerfile` 

```
docker build -t jwt-auth-flow-spring-security .
```
```
docker container run -d -p 8080:8080 jwt-auth-flow-spring-security
```

---

### Demonstration screen recording

https://user-images.githubusercontent.com/69693621/177231299-5d927ea8-04f3-4cd4-935c-de88a8479d69.mov

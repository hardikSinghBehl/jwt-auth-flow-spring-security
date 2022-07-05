### Spring-security JWT Authorization flow : Reference POC
##### [Application Link](http://jwt-auth-flow.hardiksinghbehl.com/swagger-ui.html) | Java Backend application using Spring-security to implement JWT based Authentication and Authorization

### Important classes
* [JwtAuthenticationFilter.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/filter/JwtAuthenticationFilter.java)
* [SecurityConfiguration.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/configuration/SecurityConfiguration.java)
* [ApiPathExclusion.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/constant/ApiPathExclusion.java)
* [CORSFilter.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/filter/CORSFilter.java)
* [JwtUtility.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/utility/JwtUtility.java)
* [JwtConfigurationProperties.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/configuration/properties/JwtConfigurationProperties.java)

Any request to an endpoint that is specified to be authenticated will be intercepted by the [JwtAuthenticationFilter](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/filter/JwtAuthenticationFilter.java) which is configured in the [SecurityConfiguration](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/configuration/SecurityConfiguration.java), Any APIs that need to be made public can be specified under the appropriate HTTP method in [ApiPathExclusion](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/constant/ApiPathExclusion.java).

Inside JwtAuthenticationFilter, the token from request header `Authorization` is extracted and decrypted, the corresponding user in database is fetched and is loaded onto the SecurityContextHolder object which signifies the current logged-in user for that HTTP request. The user details are added as part of custom claims in generated JWT. [refer [JwtUtility](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/security/utility/JwtUtility.java)]

The secret key which is to be used for JWT generation and decryption, along with Access-token and Refresh-token validity can be specified in `application.properties` file corresponding to [JwtConfigurationProperties.java](https://github.com/hardikSinghBehl/jwt-auth-flow-spring-security/blob/master/src/main/java/com/behl/cerberus/configuration/properties/JwtConfigurationProperties.java)

```
# JWT Configuration
com.behl.cerberus.jwt.secret-key=093617ebfa4b9af9700db274ac204ffa34195494d97b9c26c23ad561de817926
com.behl.cerberus.jwt.access-token.validity=30
com.behl.cerberus.jwt.refresh-token.validity=5
```


----

### Local Setup

* Install Java 17 (recommended to use [SdkMan](https://sdkman.io))

```
sdk install java 17-open
```

* Install Maven (recommended to use [SdkMan](https://sdkman.io))

```
sdk install maven
```

* Clone the repo and run the below command in core

```
mvn clean install
```

* To start the application, run any of the below 2 commands

```
mvn spring-boot:run &
```

```
java -jar /target/jwt-auth-flow-spring-security-0.0.1-SNAPSHOT.jar &
```

* Access the swagger-ui

```
http://localhost:8080/swagger-ui.html
```

### Demonstration Screen-record
https://user-images.githubusercontent.com/69693621/177231299-5d927ea8-04f3-4cd4-935c-de88a8479d69.mov

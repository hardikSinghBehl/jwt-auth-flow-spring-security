FROM maven:3.8.4-openjdk-17 as backend
WORKDIR /backend
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean install
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:17
ARG DEPENDENCY=/backend/target/dependency
COPY --from=backend ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=backend ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=backend ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.behl.cerberus.JwtAuthFlowSpringSecurityApplication"]

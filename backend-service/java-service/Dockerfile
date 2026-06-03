FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml ./
COPY kai-lei-common/pom.xml ./kai-lei-common/pom.xml
COPY kai-lei-framework/pom.xml ./kai-lei-framework/pom.xml
COPY kai-lei-system/pom.xml ./kai-lei-system/pom.xml
COPY kai-lei-business/pom.xml ./kai-lei-business/pom.xml
COPY kai-lei-server/pom.xml ./kai-lei-server/pom.xml
RUN mvn -B -q -pl kai-lei-server -am dependency:go-offline

COPY kai-lei-common/src ./kai-lei-common/src
COPY kai-lei-framework/src ./kai-lei-framework/src
COPY kai-lei-system/src ./kai-lei-system/src
COPY kai-lei-business/src ./kai-lei-business/src
COPY kai-lei-server/src ./kai-lei-server/src
RUN mvn -B -q -pl kai-lei-server -am -DskipTests package

FROM eclipse-temurin:21-jre

ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=docker \
    SERVER_PORT=8082 \
    JAVA_OPTS=""

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app

COPY --from=builder /workspace/kai-lei-server/target/*.jar /app/app.jar

EXPOSE 8080

USER app

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

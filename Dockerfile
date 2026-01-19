FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 AS builder

WORKDIR /app
COPY . .

RUN sbt 'set test in assembly := {}' assembly

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/scala-3.5.1/*-assembly-*.jar app.jar

ENV JAVA_OPTS="-Djava.awt.headless=true"

ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "app.jar"]

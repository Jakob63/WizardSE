FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 AS builder

WORKDIR /app
COPY . .

RUN sbt 'set test in assembly := {}' assembly

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN apt-get update && apt-get install -y \
    xvfb \
    x11vnc \
    novnc \
    websockify \
    fluxbox \
    libgtk-3-0 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/scala-3.5.1/*-assembly-*.jar app.jar

COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

# Port für noVNC
EXPOSE 6080

ENTRYPOINT ["./entrypoint.sh"]

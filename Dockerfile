FROM maven:3.9.3-eclipse-temurin-17-alpine@sha256:1cbc71cb8e2f594338f4b4cbca897b9f9ed6183e361489f1f7db770d57efe839 AS build
COPY src /tmp/src/bot/src
COPY pom.xml /tmp/src/bot
RUN mvn -f /tmp/src/bot/pom.xml clean package -DskipTests

FROM eclipse-temurin:17.0.8_7-jre-alpine@sha256:221790c3159317e57c4106c25cac581381ad2f2e249fbeabdad663573ab19adc
RUN addgroup -S javauser && adduser -S javauser -D -G javauser
WORKDIR /app

LABEL maintainer="Brandon Fontany--Legall <brandon@fontany-legall.xyz>"
LABEL description="Discord Role Persistence is a verified Discord bot with the objective to save the roles of users even after a leave/join server"
LABEL website="https://discord-role-persistence.com"
LABEL github="https://github.com/brandonfl/discord-role-persistence"

RUN apk update && apk upgrade
RUN apk add --no-cache dumb-init bash

COPY --from=build /tmp/src/bot/target/bot.war /app
COPY docker/utils/wait-for-it.sh /app
COPY docker-custom-entrypoint.sh /app

RUN chmod +x /app/wait-for-it.sh
RUN chmod +x /app/docker-custom-entrypoint.sh
RUN chown -R javauser:javauser /app

EXPOSE 8080
USER javauser
ENTRYPOINT ["/app/docker-custom-entrypoint.sh"]
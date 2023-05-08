FROM maven:3.8.6-openjdk-11@sha256:37a94a4fe3b52627748d66c095d013a17d67478bc0594236eca55c8aef33ddaa AS build
COPY src /usr/src/bot/src
COPY pom.xml /usr/src/bot
RUN mvn -f /usr/src/bot/pom.xml clean package

FROM openjdk:11-slim-bullseye@sha256:d2b6af2093e823ba0cdee4bcd45a905afe3fa054d08bde55b1d850515da69a08
RUN useradd --system -m -d /app -U -s /bin/false javauser
WORKDIR /app

LABEL maintainer="Brandon Fontany--Legall <brandon@fontany-legall.xyz>"
LABEL description="Discord Role Persistence is a verified Discord bot with the objective to save the roles of users even after a leave/join server"
LABEL website="https://discord-role-persistence.com"
LABEL github="https://github.com/brandonfl/discord-role-persistence"

COPY --from=build /usr/src/bot/target/bot.war /app
COPY docker/utils/wait-for-it.sh /app
COPY docker-custom-entrypoint.sh /app

RUN chmod +x /app/wait-for-it.sh
RUN chmod +x /app/docker-custom-entrypoint.sh
RUN chown -R javauser:javauser /app

EXPOSE 8080
USER javauser
ENTRYPOINT ["/app/docker-custom-entrypoint.sh"]
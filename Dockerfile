FROM maven:openjdk AS build
COPY src /usr/src/bot/src
COPY pom.xml /usr/src/bot
RUN mvn -f /usr/src/bot/pom.xml clean package

FROM openjdk:11-slim

LABEL maintainer="Brandon Fontany--Legall <brandon@fontany-legall.xyz>"
LABEL description="Discord Role Persistence is a verified Discord bot with the objective to save the roles of users even after a leave/join server"
LABEL website="https://discord-role-persistence.com"
LABEL github="https://github.com/brandonfl/discord-role-persistence"

COPY --from=build /usr/src/bot/target/bot.war .

COPY docker/utils/wait-for-it.sh .
RUN chmod +x /wait-for-it.sh

ENTRYPOINT ["java","-jar","bot.war"]
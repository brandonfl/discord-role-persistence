FROM maven:openjdk AS build
COPY src /usr/src/bot/src
COPY pom.xml /usr/src/bot
RUN mvn -f /usr/src/bot/pom.xml clean package

FROM openjdk:11-slim
COPY --from=build /usr/src/bot/target/bot.war .

COPY docker/utils/wait-for-it.sh .
RUN chmod +x /wait-for-it.sh

ENTRYPOINT ["java","-jar","bot.war"]
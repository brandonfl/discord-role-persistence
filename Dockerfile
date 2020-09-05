FROM maven:alpine AS build
COPY src /usr/src/bot/src
COPY pom.xml /usr/src/bot
RUN mvn -f /usr/src/bot/pom.xml clean package

FROM openjdk:11-slim
COPY --from=build /usr/src/bot/target/bot.war /usr/local/lib/bot.war
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/bot.war"]
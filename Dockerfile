FROM maven:3.9.3-eclipse-temurin-17-alpine@sha256:1cbc71cb8e2f594338f4b4cbca897b9f9ed6183e361489f1f7db770d57efe839 AS build
COPY . /tmp/project
RUN mvn -f /tmp/project/pom.xml clean package -DskipTests

FROM ibm-semeru-runtimes:open-17-jre-jammy

LABEL maintainer="Brandon Fontany--Legall <brandon@fontany-legall.xyz>"
LABEL description="Discord Role Persistence is a verified Discord bot with the objective to save the roles of users even after a leave/join server"
LABEL website="https://discord-role-persistence.com"
LABEL github="https://github.com/brandonfl/discord-role-persistence"

RUN apt update && apt upgrade -y
RUN apt install -y dumb-init bash && apt-get clean
RUN apt auto-remove -y

WORKDIR /app
COPY --from=build /tmp/project/target/bot.war /app
COPY docker/utils/wait-for-it.sh /app
COPY docker-custom-entrypoint.sh /app

RUN groupadd -g 10001 drp && \
   useradd -u 10000 -g drp drp \
   && chown -R drp:drp /app

USER drp:drp

RUN chmod +x /app/wait-for-it.sh
RUN chmod +x /app/docker-custom-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/app/docker-custom-entrypoint.sh"]
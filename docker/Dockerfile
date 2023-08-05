FROM maven:3.9.3-eclipse-temurin-17-alpine@sha256:1cbc71cb8e2f594338f4b4cbca897b9f9ed6183e361489f1f7db770d57efe839
WORKDIR /var/opt/bot
ADD utils/wait-for-it.sh /var/opt/wait-for-it.sh
RUN chmod +x ../wait-for-it.sh
CMD ../wait-for-it.sh -t 0 mysql:3306 -- mvn spring-boot:run
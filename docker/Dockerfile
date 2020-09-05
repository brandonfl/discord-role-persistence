FROM openjdk:11-slim
FROM maven:alpine
WORKDIR /var/opt/bot
ADD utils/wait-for-it.sh /var/opt/wait-for-it.sh
RUN chmod +x ../wait-for-it.sh
CMD ../wait-for-it.sh -t 0 mysql:3306 -- mvn spring-boot:run
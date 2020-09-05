
<h1 align="center">
  <br>
Discord Java Docker Bot
  <br>
</h1>
<h4 align="center"> Template project to create Discord bots with Docker and Java

<p align="center">
  <a href="https://github.com/FontanyLegall-Brandon/discord-java-docker-bot/releases"><img src="https://img.shields.io/github/v/release/FontanyLegall-Brandon/discord-java-docker-bot" alt="release"></a>
  <a href="https://github.com/FontanyLegall-Brandon/discord-java-docker-bot/actions?query=workflow%3Abuild-docker-and-publish"><img src="https://github.com/FontanyLegall-Brandon/discord-java-docker-bot/workflows/build-docker-and-publish/badge.svg" alt="github-docker"></a>
  <a href="https://github.com/FontanyLegall-Brandon/discord-java-docker-bot/actions?query=workflow%3Asonar-gate"><img src="https://github.com/FontanyLegall-Brandon/discord-java-docker-bot/workflows/sonar-gate/badge.svg" alt="github-sonar"></a>
  <a href="https://sonarcloud.io/dashboard?id=FontanyLegall-Brandon_discord-java-docker-bot"><img src="https://sonarcloud.io/api/project_badges/measure?project=FontanyLegall-Brandon_discord-java-docker-bot&metric=alert_status" alt="sonar-gate"></a>
  <a href="https://github.com/FontanyLegall-Brandon/discord-java-docker-bot/blob/master/LICENSE"><img src="https://img.shields.io/github/license/FontanyLegall-Brandon/discord-java-docker-bot" alt="licence"></a>
</p>

<p align="center">
  <a href="#how-to-use">How to use</a> •
  <a href="#variables">Variables</a> •
  <a href="#licence">Licence</a> 
</p>

## How to use
#### Use with docker-compose
1. Change <a href="#variables">variables</a> in [application.properties](https://github.com/FontanyLegall-Brandon/discord-java-docker-bot/blob/master/src/main/resources/application.properties) file

2. Use command `docker-compose up`

#### Use with docker run
Command 
`docker run IMAGE -e BOT_TOKEN=TOKEN ...` 

with `-e` the <a href="#variables">variables</a>

#### Use with java
1. Compile `mvn clean package`
2. Run `java -jar target/bot.war` with <a href="#variables">variables</a>

#### Use with tomcat
1. Compile `mvn clean package` and get the war file in `target`folder
2. Config the config file of your bot `CATALINA-HOME/conf/Catalina/localhost/bot.xml` with <a href="#variables">variables</a>
3. Deploy the war `CATALINA-HOME/webapps/bot.war`

## Variables

| Key | Description | Default |
|--|--|--|
| LOG_FILE | Location of log file | ./log/bot.log |
| BOT_TOKEN | Token of the Discord bot | None - **required** |
| DB_USERNAME | The username used for your database | bot - **required** |
| DB_PASSWORD | The password used for your database | bot - **required** |
| DB_HOST | Where your database is hosted | bot-mysql - **required** |
| DB_PORT | The port of your database | 3306 |
| DB_NAME | The name of the table | bot - **required** |
| DB_TIMEZONE | The timezone used | UTC |

## Licence

Project under [MIT](https://github.com/FontanyLegall-Brandon/discord-java-docker-bot/blob/master/LICENSE) licence


<h1 align="center">
  <a href="https://discord-role-persistence.com"><img src="https://discord-role-persistence.com/wp-content/uploads/2020/09/cropped-discord-role-persistence-v2-1024.png" width="100"/></a>
  <br>
  <a href="https://discord-role-persistence.com">Discord Role Persistence</a>
  <br>
</h1>
<h4 align="center"> Discord Role Persistence is a verified Discord bot with the objective to save the roles of users even after a leave/join server

<p align="center">
  <a href="https://github.com/brandonfl/discord-role-persistence/releases"><img src="https://img.shields.io/github/v/release/brandonfl/discord-role-persistence" alt="release"></a>
  <a href="https://github.com/brandonfl/discord-role-persistence/actions?query=workflow%3Abuild-docker-and-publish"><img src="https://github.com/brandonfl/discord-role-persistence/workflows/build-docker-and-publish/badge.svg" alt="github-docker"></a>
  <a href="https://github.com/brandonfl/discord-role-persistence/actions?query=workflow%3Asonar-gate"><img src="https://github.com/brandonfl/discord-role-persistence/workflows/sonar-gate/badge.svg" alt="github-sonar"></a>
  <a href="https://sonarcloud.io/dashboard?id=brandonfl_discord-role-persistence"><img src="https://sonarcloud.io/api/project_badges/measure?project=brandonfl_discord-role-persistence&metric=alert_status" alt="sonar-gate"></a>
  <a href="https://github.com/brandonfl/discord-role-persistence/blob/master/LICENSE"><img src="https://img.shields.io/github/license/brandonfl/discord-role-persistence" alt="licence"></a>
</p>

<p align="center">
  <a href="#invite">Invite</a> •
  <a href="#how-to-use">How to use</a> •
  <a href="#variables">Variables</a> •
  <a href="#licence">Licence</a> 
</p>

## Invite
The invitation link for the bot is present into the website : [https://discord-role-persistence.com](https://discord-role-persistence.com)

## How to use
#### Use with docker-compose
1. Change <a href="#variables">variables</a> in [application.properties](https://github.com/brandonfl/discord-role-persistence/blob/master/src/main/resources/application.properties) file

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

Project under [MIT](https://github.com/brandonfl/discord-role-persistence/blob/master/LICENSE) licence

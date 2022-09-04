
<h1 align="center">
  <a href="https://discord-role-persistence.com"><img src="https://raw.githubusercontent.com/brandonfl/discord-role-persistence/assets/discord-role-persistence-logo.png" width="100"/></a>
  <br>
  <a href="https://discord-role-persistence.com">Discord Role Persistence</a>
  <br>
</h1>
<h4 align="center"> Discord Role Persistence is a verified Discord bot with the objective to save the roles of users even after a leave/join server

<p align="center">
  <a href="https://github.com/brandonfl/discord-role-persistence/releases"><img src="https://img.shields.io/github/v/release/brandonfl/discord-role-persistence" alt="release"></a>
  <a href="https://hub.docker.com/r/brandonfl/discord-role-persistence"><img src="https://img.shields.io/docker/v/brandonfl/discord-role-persistence/latest?label=Docker%20version" alt="Docker version"></a>
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
| DB_HOST | Define where the mysql server is. If not set, in-memory storage will be used. | None |
| DB_NAME | Define the database name | bot|
| DB_USERNAME | Define the username used to connect to the datasource | bot |
| DB_PASSWORD | Define the password used to connect to the datasource | bot |
| DB_TIMEZONE | Define the timezone used to store date into the datasource | UTC |
| DB_PATH | Define the path of the in-memory storage. If not set, data will be lost at bot reboot. | ./data/drp |

## Licence

Project under [MIT](https://github.com/brandonfl/discord-role-persistence/blob/master/LICENSE) licence

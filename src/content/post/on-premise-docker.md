---
title: On premise with Docker
date: 2021-11-14
---

1. **Create a new bot into the developer page**

[https://discord.com/developers]( https://discord.com/developers)

![exemple-client-id](/img/exemple-client-id.png)

![bot-token](/img/bot-token.png)

---

2. **Install Docker from Docker website**

[https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)

---

3. **Install the Discord bot with Docker command**

Here is a example command to insert into a new terminal

```shell
$ docker run -d --restart=always \
> --name discord-role-persistence \
> --env BOT_TOKEN=`TO_CHANGE` \
> -v `TO_CHANGE_PATH`:/data \
> brandonfl/discord-role-persistence
```

- `TO_CHANGE_TOKEN` : Put here the bot token that you get when creating the Discord bot in stage 1

- `TO_CHANGE_PATH` : Change where the data will be stored. Without this information, the data will be lost between 2 restart.

_Example : `docker run -d --restart="always" --name discord-role-persistence --env BOT_TOKEN=thisisatoken -v /srv/drp:/data/drp brandonfl/discord-role-persistence`_

Docker run documentation : [docs.docker.com](https://docs.docker.com/engine/reference/run/)

---

4. **Verify that bot is working**

Into the Docker Desktop, you can find the list of apps. You should find the discord-role-persistence app with controls allowing to start/restart/stop.

![docker-apps](/img/docker-apps.png)

You also can view logs by clicking the discord-role-persistence app

![docker-console](/img/docker-console.png)

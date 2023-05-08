#!/bin/bash

if [ ! -z "$DB_HOST" ]; then
    /app/wait-for-it.sh -t 0 $DB_HOST:3306
fi

dumb-init -- java $JAVA_OPTS -jar /app/bot.war
#! /bin/bash

PHPMYADMIN_PORT=8081

enter_new_port(){
read -p "Enter a new port:" val

echo "$val"|grep "^[0-9]*$"
val="$?"

if [[ ! $val == 0 ]]
  then
    enter_new_port
fi
}

check_port() {
if lsof -Pi :$PHPMYADMIN_PORT -sTCP:LISTEN -t >/dev/null ; then
    echo "Port ${PHPMYADMIN_PORT} is already in use !"
    PHPMYADMIN_PORT=$(enter_new_port)
    check_port
fi
}

check_port

docker run -d --rm --network discord-java-docker-bot_default --link bot-mysql -p $PHPMYADMIN_PORT:80 -e PMA_HOST=bot-mysql -e PMA_USER=bot -e PMA_PASSWORD=bot phpmyadmin/phpmyadmin
echo -e "\n Phpmyadmin started... http://localhost:${PHPMYADMIN_PORT}/"
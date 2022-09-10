# Discord-role-persistence website

## Dev

- Hugo shell 

```shell
docker run --rm -it \
  -v $(pwd):/src \
  klakegg/hugo:0.101.0-alpine \
  shell
```

- Server

```shell
docker-compose up
```

- Build

```shell
docker run --rm -it \
  -v $(pwd)/src:/src \
  klakegg/hugo:0.101.0
```

- Shell for test env

```shell
docker run --rm -it \
  -v $(pwd)/src:/src \
  klakegg/hugo:0.101.0 -b https://test.discord-role-persistence.com/
```

- For prod env

```shell
docker run --rm -it \
  -v $(pwd)/src:/src \
  klakegg/hugo:0.101.0 -b https://discord-role-persistence.com/
```
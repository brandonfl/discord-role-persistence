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
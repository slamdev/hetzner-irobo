# hetzner-irobo

Inventory manager for [Hetzner Robot](https://robot.your-server.de)

## Run locally

```shell
export HETZNER_IROBO_ROBOT_USERNAME="xxx"
export HETZNER_IROBO_ROBOT_PASSWORD="xxx"
export HETZNER_IROBO_ZABBIX_USERNAME="xxx"
export HETZNER_IROBO_ZABBIX_PASSWORD="xxx"
export ZABBIX_API_URL="xxx"
docker-compose --profile=complete up
```

## Release

```shell
TAG=x.x.x && git tag -a ${TAG} -m "make ${TAG} release" && git push --tags
```

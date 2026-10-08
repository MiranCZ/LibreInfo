.PHONY: up build down

up:
	docker compose --profile tools create --force-recreate
	docker compose up

build:
	docker compose --profile tools build
	docker compose build
	docker compose --profile tools create --force-recreate
	docker compose up

down:
	docker compose down

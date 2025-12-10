dev:
	docker compose up

build:
	docker compose build

rebuild:
	docker compose down -v
	docker compose up --build

clean:
	docker compose down -v

logs:
	docker compose logs -f backend

ps:
	docker compose ps

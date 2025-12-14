dev:
	docker compose up -d

build:
	docker compose build

rebuild:
	docker compose down -v
	docker compose up -d --build

clean:
	docker compose down -v

logs:
	docker compose logs -f backend

ps:
	docker compose ps

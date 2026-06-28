dev:
	docker compose up -d

build:
	docker compose build

test:
	docker compose run --rm tests mvn clean test jacoco:report
	powershell -NoProfile -ExecutionPolicy Bypass -File scripts/coverage-summary.ps1

rebuild:
	docker compose down -v
	docker compose up -d --build

clean:
	docker compose down -v

logs:
	docker compose logs -f backend

ps:
	docker compose ps

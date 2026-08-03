CONFIG_FILE := config/application-dev.yml
CONFIG_EXAMPLE := config/application-dev.example.yml

.PHONY: config dev build test rebuild clean logs ps

config:
	powershell -NoProfile -ExecutionPolicy Bypass -Command "if (-not (Test-Path '$(CONFIG_FILE)')) { Copy-Item '$(CONFIG_EXAMPLE)' '$(CONFIG_FILE)'; Write-Host 'Created $(CONFIG_FILE). Review it before starting.' } else { Write-Host 'Using existing $(CONFIG_FILE).' }"

dev: config
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

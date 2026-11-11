CONFIG_FILE := config/application.yml
CONFIG_EXAMPLE := config/application.example.yml

# One Compose project per worktree: the main checkout keeps "hubscore", worktree <name> uses "hubscore-<name>".
WORKTREE := $(notdir $(CURDIR))
COMPOSE_PROJECT_NAME ?= $(if $(filter hubscore,$(WORKTREE)),hubscore,hubscore-$(WORKTREE))
export COMPOSE_PROJECT_NAME

.PHONY: install config dev build test rebuild clean logs ps

# Worktree init (orca): local config files + image build, without starting containers.
install:
	powershell -NoProfile -ExecutionPolicy Bypass -File scripts/worktree-install.ps1
	docker compose build

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

# Worktree archive (orca): removes this project's containers, volumes and locally built images.
clean:
	docker compose down -v --remove-orphans --rmi local

logs:
	docker compose logs -f backend

ps:
	docker compose ps

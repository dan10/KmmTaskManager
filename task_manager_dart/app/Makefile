# Task Manager App - Development Commands

.PHONY: help l10n build run clean test

help: ## Show this help message
	@echo "Available commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

l10n: ## Generate localization files from ARB
	@echo "🌍 Generating localization files..."
	flutter gen-l10n
	@echo "✅ Localization files generated!"

build: ## Build the app (debug)
	@echo "🔨 Building debug app..."
	flutter build apk --debug

run: ## Run the app on connected device
	@echo "🚀 Running app..."
	flutter run

clean: ## Clean build artifacts
	@echo "🧹 Cleaning..."
	flutter clean
	flutter pub get

test: ## Run unit tests
	@echo "🧪 Running tests..."
	flutter test

analyze: ## Run static analysis
	@echo "🔍 Running analyzer..."
	flutter analyze

format: ## Format Dart code
	@echo "✨ Formatting code..."
	dart format lib/ test/

# Development workflow
dev-setup: clean l10n ## Setup development environment
	@echo "✅ Development environment ready!"

# Generate and run
dev-run: l10n run ## Generate l10n and run app

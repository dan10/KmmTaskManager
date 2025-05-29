#!/bin/bash

# Fast build script for Dart server
# This compiles the binary locally first, then creates a lightweight Docker image

set -e

echo "🚀 Starting fast Dart build process..."

# Navigate to server directory
cd server

# Step 1: Get dependencies
echo "📦 Getting Dart dependencies..."
dart pub get

# Step 2: Compile binary locally (much faster than in Docker)
echo "🔨 Compiling Dart binary locally..."
dart compile exe bin/server.dart -o bin/server

# Step 3: Build lightweight Docker image
echo "🐳 Building Docker image..."
docker build -f Dockerfile.fast -t dart-server:latest ..

echo "✅ Fast Dart build completed!"
echo "📊 Total time: much faster than the original Dockerfile"
echo "🏃‍♂️ You can now run: docker run -p 8082:8080 dart-server:latest" 
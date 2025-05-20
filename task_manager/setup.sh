#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print status messages
print_status() {
    echo -e "${BLUE}==>${NC} $1"
}

# Function to print success messages
print_success() {
    echo -e "${GREEN}==>${NC} $1"
}

# Function to print error messages
print_error() {
    echo -e "${RED}==>${NC} $1"
}

# Check if required tools are installed
check_requirements() {
    print_status "Checking requirements..."
    
    # Check Flutter
    if ! command -v flutter &> /dev/null; then
        print_error "Flutter is not installed. Please install Flutter first."
        exit 1
    fi
    
    # Check Dart
    if ! command -v dart &> /dev/null; then
        print_error "Dart is not installed. Please install Dart first."
        exit 1
    fi
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    print_success "All requirements are satisfied!"
}

# Install dependencies
install_dependencies() {
    print_status "Installing dependencies..."
    
    # Install app dependencies
    print_status "Installing app dependencies..."
    cd app
    flutter pub get
    if [ $? -ne 0 ]; then
        print_error "Failed to install app dependencies"
        exit 1
    fi
    cd ..
    
    # Install server dependencies
    print_status "Installing server dependencies..."
    cd server
    dart pub get
    if [ $? -ne 0 ]; then
        print_error "Failed to install server dependencies"
        exit 1
    fi
    cd ..
    
    # Install shared dependencies
    print_status "Installing shared dependencies..."
    cd shared
    dart pub get
    if [ $? -ne 0 ]; then
        print_error "Failed to install shared dependencies"
        exit 1
    fi
    cd ..
    
    print_success "All dependencies installed successfully!"
}

# Generate code
generate_code() {
    print_status "Generating code..."
    
    # Generate shared code
    print_status "Generating shared code..."
    cd shared
    dart run build_runner build --delete-conflicting-outputs
    if [ $? -ne 0 ]; then
        print_error "Failed to generate shared code"
        exit 1
    fi
    cd ..
    
    # Generate server code
    print_status "Generating server code..."
    cd server
    dart run build_runner build --delete-conflicting-outputs
    if [ $? -ne 0 ]; then
        print_error "Failed to generate server code"
        exit 1
    fi
    cd ..
    
    print_success "Code generation completed successfully!"
}

# Create environment file
create_env_file() {
    print_status "Creating environment file..."
    
    # Check if .env file exists
    if [ -f "server/.env" ]; then
        print_status ".env file already exists. Skipping..."
    else
        # Create .env file
        cat > server/.env << EOL
DATABASE_URL=postgres://postgres:postgres@localhost:5432/task_manager
JWT_SECRET=your-secret-key-here
PORT=8080
HOST=0.0.0.0
EOL
        print_success "Environment file created successfully!"
    fi
}

# Main execution
main() {
    print_status "Starting setup process..."
    
    # Check requirements
    check_requirements
    
    # Install dependencies
    install_dependencies
    
    # Generate code
    generate_code
    
    # Create environment file
    create_env_file
    
    print_success "Setup completed successfully!"
    print_status "You can now run the application using:"
    echo "docker-compose up"
}

# Run main function
main 
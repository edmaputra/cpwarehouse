#!/bin/bash
set -e


# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker/Podman is available
if command -v docker &> /dev/null; then
    CONTAINER_CMD="docker"
    COMPOSE_CMD="docker-compose"
    print_info "Using Docker"
elif command -v podman &> /dev/null; then
    CONTAINER_CMD="podman"
    COMPOSE_CMD="podman-compose"
    print_info "Using Podman"
    export DOCKER_HOST=unix://$XDG_RUNTIME_DIR/podman/podman.sock
else
    print_error "Neither Docker nor Podman is installed. Please install one of them."
    exit 1
fi

# Check if compose is available
if ! command -v $COMPOSE_CMD &> /dev/null; then
    print_error "$COMPOSE_CMD is not installed. Please install it."
    exit 1
fi

# Check if Maven is available
if ! command -v ./mvnw &> /dev/null; then
    print_error "Maven wrapper (mvnw) not found in current directory."
    exit 1
fi

# Step 1: Start MongoDB
print_info "Starting MongoDB..."
$COMPOSE_CMD up -d mongodb

# Wait for MongoDB to be ready
print_info "Waiting for MongoDB to be ready..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if $CONTAINER_CMD exec cpwarehouse-mongodb mongosh --eval "db.adminCommand('ping')" --quiet > /dev/null 2>&1; then
        print_info "MongoDB is ready!"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo -n "."
    sleep 1
done

echo ""

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    print_error "MongoDB failed to start within timeout period"
    exit 1
fi

# Step 2: Run the application directly from source
print_info "Starting Spring Boot application from source..."
echo ""
echo "================================"
echo "Application will start at: http://localhost:8080"
echo ""
echo "Press Ctrl+C to stop the application"
echo "================================"
echo ""

./mvnw spring-boot:run

# Cleanup function
cleanup() {
    echo ""
    print_info "Shutting down..."
    $COMPOSE_CMD down
    print_info "Cleanup complete"
}

trap cleanup EXIT

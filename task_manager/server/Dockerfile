# Use latest stable channel SDK.
FROM dart:stable AS build

# Set working directory
WORKDIR /app

# Copy shared code first
COPY shared ./shared

# Copy server code
COPY server ./server

# Set working directory to server
WORKDIR /app/server

# Resolve app dependencies.
RUN dart pub get

# AOT compile app.
RUN dart compile exe bin/server.dart -o bin/server

# Build minimal serving image from AOT-compiled `/server`
# and the pre-built AOT-runtime in the `/runtime/` directory of the base image.
FROM scratch
COPY --from=build /runtime/ /
COPY --from=build /app/server/bin/server /app/bin/

# Start server.
EXPOSE 8080
CMD ["/app/bin/server"]
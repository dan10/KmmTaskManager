FROM dart:stable AS build

WORKDIR /app
COPY . .
RUN dart pub get
RUN dart compile exe bin/server.dart -o bin/server

FROM debian:bullseye-slim

WORKDIR /app
COPY --from=build /app/bin/server /app/bin/server
COPY --from=build /app/.env /app/.env

EXPOSE 8080
CMD ["/app/bin/server"] 
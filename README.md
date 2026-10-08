# MongoDB connection test

## MongoDB Quick Start

For local development with Docker, start the repository's MongoDB service:

```sh
docker compose up -d mongodb
MONGODB_URI=mongodb://127.0.0.1:27017/gadisaath mvn spring-boot:run
```

You can also install MongoDB Community Server natively; Docker is optional. See [Local MongoDB setup](docs/local-mongodb-setup.md) for installation, Compass, troubleshooting, and Windows/Linux instructions.

The application reads its connection string from `MONGODB_URI`; `.env.example` shows the local development value. A backend running in GitHub Codespaces needs a MongoDB instance reachable from that Codespace. `127.0.0.1` in a Codespace refers to the Codespace environment, not a developer's Mac. Run the Compose service in the Codespace or configure a URI to another intentionally reachable development instance. Do not expose an unauthenticated local MongoDB server to a network or the internet.

Run the application once `MONGODB_URI` is set:

```sh
mvn spring-boot:run
```

Successful output:

```text
MongoDB connection successful
```
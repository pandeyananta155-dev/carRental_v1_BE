# GadiSaath MongoDB POC

This root Maven module is a minimal Spring Boot API demonstrating a live MongoDB health check and retrieval of manually managed configuration documents. It does not seed MongoDB on startup.

## Start local services

Prerequisites: Docker Engine/Desktop with the Compose plugin, Java 17, and Maven. From the directory containing `compose.yaml`, copy the sample environment file and start the services:

```sh
cp .env.example .env
docker compose up -d
docker compose ps --all
```

Wait for `mongodb` to become healthy and `mongodb-rs-init` to exit successfully before starting the backend. The local endpoints are MongoDB at `127.0.0.1:27017`, MinIO API at `127.0.0.1:9000`, MinIO console at <http://127.0.0.1:9001>, and Ollama API at `127.0.0.1:11434`. MinIO's sample console credentials are `minioadmin` / `minioadmin`; replace them in your untracked `.env` for local use beyond a throwaway machine.

Create MinIO buckets through its console when needed. Ollama does not download models automatically; after installing the Ollama CLI, pull a model with `ollama pull <model>` (for example, `ollama pull llama3.2`). MinIO objects and Ollama models persist in named Docker volumes.

Stop the services while preserving data with `docker compose down`. `docker compose down -v` permanently deletes the MongoDB, MinIO, and Ollama volumes.

## Run the backend

The API runs on the host. By default, the `local` profile connects to a native standalone MongoDB at `127.0.0.1:27017`, so with `mongod` running you can start the backend directly:

```sh
mvn spring-boot:run
```

To use the Compose replica set instead, override the URI in the shell that launches Maven:

```powershell
$env:MONGODB_URI = "mongodb://127.0.0.1:27017/gadisaath?replicaSet=rs0"
mvn spring-boot:run
```

On macOS or Linux, the equivalent is:

```sh
MONGODB_URI='mongodb://127.0.0.1:27017/gadisaath?replicaSet=rs0' mvn spring-boot:run
```

The default URI is configured in `src/main/resources/application-local.properties`; `MONGODB_URI` overrides it. Compose reads `.env` for service interpolation, but Maven and Spring Boot do not automatically load that file.

## Import the vehicle service configuration

In MongoDB Compass, connect to `mongodb://127.0.0.1:27017/gadisaath?replicaSet=rs0`, select database `gadisaath`, and open or create collection `app_config`. Choose **Add Data > Insert Document**, then paste the contents of [`src/main/resources/config/vehicle-service.json`](src/main/resources/config/vehicle-service.json) as one document. Insert it once manually; application startup does not import or overwrite it.

For uniqueness in this POC, open the collection's **Indexes** tab and create an ascending index on `typeCode` with **Unique** enabled. Create the index after removing any duplicate `typeCode` values. This prevents duplicate configuration keys.

## API

Interactive API documentation: <http://localhost:8080/swagger-ui>

Actuator health check: <http://localhost:8080/actuator/health>. This reports overall application health, including the configured MongoDB health indicator. The existing API-specific check remains available at `/api/health` and returns its MongoDB connectivity response.

Health check:

```sh
curl -i http://localhost:8080/api/health
```

When MongoDB responds to a real ping command, the response is HTTP 200:

```json
{"apiStatus":"UP","mongodbStatus":"CONNECTED"}
```

When MongoDB is unavailable, it returns HTTP 503 with `mongodbStatus` set to `UNAVAILABLE`.

Read the stored configuration by exact `typeCode`:

```sh
curl -i http://localhost:8080/api/config/VEHICLE_SERVICE
```

On success, the API returns the stored document, for example:

```json
{
	"typeCode": "VEHICLE_SERVICE",
	"name": "Vehicle Service",
	"enabled": true,
	"currency": "NPR",
	"defaultServiceRadiusKm": 15,
	"supportedVehicleTypes": ["CAR", "MOTORCYCLE"],
	"version": 1
}
```

An unknown `typeCode` returns HTTP 404. A failed MongoDB query returns HTTP 503 with an error and a troubleshooting message. To verify dynamic configuration, edit `defaultServiceRadiusKm` in the document in Compass, save it, and repeat the curl request; the response reflects the stored value without restarting the API.

## GitHub Codespaces

A Mac's `127.0.0.1` is not automatically accessible from a backend running in GitHub Codespaces. Run MongoDB in the Codespace environment or configure `MONGODB_URI` to an instance intentionally reachable from it; do not expose an unauthenticated local MongoDB server to a network or the internet. See the [local setup guide](docs/local-mongodb-setup.md) for Compose and platform-specific details.
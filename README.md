# GadiSaath MongoDB POC

This root Maven module is a minimal Spring Boot API demonstrating a live MongoDB health check and retrieval of manually managed configuration documents. It does not seed MongoDB on startup.

## Run the backend

Prerequisites: Java 17, Maven, and a reachable MongoDB Community Server. Start your local server using the [Local MongoDB setup](docs/local-mongodb-setup.md) instructions. For the default local server, set:

```sh
export MONGODB_URI=mongodb://127.0.0.1:27017/gadisaath
```

PowerShell:

```powershell
$env:MONGODB_URI = "mongodb://127.0.0.1:27017/gadisaath"
```

Build and run from the repository root:

```sh
mvn clean package
mvn spring-boot:run
```

`MONGODB_URI` defaults to `mongodb://127.0.0.1:27017/gadisaath` if not set. The application reads it through `spring.data.mongodb.uri`.

## Import the vehicle service configuration

In MongoDB Compass, connect to `mongodb://127.0.0.1:27017/gadisaath`, select database `gadisaath`, and open or create collection `app_config`. Choose **Add Data > Insert Document**, then paste the contents of [`src/main/resources/config/vehicle-service.json`](src/main/resources/config/vehicle-service.json) as one document. Insert it once manually; application startup does not import or overwrite it.

For uniqueness in this POC, open the collection's **Indexes** tab and create an ascending index on `typeCode` with **Unique** enabled. Create the index after removing any duplicate `typeCode` values. This prevents duplicate configuration keys.

## API

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
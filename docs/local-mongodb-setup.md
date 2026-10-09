# Local MongoDB Setup

This guide describes the default local development stack for GadiSaath: MongoDB as a single-node replica set, plus MinIO and Ollama. Each developer runs their own services; local data is not shared with the team and must never be committed to Git. A native MongoDB installation is also described as a standalone-only alternative and is not compatible with the default replica-set URI.

MongoDB authentication is disabled in these isolated local development examples. Keep the server bound to loopback (`127.0.0.1`) and never expose an unauthenticated MongoDB instance to a network or the internet.

## Prerequisites

- Git and this repository.
- Docker Engine/Desktop with the Compose plugin (`docker compose`).
- Java 17 and Maven to run this Spring Boot application.
- MongoDB Compass is optional.

## Docker Compose (recommended)

The Compose stack uses MongoDB `9.0.2` configured as a single-node replica set named `rs0`, MinIO for local object storage, and Ollama for local model serving. All ports bind to loopback only. MongoDB, MinIO, and Ollama data persist in named Docker volumes.

From the directory containing `compose.yaml`, create the local environment file and start all services:

```sh
cp .env.example .env
docker compose up -d
docker compose ps --all
docker compose logs mongodb mongodb-rs-init
```

Wait for `mongodb` to be healthy and `mongodb-rs-init` to finish successfully. Connect to MongoDB with `mongodb://127.0.0.1:27017/gadisaath?replicaSet=rs0`. Run the backend from the host with:

```sh
MONGODB_URI='mongodb://127.0.0.1:27017/gadisaath?replicaSet=rs0' mvn spring-boot:run
```

In PowerShell, set the variable for the current shell before running Maven:

```powershell
$env:MONGODB_URI = "mongodb://127.0.0.1:27017/gadisaath?replicaSet=rs0"
mvn spring-boot:run
```

Compose reads `.env` for service configuration. Spring Boot and Maven do not automatically load `.env`, so set `MONGODB_URI` in the host shell as shown. The local service endpoints are MongoDB at `127.0.0.1:27017`, MinIO API at `127.0.0.1:9000`, MinIO console at <http://127.0.0.1:9001>, and Ollama API at `127.0.0.1:11434`. The sample MinIO console credentials are `minioadmin` / `minioadmin`; change them in your untracked `.env` for local use beyond a throwaway machine.

Create MinIO buckets manually through the console. Ollama does not download models automatically; use `ollama pull <model>` with the Ollama CLI when needed. Model files and MinIO objects persist across container recreation.

Stop the stack without deleting its data:

```sh
docker compose down
```

To intentionally delete all local MongoDB, MinIO, and Ollama data, run `docker compose down -v`. This permanently removes the named volumes. `docker compose stop` and `docker compose start` can stop and restart existing containers without removing their data.

## Native MongoDB (standalone alternative)

The native instructions below start a standalone MongoDB server, not the Compose `rs0` replica set. The backend's default local profile uses `mongodb://127.0.0.1:27017/gadisaath` for this standalone server, so no `MONGODB_URI` override is needed. This only replaces MongoDB; MinIO and Ollama still require Compose if needed.

### macOS

Use the archive that matches the Mac processor: Intel (`x86_64`) or Apple Silicon (`arm64`). These instructions install the server under `~/mongodb/mongodb-9.0.2`, with data and logs outside the repository. The installation command is the same except for the architecture in the download URL and extracted directory name.

1. Download MongoDB Community Server 9.0.2 from the official archive and extract it. For Intel:

   ```sh
   mkdir -p ~/mongodb
   cd ~/mongodb
   curl -LO https://fastdl.mongodb.org/osx/mongodb-macos-x86_64-9.0.2.tgz
   tar -xzf mongodb-macos-x86_64-9.0.2.tgz
   mv mongodb-macos-x86_64-9.0.2 mongodb-9.0.2
   ```

   On Apple Silicon, use the arm64 archive and directory instead:

   ```sh
   mkdir -p ~/mongodb
   cd ~/mongodb
   curl -LO https://fastdl.mongodb.org/osx/mongodb-macos-arm64-9.0.2.tgz
   tar -xzf mongodb-macos-arm64-9.0.2.tgz
   mv mongodb-macos-arm64-9.0.2 mongodb-9.0.2
   ```

2. Start `mongod` in a terminal. The process stays in the foreground; do not add `--fork` (it is not supported in this setup).

   ```sh
   mkdir -p ~/mongodb/data/db ~/mongodb/data/log
   ~/mongodb/mongodb-9.0.2/bin/mongod \
     --dbpath ~/mongodb/data/db \
     --bind_ip 127.0.0.1 \
     --port 27017 \
     --logpath ~/mongodb/data/log/mongodb.log
   ```

3. Keep that terminal open while using MongoDB. Stop the server with `Ctrl+C`. Restart it by running the same `mongod` command again; its data remains in `~/mongodb/data/db`.

If the extracted folder already exists, do not repeat the `mv` command; run the server command from step 2.

### Windows and Linux

The Compose option above is the same on Windows and Linux and avoids operating-system-specific installation paths. For a native install, install MongoDB Community Server 9.0.2 using the official [MongoDB installation documentation](https://www.mongodb.com/docs/manual/administration/install-community/), selecting the guide for your OS and CPU architecture. Keep the installed server binary and data directory local to your machine, then start it in the foreground bound only to `127.0.0.1` on port `27017`.

For a native Linux installation where `mongod` is on `PATH`:

```sh
mkdir -p "$HOME/mongodb/data/db" "$HOME/mongodb/data/log"
mongod --dbpath "$HOME/mongodb/data/db" --bind_ip 127.0.0.1 --port 27017 --logpath "$HOME/mongodb/data/log/mongodb.log"
```

On Windows, use the official MSI/package instructions for your version and architecture. In PowerShell, create a data directory and run the installed `mongod.exe` in the foreground (adjust the version path if needed):

```powershell
New-Item -ItemType Directory -Force "$HOME\mongodb\data\db", "$HOME\mongodb\data\log"
& "C:\Program Files\MongoDB\Server\9.0\bin\mongod.exe" --dbpath "$HOME\mongodb\data\db" --bind_ip 127.0.0.1 --port 27017 --logpath "$HOME\mongodb\data\log\mongodb.log"
```

Stop a foreground native server with `Ctrl+C`; restart it with the same command. If your package installation configures MongoDB as a service, use the OS service manager instead and ensure its bind address remains loopback-only.

## Connect and Verify

For the Compose replica set, use this URI in the local application and MongoDB Compass:

```text
mongodb://127.0.0.1:27017/gadisaath?replicaSet=rs0
```

For the native standalone alternative, omit `?replicaSet=rs0`; this is the backend's default when `MONGODB_URI` is unset. For Compose, set `MONGODB_URI` in the shell to the replica-set URI before running `mvn spring-boot:run`. `.env.example` is read by Compose only and is not automatically loaded by Maven or Spring Boot. The sample credentials in `.env.example` are for local development only; do not put real credentials or other secrets in that file.

In MongoDB Compass, paste the applicable URI into the connection field and select **Connect**. To verify the Compose replica set, run `docker compose ps` and confirm MongoDB is healthy and the initializer exited successfully; inspect `docker compose logs mongodb mongodb-rs-init` if startup fails. The database is created when the application first writes data.

## GitHub Codespaces

`127.0.0.1` always refers to the machine or container where the backend process runs. It does not refer to a developer's Mac, and teammates should not use a developer's private IP as a shared endpoint.

When the backend runs in a Codespace, start this repository's Compose services there and connect from the host-run backend with `mongodb://127.0.0.1:27017/gadisaath?replicaSet=rs0`. The replica set advertises `localhost:27017` for the current host-run workflow; it is not configured for a backend container on the Compose network. Otherwise configure `MONGODB_URI` for an intentionally reachable development instance. Never expose an unauthenticated server publicly to make it reachable.

## Troubleshooting

- **Connection refused:** Confirm `mongod` or the Compose service is running, listening on port 27017, and that `MONGODB_URI` points to the same environment as the backend. In Codespaces, a laptop's `127.0.0.1` is not reachable from the Codespace.
- **Replica-set name mismatch:** The default Compose URI requires `replicaSet=rs0`; a native standalone server must use a URI without that option.
- **Port 27017 already in use:** Stop the other MongoDB process/container or choose another host port and update the URI to match. For Compose, change the host-side port in `compose.yaml` while keeping the container-side port `27017`.
- **Incorrect path or permission error:** Confirm the `--dbpath` and `--logpath` directories exist and are writable by the user running MongoDB. Create them before starting the server.
- **Missing server binary:** Confirm the archive matches your operating system and CPU architecture, that extraction completed, and that the command points to the extracted `bin/mongod` (macOS) or installed `mongod`/`mongod.exe`. Reinstall using the official MongoDB instructions if needed.
- **Compose image or service unavailable:** Check Docker is running, then use `docker compose logs mongodb` for startup details. The first start needs to download the `mongo:9.0.2` image.
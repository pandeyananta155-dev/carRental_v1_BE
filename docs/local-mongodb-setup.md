# Local MongoDB Setup

This guide provides a private, per-developer MongoDB Community Server for GadiSaath development. Each developer runs their own instance; local database data is not shared with the team and must never be committed to Git. Docker is optional: choose the Compose setup or the native installation for your operating system.

MongoDB authentication is disabled in these isolated local development examples. Keep the server bound to loopback (`127.0.0.1`) and never expose an unauthenticated MongoDB instance to a network or the internet.

## Prerequisites

- Git and this repository.
- For Compose: Docker Engine/Desktop with the Compose plugin (`docker compose`).
- For native installation: MongoDB Community Server 9.0.2 and a terminal. MongoDB Compass is optional.
- Java 17 and Maven to run this Spring Boot application.

## Docker Compose (macOS, Windows, and Linux)

The repository Compose service uses the official MongoDB Community image pinned to `9.0.2`, binds port 27017 only to the local host, and stores database files in a named Docker volume.

From the repository root, start MongoDB and inspect its status/logs:

```sh
docker compose up -d mongodb
docker compose ps
docker compose logs -f mongodb
```

Once healthy, connect with `mongodb://127.0.0.1:27017/gadisaath`. Run the backend from the host with:

```sh
MONGODB_URI=mongodb://127.0.0.1:27017/gadisaath mvn spring-boot:run
```

In PowerShell, set the variable for the current shell before running Maven:

```powershell
$env:MONGODB_URI = "mongodb://127.0.0.1:27017/gadisaath"
mvn spring-boot:run
```

Stop the container without deleting its database:

```sh
docker compose down
```

The named volume persists across stops and container recreation. To intentionally delete the local database too, run `docker compose down -v`; this permanently removes that volume's data. `docker compose start mongodb` and `docker compose stop mongodb` can also start or stop the existing container.

## Native macOS Installation

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

## Windows and Linux

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

Use this URI in the local application and MongoDB Compass:

```text
mongodb://127.0.0.1:27017/gadisaath
```

For the Spring Boot application, set `MONGODB_URI` in the shell before running `mvn spring-boot:run`. `.env.example` documents the variable and sample value; it is not automatically loaded by Maven or Spring Boot. Do not put credentials or other secrets in the example file.

In MongoDB Compass, paste the same URI into the connection field and select **Connect**. To verify a Compose server, run `docker compose ps` and confirm the service is healthy, or inspect `docker compose logs mongodb`. For native installs, a successful foreground startup with no bind or storage errors indicates the server started; Compass can also confirm a connection. The database is created when the application first writes data.

## GitHub Codespaces

`127.0.0.1` always refers to the machine or container where the backend process runs. It does not refer to a developer's Mac, and teammates should not use a developer's private IP as a shared endpoint.

When the backend runs in a Codespace, run a MongoDB instance reachable from that Codespace, for example by starting this repository's Compose service in the Codespace. If the backend runs directly in the Codespace, use `mongodb://127.0.0.1:27017/gadisaath` after the service starts. If both backend and MongoDB run as Compose services on the same Compose network, use `mongodb://mongodb:27017/gadisaath` from the backend container. Otherwise configure `MONGODB_URI` with the URI of an intentionally reachable development instance. Never expose an unauthenticated server publicly to make it reachable.

## Troubleshooting

- **Connection refused:** Confirm `mongod` or the Compose service is running, listening on port 27017, and that `MONGODB_URI` points to the same environment as the backend. In Codespaces, a laptop's `127.0.0.1` is not reachable from the Codespace.
- **Port 27017 already in use:** Stop the other MongoDB process/container or choose another host port and update the URI to match. For Compose, change the host-side port in `compose.yaml` while keeping the container-side port `27017`.
- **Incorrect path or permission error:** Confirm the `--dbpath` and `--logpath` directories exist and are writable by the user running MongoDB. Create them before starting the server.
- **Missing server binary:** Confirm the archive matches your operating system and CPU architecture, that extraction completed, and that the command points to the extracted `bin/mongod` (macOS) or installed `mongod`/`mongod.exe`. Reinstall using the official MongoDB instructions if needed.
- **Compose image or service unavailable:** Check Docker is running, then use `docker compose logs mongodb` for startup details. The first start needs to download the `mongo:9.0.2` image.
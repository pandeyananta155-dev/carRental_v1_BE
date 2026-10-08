# MongoDB connection test

In GitHub Codespaces, add a Codespaces secret named `MONGODB_URI` in the repository's **Settings > Secrets and variables > Codespaces**. Set its value to the connection URI from your MongoDB provider, then rebuild the Codespace so the secret is available.

Run the application:

```sh
mvn spring-boot:run
```

Successful output:

```text
MongoDB connection successful
```
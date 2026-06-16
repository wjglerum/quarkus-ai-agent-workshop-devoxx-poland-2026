# Weather MCP Server

This module showcases how to implement an MCP server with Quarkus.
It is a standalone Quarkus application that runs on port 8081 and exposes a weather forecast tool over MCP.
The MCP transport is HTTP/SSE and the tool is served at the path `/mcp/sse`.

## Authentication

The `/mcp/sse` endpoint is protected by OIDC, so a caller needs a valid bearer token to reach the weather tool.
This is enforced with the following configuration in `application.properties`:

```properties
quarkus.http.auth.permission.authenticated.paths=/mcp/sse
quarkus.http.auth.permission.authenticated.policy=authenticated
```

In dev mode Quarkus starts a Keycloak Dev Service automatically.
To obtain a bearer token for testing, open the Dev UI at http://localhost:8081/q/dev-ui and use the OpenID Connect / Keycloak card:

1. Open the Dev UI OpenID Connect card and follow the link to the Keycloak provider.
2. Sign in with either `alice:alice` or `bob:bob`.
3. Copy the access token.

You can then call the tool from the Dev UI under _Extensions_ > _Tools_ > _Call_, passing the access token together with a latitude and longitude.

## Token propagation self-call architecture

This module demonstrates a token propagation pattern where the caller's OIDC token is propagated downstream through an internal self-call.
The flow is:

1. `WeatherMcpServer` exposes the `current_weather` MCP tool. The tool method injects `WeatherClientInternal`.
2. `WeatherClientInternal` is a REST client annotated with `@AccessToken`. It calls back into this same application's secured `/weather/forecast` endpoint, propagating the incoming user token on the outgoing request.
3. `WeatherResource` is annotated with `@Authenticated` and serves `/weather/forecast`. Because the token was propagated, the call is authorized. It then injects `WeatherClient`.
4. `WeatherClient` is a REST client that calls the public Open-Meteo API at `https://api.open-meteo.com/v1/forecast` and returns the forecast.

The key point: the OIDC token presented to the MCP tool is propagated to the internal secured resource, so the same identity flows through every hop.

```
MCP caller (with OIDC token)
        |
        v
WeatherMcpServer  (@Tool current_weather)
        |  injects
        v
WeatherClientInternal  (@AccessToken REST client, propagates the token)
        |  HTTP call to http://localhost:8081/weather/forecast
        v
WeatherResource  (@Authenticated, /weather + /forecast)
        |  injects
        v
WeatherClient  (REST client)
        |  HTTP call
        v
Open-Meteo public API  (https://api.open-meteo.com/v1/forecast)
```

The base URL of the internal client is configured through a config key rather than hardcoded:

```properties
quarkus.rest-client.weather-internal.url=http://localhost:8081
```

## Relation to step 5

In `step-05-mcp-server` the AI agent acts as the MCP client of this server.
The agent connects to `http://localhost:8081/mcp/sse` and propagates the signed-in user's token, which lets it call the `current_weather` tool exposed here.
Both applications must run at the same time: this server on port 8081 and the agent on port 8080.

## References

- [Quarkus MCP Server](https://docs.quarkiverse.io/quarkus-mcp-server/dev/)
- [Open-Meteo](https://open-meteo.com)

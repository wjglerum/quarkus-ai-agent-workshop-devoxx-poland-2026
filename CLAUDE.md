# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

A hands-on conference workshop for building a secure AI agent with Quarkus and LangChain4j. It is a multi-module Maven project where each module is a self-contained Quarkus application. Requires JDK 25; Quarkus and the LangChain4j extensions are pinned by `quarkus.platform.version` (currently 3.36.2) in each module pom.

## The module model (read this first)

The numbered modules are not arbitrary packages, they are a teaching progression:

- `step-00-your-solution` is the participant's **working module**. It ships a minimal starter (an empty `ChatBot` placeholder interface that participants complete in step-02, plus the provider config) and is where workshop attendees write their own code. Its README is a linear guide that points at each step's README and explains the cumulative model.
- `step-01-introduction` through `step-08-testing` are **cumulative reference solutions**. Each step is the full solution up to and including that topic: `step-02` = chatbot, `step-03` = chatbot + auth, `step-04` = + tools, `step-05` = + MCP client, `step-06` = + RAG, `step-07` = + guardrails, `step-08` = + tests. To see the reference implementation for a given feature, open the module named after that feature, not the next one.
- `step-bonus-01..03` are **documentation only** (README with links, no code) and are intentionally excluded from the root `pom.xml` and the CI matrix.

When changing a behavior that exists in multiple steps (for example the `ChatBot` system message, a provider config key, or a guardrail), the same change usually has to be applied across every step module from where the feature is introduced onward. Configuration in `application.properties` is duplicated per module by design.

## Two runnable applications

1. **The AI agent** lives in the step modules and serves a WebSocket chatbot UI on **port 8080** (`http://localhost:8080`, chat widget bottom-right; Dev UI at `/q/dev-ui`).
2. **`weather-mcp-server`** is a separate Quarkus app on **port 8081** exposing a weather tool over MCP (HTTP/SSE at `/mcp/sse`). From `step-05` onward the agent is an MCP client of this server, so both apps must run at the same time for the weather feature to work. The agent points at it via `quarkus.langchain4j.mcp.weather.url`.

## Common commands

```shell
# One-time: warm the dependency cache for all modules (slow on first run)
./mvnw install -DskipTests

# Run a module in dev mode (hot reload + continuous testing + Dev UI)
cd step-08-testing && ./mvnw quarkus:dev

# Full build + tests for one module
./mvnw -B verify -pl step-08-testing

# Run a single test class
./mvnw test -pl step-08-testing -Dtest=MaxLengthTest
```

CI (`.github/workflows/build.yml`) runs `./mvnw -B verify` per module via a matrix; it sparse-checks out one module at a time, so every module builds standalone.

In dev mode Quarkus continuous testing runs tests automatically on change. Do not run `mvn clean` while dev mode is running, it breaks the running instance.

## LLM provider configuration

Provider is selected with `quarkus.langchain4j.chat-model.provider` in each module's `application.properties`. Four providers are on the classpath in every step module (ollama, openai, ai-gemini, anthropic). The default active provider is **Ollama** with model `qwen3.5:0.8b` (small, local, supports tool calling, which the tools/MCP/RAG steps require). OpenAI, Gemini, and Anthropic blocks are present but commented out; uncomment one and set the matching `*_API_KEY` env var to switch. Tool-using steps need a tool-capable model, so models without function calling are not suitable.

## Auth and dev services

From `step-03` onward the agent enables `quarkus-oidc`, which starts a Keycloak Dev Service automatically with default users `alice` / `bob` (password = username). Endpoints are protected (`quarkus.http.auth.permission.authenticated`), so dev mode prompts for login. The MCP server (`weather-mcp-server`) protects `/mcp/sse` and a bearer token can be obtained from its Dev UI OIDC card.

## Code layout

All Java lives under `org.acme`. Recurring types: `ChatBot` (the `@RegisterAiService` interface, where system message / `@ToolBox` / `@McpToolBox` / guardrails are wired), `ChatBotWebSocket` (the `/chat-bot` WebSocket endpoint), `IPLookupClient` (a REST client exposed as a `@Tool`), and `org.acme.guardrails` (input/output `Guardrail` implementations, tested in `step-08`). The chat UI is static `index.html` under `src/main/resources/META-INF/resources`.

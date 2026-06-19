# Step 1 - Introduction

This is the first step where we will introduce the project.
Start by running the following command in this directory:

```shell
# Change directory to this folder
cd step-01-introduction
./mvnw quarkus:dev
```

## Quarkus Dev Mode

This will start Quarkus in development mode. For maximum developer joy the following features are enabled by default:

- [Hot reload](https://quarkus.io/guides/maven-tooling#dev-mode)
- [Continuous Testing](https://quarkus.io/guides/continuous-testing)
- [Dev Services](https://quarkus.io/guides/dev-services)
- [Dev UI](https://quarkus.io/guides/dev-ui)
- ...

### Dev UI

Open your browser at [http://localhost:8080](http://localhost:8080) and navigate to the Dev UI:

![dev-ui.png](./../docs/images/dev-ui.png)

From the Dev UI you can explore the application and get a better understanding of how it works:

- Extensions
- Configuration
- Dev Services
- Logs
- ...

![dev-ui-extensions.png](./../docs/images/dev-ui-extensions.png)

Have a look around and try out the different features.

### Chat from Dev UI

We haven't implemented any chatbot yet, but you can already use the Dev UI to chat with the language model.
Navigate to the [chat](http://localhost:8080/q/dev-ui/quarkus-langchain4j-core/chat) and try it out.
If that link returns a 404, open the Dev UI at [http://localhost:8080/q/dev-ui](http://localhost:8080/q/dev-ui) and find the Chat option under the LangChain4j card.
You can even set the system message to give instructions to the model.
Try out some different messages and see how the model responds.

> [!NOTE]
> Notice that the model doesn't really care about typos or grammatical errors.
>

![dev-ui-langchain4j-chat.png](./../docs/images/dev-ui-langchain4j-chat.png)

### Logs

For easier debugging you can also check the logs for the interactions with the model.

These are enabled with the following [application.properties](src/main/resources/application.properties):

```properties
quarkus.langchain4j.log-requests=true
quarkus.langchain4j.log-responses=true
```

Example output:

```
2025-08-30 14:04:35,153 INFO  [dev.lan.htt.cli.log.LoggingHttpClient] (executor-thread-1) HTTP request:
- method: POST
- url: http://localhost:34941/api/chat
- headers: [Content-Type: application/json]
- body: {
  "model" : "qwen3.5:0.8b",
  "messages" : [ {
    "role" : "user",
    "content" : "Hi!"
  } ],
  "options" : {
    "temperature" : 0.8,
    "top_k" : 40,
    "top_p" : 0.9,
    "stop" : [ ]
  },
  "stream" : false,
  "tools" : [ ]
}

2025-08-30 14:04:38,628 INFO  [dev.lan.htt.cli.log.LoggingHttpClient] (executor-thread-1) HTTP response:
- status code: 200
- headers: [Content-Length: 330], [Content-Type: application/json; charset=utf-8], [Date: Sat, 30 Aug 2025 12:04:38 GMT]
- body: {
  "model": "qwen3.5:0.8b",
  "created_at": "2025-08-30T12:04:38.601433113Z",
  "message": {
    "role": "assistant",
    "content": "Hello! How can I assist you today?"
  },
  "done_reason": "stop",
  "done": true,
  "total_duration": 3401817342,
  "load_duration": 2845416824,
  "prompt_eval_count": 27,
  "prompt_eval_duration": 342279614,
  "eval_count": 10,
  "eval_duration": 213385322
}
```

## Quarkus LangChain4j extension

The [Quarkus LangChain4j](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html) extension is already included
in the project.
You can use it with various providers, here we explain how to use it with OpenAI, Ollama or Gemini.
Depending on the provider you have to add the corresponding dependency and configuration to your project.
Choose one of them and configure them accordingly with the instructions below.

### Quarkus LangChain4j OpenAI

To use OpenAI with Quarkus you have to add the following dependency to your project:

> [!NOTE]
> You have to set the `OPENAI_API_KEY` environment variable to use OpenAI.
> If you use Ollama you don't have to do this.

```xml

<dependency>
    <groupId>io.quarkiverse.langchain4j</groupId>
    <artifactId>quarkus-langchain4j-openai</artifactId>
</dependency>
```

And set the following configuration:

```properties
quarkus.langchain4j.chat-model.provider=openai
quarkus.langchain4j.openai.api-key=${OPENAI_API_KEY}
quarkus.langchain4j.openai.chat-model.model-name=gpt-4o
```

Or instead use Ollama with OpenAI compatibility:

```properties
quarkus.langchain4j.chat-model.provider=openai
quarkus.langchain4j.openai.base-url=http://localhost:11434/v1/
quarkus.langchain4j.openai.chat-model.model-name=qwen3.5:0.8b
```

For the details of the configuration please refer to
the [documentation](https://docs.quarkiverse.io/quarkus-langchain4j/dev/quickstart.html).

### Quarkus LangChain4j Ollama

To use Ollama directly with Quarkus you have to add the following dependency to your project:

```xml

<dependency>
    <groupId>io.quarkiverse.langchain4j</groupId>
    <artifactId>quarkus-langchain4j-ollama</artifactId>
</dependency>
```

And set the following configuration:

```properties
quarkus.langchain4j.chat-model.provider=ollama
quarkus.langchain4j.ollama.chat-model.model-id=qwen3.5:0.8b
```

For running Ollama you have two options:

- Run it yourself with `ollama run qwen3.5:0.8b` as we saw before
- Or let Quarkus run it for you automatically with devservices

For the details of the configuration please refer to
the [documentation](https://docs.quarkiverse.io/quarkus-langchain4j/dev/guide-ollama.html).

> [!IMPORTANT]
> Local models can be slow, and the default Ollama timeout is only `10s` (see [quarkiverse/quarkus-langchain4j#398](https://github.com/quarkiverse/quarkus-langchain4j/issues/398)). To avoid timeout errors this project raises it in [application.properties](src/main/resources/application.properties):
>
> ```properties
> # Set larger timeout for local language models
> quarkus.langchain4j.timeout=1m
> quarkus.langchain4j.ollama.timeout=1m
> ```
>
> `quarkus.langchain4j.timeout` is the global default; `quarkus.langchain4j.ollama.timeout` is the Ollama specific override that is guaranteed to apply. Raise it further (for example `2m`) if a minute is not enough.

### Quarkus LangChain4j Gemini

To use Gemini with Quarkus you have to add the following dependency to your project:

```xml

<dependency>
    <groupId>io.quarkiverse.langchain4j</groupId>
    <artifactId>quarkus-langchain4j-ai-gemini</artifactId>
</dependency>
```

And set the following configuration:

```properties
quarkus.langchain4j.chat-model.provider=ai-gemini
quarkus.langchain4j.ai.gemini.api-key=${GEMINI_API_KEY}
quarkus.langchain4j.ai.gemini.chat-model.model-id=gemini-2.5-flash
```

For the details of the configuration please refer to
the [documentation](https://docs.quarkiverse.io/quarkus-langchain4j/dev/gemini-chat-model.html).

### Quarkus LangChain4j Anthropic

To use Anthropic's Claude models with Quarkus you have to add the following dependency to your project:

> [!NOTE]
> You have to set the `ANTHROPIC_API_KEY` environment variable to use Anthropic.

```xml

<dependency>
    <groupId>io.quarkiverse.langchain4j</groupId>
    <artifactId>quarkus-langchain4j-anthropic</artifactId>
</dependency>
```

And set the following configuration:

```properties
quarkus.langchain4j.chat-model.provider=anthropic
quarkus.langchain4j.anthropic.api-key=${ANTHROPIC_API_KEY}
quarkus.langchain4j.anthropic.chat-model.model-name=claude-opus-4-8
```

For the details of the configuration please refer to
the [documentation](https://docs.quarkiverse.io/quarkus-langchain4j/dev/anthropic-chat-model.html).

## Next step

Feel free to explore the rest of the project, and afterwards continue with the
next [step](./../step-02-chatbot/README.md).

> [!NOTE]
> You should stay on working in the current directory, the other steps already contain the full solutions.

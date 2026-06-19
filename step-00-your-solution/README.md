# Step 0 - Your Workspace

Welcome! This is **your personal workspace** for the workshop.

**Stay in this folder** and do all your work here. You gradually build your own solution by writing and modifying code in this one module.

Each numbered step in the repository (`step-01-introduction`, `step-02-chatbot`, and so on) has its own README with explanations, hints, and a finished reference implementation. You read the instructions there, but you apply the changes here in `step-00-your-solution`.

This module starts almost empty on purpose. It has the Maven setup and the LLM provider configuration, but no application code yet. You create your first class, the `ChatBot`, in step 2.

## What to do

1. Make sure the project builds and runs from this module.
2. Work through the steps below in order. For each step, open that step's README, apply the changes here, and run the app to see the result.
3. If you get stuck, compare your workspace with the finished reference for that step.

## The path

The steps are cumulative: each one builds on the previous, so keep working in this same module.

| Step | What you add here | Instructions |
| ---- | ----------------- | ------------ |
| 1. Introduction | Nothing to code yet. Explore the Dev UI and chat with the model | [step-01](../step-01-introduction/README.md) |
| 2. Chatbot | Create the `ChatBot` AI service and add the WebSocket chat UI | [step-02](../step-02-chatbot/README.md) |
| 3. Authentication | Protect the app with OIDC and log in | [step-03](../step-03-authentication/README.md) |
| 4. Tools | Give the model a tool, an IP based location lookup | [step-04](../step-04-tools/README.md) |
| 5. MCP | Connect to the weather MCP server | [step-05](../step-05-mcp-server/README.md) |
| 6. RAG | Answer from your own documents | [step-06](../step-06-rag/README.md) |
| 7. Guardrails | Add input and output guardrails | [step-07](../step-07-guardrails/README.md) |
| 8. Testing | Test your guardrails | [step-08](../step-08-testing/README.md) |

## Slow model? Increase the timeout

Local models can be slow, and the default Ollama timeout is only `10s`. To avoid timeout errors this project raises it in [application.properties](src/main/resources/application.properties):

```properties
# Set larger timeout for local language models
quarkus.langchain4j.timeout=1m
quarkus.langchain4j.ollama.timeout=1m
```

`quarkus.langchain4j.ollama.timeout` is the Ollama specific override that is guaranteed to apply. Raise it (for example `2m`) if a minute is not enough. See [step-01](../step-01-introduction/README.md) for more detail.

## See only what a step adds

Because every step folder is the full solution up to that point, you can see exactly what a step introduces by comparing it with the previous step. For example, to see what step 4 adds on top of step 3, run this from the repository root:

```shell
diff -ru step-03-authentication/src step-04-tools/src
```

It is a handy way to find the few files a step actually changes.

## Continue learning

Start with [Step 01 - Introduction](../step-01-introduction/README.md) for context and your first goal.

> [!TIP]
> Try to implement each concept yourself first, then compare with the reference if you need a hand.

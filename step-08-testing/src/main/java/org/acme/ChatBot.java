package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;
import jakarta.enterprise.context.SessionScoped;
import org.acme.guardrails.AllowedLocationsGuardrail;
import org.acme.guardrails.MaxLength;
import org.acme.guardrails.PromptInjectionGuard;

@SessionScoped
@RegisterAiService
public interface ChatBot {

    @SystemMessage("""
                You are a helpful bot that helps users with recommendations about their location.
                You can get their location and extract the latitude and longitude.
                You use provided information to you about Krakow.
            """)
    // The explicit user-message template exposes the raw input as the "userMessage"
    // template variable, so MaxLength can measure what the user typed instead of the
    // RAG-augmented prompt the guardrail otherwise receives.
    @UserMessage("{userMessage}")
    @InputGuardrails({MaxLength.class, PromptInjectionGuard.class})
    @OutputGuardrails({ AllowedLocationsGuardrail.class })
    @ToolBox(IPLookupClient.class)
    @McpToolBox("weather")
    String chat(String userMessage);
}

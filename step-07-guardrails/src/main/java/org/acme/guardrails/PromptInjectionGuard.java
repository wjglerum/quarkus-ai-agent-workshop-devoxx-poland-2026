package org.acme.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Blocks common prompt-injection attempts before they reach the model.
 * Deterministic and case-insensitive: the input is matched against a configurable
 * list of suspicious phrases. A production system would layer an LLM based or
 * dedicated detector on top, this keeps the workshop demo fast and predictable.
 */
@ApplicationScoped
public class PromptInjectionGuard implements InputGuardrail {

    @ConfigProperty(name = "guardrails.injection.phrases") // check application.properties for the list
    String suspiciousCsv;

    @Override
    public InputGuardrailResult validate(UserMessage um) {
        String text = um.singleText();
        if (text == null || text.isBlank()) {
            return success(); // Nothing to validate
        }

        String lower = text.toLowerCase();
        for (String raw : suspiciousCsv.split(",")) {
            String phrase = raw.trim().toLowerCase();
            if (!phrase.isEmpty() && lower.contains(phrase)) {
                // A fatal failure: later guardrails won't run and the LLM will not be called
                return fatal("Your message contains a restricted phrase and cannot be processed.");
            }
        }
        return success();
    }
}

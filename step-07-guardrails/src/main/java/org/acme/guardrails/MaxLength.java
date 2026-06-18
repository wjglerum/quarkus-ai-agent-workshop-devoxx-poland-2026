package org.acme.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Fails if the user input exceeds the configured character budget.
 * Uses Unicode code point count (so emoji / surrogate pairs are counted correctly).
 *
 * Input guardrails run AFTER RAG augmentation, so the UserMessage handed to the
 * guardrail already includes the retrieved context, not just what the user typed.
 * To bound the raw input we read it from the "userMessage" template variable
 * populated by the ChatBot's @UserMessage("{userMessage}") template, and fall back
 * to the (augmented) message only when no such variable is available.
 */
@ApplicationScoped
public class MaxLength implements InputGuardrail {

    @ConfigProperty(name = "guardrails.max-input-chars", defaultValue = "1000")
    int maxChars;

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        Object raw = request.requestParams().variables().get("userMessage");
        if (raw != null) {
            return validate(UserMessage.from(raw.toString()));
        }
        return validate(request.userMessage());
    }

    @Override
    public InputGuardrailResult validate(UserMessage um) {
        String text = um.singleText();
        if (text == null) {
            return success(); // Nothing to validate
        }
        int len = text.codePointCount(0, text.length());
        if (len > maxChars) {
            // a fatal failure, the next InputGuardrail won't be evaluated
            return fatal("Your message is too long (" + len + " characters). Please keep it under " + maxChars + " characters.");
        }
        return success();
    }
}

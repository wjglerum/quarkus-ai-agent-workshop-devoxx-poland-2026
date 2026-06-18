package org.acme.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.GuardrailRequestParams;
import dev.langchain4j.guardrail.GuardrailResult;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.langchain4j.test.guardrail.GuardrailAssertions.assertThat;

class MaxLengthTest {

    @Test
    void underLimit_isSuccessful() {
        MaxLength rail = new MaxLength();
        rail.maxChars = 1000;

        InputGuardrailResult res = rail.validate(UserMessage.from("a".repeat(1000)));
        assertThat(res).isSuccessful();
    }

    @Test
    void overLimit_isFatal_withClearMessage() {
        MaxLength rail = new MaxLength();
        rail.maxChars = 1000;

        InputGuardrailResult res = rail.validate(UserMessage.from("a".repeat(1001)));
        assertThat(res)
                .hasResult(GuardrailResult.Result.FATAL)
                .assertSingleFailureSatisfies(f ->
                        org.assertj.core.api.Assertions.assertThat(f.message())
                                .contains("too long")
                                .contains("1001")
                                .contains("1000")
                );
    }

    @Test
    void emoji_areCountedByCodePoint() {
        MaxLength rail = new MaxLength();
        rail.maxChars = 3;

        // "A🙂B" == 3 code points; this is OK
        assertThat(rail.validate(UserMessage.from("A🙂B"))).isSuccessful();

        // "A🙂BC" == 4 code points; this is incorrect
        assertThat(rail.validate(UserMessage.from("A🙂BC")))
                .hasResult(GuardrailResult.Result.FATAL)
                .assertSingleFailureSatisfies(f ->
                        org.assertj.core.api.Assertions.assertThat(f.message())
                                .contains("too long")
                                .contains("4")
                                .contains("3")
                );

    }

    // Input guardrails run after RAG augmentation, so request.userMessage() is the
    // augmented prompt. The guardrail must instead measure the raw "userMessage"
    // template variable. These two tests pin that behavior.

    @Test
    void requestPath_measuresRawInput_notAugmentedPrompt() {
        MaxLength rail = new MaxLength();
        rail.maxChars = 1000;

        String rawInput = "what can we do in Krakow?";
        String augmentedPrompt = "a".repeat(5000); // query + retrieved RAG context

        InputGuardrailRequest request = inputRequest(rawInput, augmentedPrompt);

        assertThat(rail.validate(request)).isSuccessful();
    }

    @Test
    void requestPath_overLimitRawInput_isFatal_reportingRawCount() {
        MaxLength rail = new MaxLength();
        rail.maxChars = 1000;

        InputGuardrailRequest request = inputRequest("a".repeat(1001), "short augmented prompt");

        assertThat(rail.validate(request))
                .hasResult(GuardrailResult.Result.FATAL)
                .assertSingleFailureSatisfies(f ->
                        org.assertj.core.api.Assertions.assertThat(f.message())
                                .contains("too long")
                                .contains("1001")
                                .contains("1000")
                );
    }

    private static InputGuardrailRequest inputRequest(String rawInput, String augmentedPrompt) {
        GuardrailRequestParams params = GuardrailRequestParams.builder()
                .variables(Map.of("userMessage", rawInput))
                .userMessageTemplate("{userMessage}")
                .build();
        return InputGuardrailRequest.builder()
                .userMessage(UserMessage.from(augmentedPrompt))
                .commonParams(params)
                .build();
    }
}

package ua.lviv.javaclub.springai;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.ai.openai.api.OpenAiApi.ChatModel.GPT_3_5_TURBO;

@RestController
@RequestMapping("ai")
public class FunctionExample {

    private final OpenAiChatClient chatClient;

    FunctionExample(OpenAiChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/weather")
    public ResponseEntity<String> weather() {
        UserMessage userMessage = new UserMessage("What's the weather like in Kyiv, Lviv and San Francisco?");

        Prompt currentWeather = new Prompt(
                List.of(userMessage),
                OpenAiChatOptions.builder()
                        .withModel(GPT_3_5_TURBO.getValue())
                        .withFunction("currentWeather")
                        .build()
        );
        ChatResponse response = chatClient.call(currentWeather);

        return ResponseEntity.ok(response.getResult().getOutput().getContent());
    }

}

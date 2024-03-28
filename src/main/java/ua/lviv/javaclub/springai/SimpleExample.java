package ua.lviv.javaclub.springai;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import ua.lviv.javaclub.springai.dto.JokeDto;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.openai.api.OpenAiApi.ChatModel.GPT_3_5_TURBO;

@RestController
@RequestMapping("ai")
public class SimpleExample {

    private final String MESSAGE = "tell a joke about programmers"; // розкажи жарт про програмістів
    private final ChatClient aiClient;
    private final OpenAiChatClient chatClient;

    SimpleExample(ChatClient aiClient, OpenAiChatClient chatClient) {
        this.aiClient = aiClient;
        this.chatClient = chatClient;
    }

    @GetMapping("/joke")
    public ResponseEntity<String> generateJoke() {
        String response = aiClient.call(MESSAGE);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatClient.stream(prompt);
    }

    @GetMapping("/generate")
    public ResponseEntity<String> generate() {
        OpenAiApi openAiApi = new OpenAiApi(System.getenv("OPENAI_API_KEY"));
        OpenAiApi.ChatCompletionMessage chatCompletionMessage =
                new OpenAiApi.ChatCompletionMessage("Hello world", OpenAiApi.ChatCompletionMessage.Role.USER);

        ResponseEntity<OpenAiApi.ChatCompletion> response = openAiApi.chatCompletionEntity(
                new OpenAiApi.ChatCompletionRequest(List.of(chatCompletionMessage), "gpt-3.5-turbo", 0.8f, false));
        var message = response.getBody().choices()
                .stream()
                .map(c -> c.message().content())
                .collect(Collectors.joining("\n"));

        return ResponseEntity.ok(message);
    }


    @GetMapping("/prompt/joke")
    public ResponseEntity<String> generateJokeViaPrompt() {

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .withModel(GPT_3_5_TURBO.getValue())
                .withTemperature(0.2f)
                .withMaxTokens(30)
                .build();

        Prompt prompt = new Prompt(MESSAGE, chatOptions);
        ChatResponse response = aiClient.call(prompt);

        Generation result = response.getResult();
        AssistantMessage output = result.getOutput();

        return ResponseEntity.ok(output.getContent());
    }

    @GetMapping("/prompt/{type}/{profession}")
    public JokeDto generateJokeViaPrompt(
            @PathVariable("type") String type,
            @PathVariable("profession") String profession) {

        var promptString = """
                tell a {type} about {profession}
                {format}
                """;

        var promptTemplate = new PromptTemplate(promptString);
        promptTemplate.add("type", type);
        promptTemplate.add("profession", profession);

        var outputParser = new BeanOutputParser<>(JokeDto.class);
        promptTemplate.add("format", outputParser.getFormat());
        promptTemplate.setOutputParser(outputParser);

        var prompt = promptTemplate.create();
        var response = aiClient.call(prompt);

        return outputParser.parse(response.getResult().getOutput().getContent());
    }

}

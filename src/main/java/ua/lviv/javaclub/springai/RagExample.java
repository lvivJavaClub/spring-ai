package ua.lviv.javaclub.springai;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("ai")
public class RagExample {

    private final ChatClient aiClient;
    private final VectorStore vectorStore;

    RagExample(ChatClient aiClient, VectorStore vectorStore) {
        this.aiClient = aiClient;
        this.vectorStore = vectorStore;
    }

    private final String TEMPLATE = """
                        
            You're assisting with questions about City of Houston Administration and Regulatory Affairs Washington Corridor Parking Benefit District.
            The Washington Avenue Corridor Parking Benefit District (PBD) is a defined geographic area in which a portion of the meter revenue is returned to the district to finance improvements that enhance the quality of life and promote walking, cycling, and the use of public transportation.
            The area included in the Washington Avenue Corridor Parking Benefit District is bounded by Houston Avenue, Center Boulevard, Lillian Street/Decatur Street and Westcott Street.
                    
            Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
            If unsure, simply state that you don't know.
                    
            DOCUMENTS:
            {documents}
                        
            """;

    @GetMapping("/ask/{question}")
    public ResponseEntity<String> generateJokeViaPrompt(@PathVariable("question") String question) {
        var listOfSimilarDocuments = this.vectorStore.similaritySearch(question);
        var documents = listOfSimilarDocuments
                .stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));

        var systemMessage = new SystemPromptTemplate(TEMPLATE)
                .createMessage(Map.of("documents", documents));

        var userMessage = new UserMessage(question);

        var prompt = new Prompt(List.of(systemMessage, userMessage));

        var aiResponse = aiClient.call(prompt);
        return ResponseEntity.ok(aiResponse.getResult().getOutput().getContent());
    }
}

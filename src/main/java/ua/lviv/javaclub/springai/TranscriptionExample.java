package ua.lviv.javaclub.springai;

import org.springframework.ai.openai.OpenAiAudioTranscriptionClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ai")
public class TranscriptionExample {

    private final OpenAiAudioTranscriptionClient client;

    TranscriptionExample(OpenAiAudioTranscriptionClient client) {
        this.client = client;
    }

    @GetMapping("/transcription")
    public ResponseEntity<String> transcription() {

        var transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withTemperature(0f)
                .build();

        var audioFile = new FileSystemResource("src/main/resources/audio.flac");

        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        AudioTranscriptionResponse response = client.call(transcriptionRequest);
        return ResponseEntity.ok(response.getResult().getOutput());

    }

}

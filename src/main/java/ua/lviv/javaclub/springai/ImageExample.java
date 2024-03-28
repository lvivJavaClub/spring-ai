package ua.lviv.javaclub.springai;

import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ai")
public class ImageExample {

    private final ImageClient client;

    ImageExample(ImageClient client) {
        this.client = client;
    }

    @GetMapping("/image")
    public ResponseEntity<String> image(@RequestParam(value = "message", defaultValue = "A light cream colored mini golden doodle") String message) {
        var options = OpenAiImageOptions.builder()
                .withQuality("hd")
                .withN(1)
                .withStyle("vivid")
                .withResponseFormat("url")
                .build();
        var prompt = new ImagePrompt(message, options);
        ImageResponse response = client.call(prompt);
        return ResponseEntity.ok(response.getResult().getOutput().getUrl());
    }

}

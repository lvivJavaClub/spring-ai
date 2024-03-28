package ua.lviv.javaclub.springai;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.function.Function;

@Configuration
public class Config {

    @Bean
    VectorStore vectorStore(EmbeddingClient ec, JdbcTemplate t) {
        return new PgVectorStore(t, ec);
    }

    @Bean
    TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    @Bean
    ApplicationRunner applicationRunner(
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate,
            @Value("classpath:FAQ.pdf") Resource resource) {
        return args -> init(vectorStore, jdbcTemplate, resource);
    }


    static void init(VectorStore vectorStore, JdbcTemplate template, Resource pdfResource) {

        template.update("delete from vector_store");

        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(
                        new ExtractedTextFormatter.Builder()
                                .withNumberOfBottomTextLinesToDelete(3)
                                .withNumberOfTopPagesToSkipBeforeDelete(1)
                                .build()
                )
                .withPagesPerDocument(2)
                .build();

        var pdfReader = new PagePdfDocumentReader(pdfResource, config);
        var textSplitter = new TokenTextSplitter();
        vectorStore.accept(textSplitter.apply(pdfReader.get()));
    }

    @Bean
    @Description("Get the weather in location")
    public Function<MockWeatherService.Request, MockWeatherService.Response> currentWeather() {
        return new MockWeatherService();
    }
}

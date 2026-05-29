package com.poptrade.common.config;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
public class AiAssistantModelConfig {

    @Bean("aiAssistantStreamingChatModel")
    public StreamingChatLanguageModel aiAssistantStreamingChatModel(AiProperties aiProperties) {
        String apiKey = StringUtils.hasText(aiProperties.getApiKey()) ? aiProperties.getApiKey() : "missing-api-key";
        return OpenAiStreamingChatModel.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .apiKey(apiKey)
                .modelName(aiProperties.getModel())
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}

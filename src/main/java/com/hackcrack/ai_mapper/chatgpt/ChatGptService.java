package com.hackcrack.ai_mapper.chatgpt;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import okhttp3.*;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChatGptService {

    private static final String GPT_MODEL = "ada";
    private static final String GPT_API_TOKEN = "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";

    private OpenAiService service = new OpenAiService(GPT_API_TOKEN);

    public JSONObject invokeChatGPT(JSONObject source, JSONObject target) {
        System.out.println("\nCreating completion...");

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(GPT_MODEL)
                .messages(generateMessagesFromJson(source, target))
                .build();
        List<ChatCompletionChoice> choices = service.createChatCompletion(chatCompletionRequest).getChoices();
        if (choices.size() > 0) {
            System.out.println("Completion created: " + choices.get(0).toString());
        } else {
            System.out.println("Completion created: <EMPTY>");
        }
        return new JSONObject();
    }

    private List<ChatMessage> generateMessagesFromJson(JSONObject source, JSONObject target) {
        // System message to set the context
        ChatMessage systemMessage = buildChatMessage("system", "You are an assistant that creates mapping schemas.");
        // User message providing the source JSON object
        ChatMessage userMessage = buildChatMessage("user", "Create a mapping schema for the given source and target JSON objects:");

        ChatMessage sourceMessage = buildChatMessage("user", source.toString());
        ChatMessage targetMessage = buildChatMessage("user", target.toString());

        List<ChatMessage> messages = new ArrayList<>();

        messages.add(systemMessage);
        messages.add(userMessage);
        messages.add(sourceMessage);
        messages.add(targetMessage);
        return messages;
    }

    @NotNull
    private static ChatMessage buildChatMessage(String role, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        return chatMessage;
    }
}

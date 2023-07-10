package com.hackcrack.ai_mapper.chatgpt;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import okhttp3.*;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChatGptService {

    private static final String GPT_MODEL = "gpt-3.5-turbo";

    private static final String GPT_API_TOKEN = "TODO";

    private static final String GPT_INSTRUCTION_MESSAGE =
            "Create a mapping schema for the next given source and target JSON objects. " +
            "Respond with a json object that contains the mapping. " +
            "Include optional Transformations for more complex mappings - we need a transformation for each object";

    private OpenAiService service = new OpenAiService(GPT_API_TOKEN);

    private JSONParser parser = new JSONParser();

    public JSONObject invokeChatGPT(JSONObject source, JSONObject target) {
        System.out.println("\nCreating completion...");

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(GPT_MODEL)
                .messages(generateMessagesFromJson(source, target))
                .build();
        List<ChatCompletionChoice> choices = service.createChatCompletion(chatCompletionRequest).getChoices();
        if (choices.size() == 1) {
            try {
                String gptResponseContent = choices.get(0).getMessage().getContent();
                JSONObject gptResponseContentJsonObject = (JSONObject) parser.parse(gptResponseContent);
                return gptResponseContentJsonObject;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else if(choices.size() > 0) {
            return new JSONObject();
        } else {
            System.out.println("Completion created: <EMPTY>");
            return new JSONObject();
        }
    }

    private List<ChatMessage> generateMessagesFromJson(JSONObject source, JSONObject target) {
        // System message to set the context
        ChatMessage systemMessage = buildChatMessage("system", "You are an assistant that creates mapping schemas.");
        // User message providing the source JSON object
        ChatMessage userMessage = buildChatMessage("user", GPT_INSTRUCTION_MESSAGE);
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

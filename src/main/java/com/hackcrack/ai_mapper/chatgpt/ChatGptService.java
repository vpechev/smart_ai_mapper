package com.hackcrack.ai_mapper.chatgpt;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import okhttp3.*;

import java.util.List;

@Component
public class ChatGptService {

    private static final String GPT_MODEL = "ada";
    private static final String GPT_API_TOKEN = "YOUR_API_TOKEN";

    private OpenAiService service = new OpenAiService(GPT_API_TOKEN);

    public JSONObject invokeChatGPT(JSONObject source, JSONObject target) {
        System.out.println("\nCreating completion...");

        // CompletionRequest example
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(GPT_MODEL)
                .messages(generatePromptMessageFromJson(source, target))
//                .prompt(generatePromptMessageFromJson(source, target).toJSONString())
                .build();
        List<ChatCompletionChoice> choices = service.createChatCompletion(chatCompletionRequest).getChoices();
        if (choices.size() > 0) {
            System.out.println("Completion created: " + choices.get(0).toString());
        } else {
            System.out.println("Completion created: <EMPTY>");
        }
        return new JSONObject();
    }

    public JSONObject invokeChatGPTApi(JSONObject source, JSONObject target) {
        OkHttpClient client = new OkHttpClient();
        String payload = generatePromptMessageFromJson(source, target).toJSONString();
//        String payload = "Tell me something about yourself";
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), payload);

        String endpoint = "https://api.openai.com/v1/completions";
        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + GPT_API_TOKEN)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseData = response.body().string();
                System.out.println("Assistant Response: " + responseData);
            } else {
                System.out.println("Request failed with code: " + response.code());
                System.out.println("Response body: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    private JSONArray generatePromptMessageFromJson(JSONObject source, JSONObject target) {
        // System message to set the context
        JSONObject systemMessage = buildGPTMessage("system", "You are an assistant that creates mapping schemas.");
        // User message providing the source JSON object
        JSONObject userMessage = buildGPTMessage("user", "Create a mapping schema for the given source and target JSON objects:");

        JSONObject sourceMessage = buildGPTMessage("user", source.toString());
        JSONObject targetMessage = buildGPTMessage("user", target.toString());

        JSONArray messages = new JSONArray();

        messages.add(systemMessage);
        messages.add(userMessage);
        messages.add(sourceMessage);
        messages.add(targetMessage);
        return messages;
    }

    @NotNull
    private static JSONObject buildGPTMessage(String role, String content) {
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", role);
        userMessage.put("content", content);
        return userMessage;
    }
}

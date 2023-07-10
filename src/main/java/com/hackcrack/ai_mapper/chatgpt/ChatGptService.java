package com.hackcrack.ai_mapper.chatgpt;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatGptService {

    private static final String GPT_MODEL = "gpt-3.5-turbo";

    private static final String GPT_API_TOKEN = System.getenv("GPT_API_TOKEN");

    private static final String GPT_INSTRUCTION_MESSAGE =
        "Create a mapping schema for the next given source and target JSON objects. "
        + "Respond with a json object that contains mapping schema for the source and target objects."
        + "Make sure to include a mapping for each field in the source and target objects."
        + "The format of the mapping should follow the format from 3rd mapping message in the input."
        + "Include optional Transformation functions more complex mappings."
        + "Add javascript source code for transformation functions in the 'transformations' field."
        + "Make sure that the same transformation is applied for multiple related fields."
    ;

    private OpenAiService service = new OpenAiService(GPT_API_TOKEN);

    private JSONParser parser = new JSONParser();

    private static final JSONObject ERROR_RESPONSE_OBJECT = new JSONObject();

    static {
        ERROR_RESPONSE_OBJECT.put("error", "Failed to create mapping schema");
    }

    public List<JSONObject> invokeChatGPT(JSONObject source, JSONObject target) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(GPT_MODEL)
                .messages(generateMessagesFromJson(source, target))
                .n(3)
                .build();
        List<ChatCompletionChoice> choices = service.createChatCompletion(chatCompletionRequest).getChoices();
        if (choices.size() == 1) {
            return List.of(transformJsonObject(choices.get(0).getMessage()));
        } else if(choices.size() > 0) {
            return choices.stream().map(x -> transformJsonObject(x.getMessage())).collect(Collectors.toList());
        } else {
            ERROR_RESPONSE_OBJECT.put("response_content", "No response from GPT");
            return List.of(ERROR_RESPONSE_OBJECT);
        }
    }

    private JSONObject transformJsonObject(ChatMessage chatMessage) {
        String gptResponseContent = chatMessage.getContent();
        try {
            JSONObject gptResponseContentJsonObject = (JSONObject) parser.parse(gptResponseContent);
            return gptResponseContentJsonObject;
        } catch (ParseException e) {
            System.out.println("Error parsing GPT response: " + e.getMessage());
            System.out.println("ChatGPT returned: " + gptResponseContent);

            ERROR_RESPONSE_OBJECT.put("response_content", gptResponseContent);
            return ERROR_RESPONSE_OBJECT;
        }
    }

    private List<ChatMessage> generateMessagesFromJson(JSONObject source, JSONObject target) {
        // System message to set the context
        ChatMessage systemMessage = buildChatMessage("system", "You are an assistant that creates mapping schemas.");
        // User message providing the source JSON object
        ChatMessage userMessage = buildChatMessage("user", GPT_INSTRUCTION_MESSAGE);
        ChatMessage sourceMessage = buildChatMessage("user", source.toJSONString());
        ChatMessage targetMessage = buildChatMessage("user", target.toJSONString());
        ChatMessage mappingExampleMessage = buildChatMessage("user", buildMappingExampleObject().toJSONString());

        List<ChatMessage> messages = new ArrayList<>();

        messages.add(systemMessage);
        messages.add(userMessage);
        messages.add(sourceMessage);
        messages.add(targetMessage);
        messages.add(mappingExampleMessage);

        return messages;
    }

    private static JSONObject buildMappingExampleObject() {
        JSONObject example = new JSONObject();

        example.put("source", "source_object_field");
        example.put("target", "target_object_field");
        example.put("transformation", "transformation_function");

        return example;
    }

    private ChatMessage buildChatMessage(String role, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        return chatMessage;
    }
}

package com.hackcrack.ai_mapper.chatgpt;

import com.hackcrack.ai_mapper.io.IoService;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatGptService {

    private static final String GPT_MODEL = "gpt-3.5-turbo-0613";

    private static final String GPT_API_TOKEN = System.getenv("GPT_API_TOKEN");

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private OpenAiService service = new OpenAiService(GPT_API_TOKEN, DEFAULT_TIMEOUT);

    private JSONParser parser = new JSONParser();

    private IoService ioService;

    @Autowired
    public ChatGptService(IoService ioService) {
        this.ioService = ioService;
    }

    private static final JSONObject ERROR_RESPONSE_OBJECT = new JSONObject();

    static {
        ERROR_RESPONSE_OBJECT.put("error", "Failed to create mapping schema");
    }

    public List<JSONObject> invokeChatGPT(JSONObject source, JSONObject target) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(GPT_MODEL)
                .messages(generateMessagesFromJson(source, target))
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

        return List.of(
            // System message to set the context
            buildSystemChatMessage("Your task is to generate a mapping schema that transforms a given source JSON object into a target JSON object"),

            // User message providing the source JSON object
            buildSystemChatMessage("Make sure to include a mapping for each field in the source and target objects.\"\n" +
                    "        + \"Include optional Transformation functions more complex mappings."),
//            buildSystemChatMessage("Mapping should follow the format from the example below"),
//            buildSystemChatMessage(buildMappingExampleObject().toJSONString()),
            buildSystemChatMessage("You can use the following example as a reference while doing the mapping:"),
            buildSystemChatMessage(readTrainJson().toJSONString()),

            buildUserChatMessage("Create a mapping schema for the next given source and target JSON objects based on the instructions above."),
            buildUserChatMessage(source.toJSONString()),
            buildUserChatMessage(target.toJSONString()),

            buildSystemChatMessage("Respond ONLY with a json object that contains mapping schema.")
        );
    }

    private static JSONObject buildMappingExampleObject() {
        JSONObject example = new JSONObject();

        example.put("source", "source_object_field");
        example.put("target", "target_object_field");
        example.put("transformation", "transformation_function");

        return example;
    }

    private ChatMessage buildSystemChatMessage(String content) {
        return buildChatMessage("system", content);
    }

    private ChatMessage buildUserChatMessage(String content) {
        return buildChatMessage("user", content);
    }

    private ChatMessage buildChatMessage(String role, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        return chatMessage;
    }

    private JSONObject readTrainJson() {
        String sourceFileName = "train_data/train_0.json";
        JSONObject sourceJsonObject = ioService.readJsonFile(sourceFileName);
        return sourceJsonObject;
    }
}

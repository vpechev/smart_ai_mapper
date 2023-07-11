package com.hackcrack.ai_mapper.chatgpt;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapperEngineService {

    private ChatGptService chatGptService;

    @Autowired
    public MapperEngineService(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }

    public List<JSONObject> createMappingSchema(JSONObject source, JSONObject target) {
//        var list = new ArrayList<JSONObject>();
//        IntStream
//            .range(0, 3)
//            .forEach(idx -> list.addAll(chatGptService.invokeChatGPT(source, target)));
//        return list;

        return chatGptService.invokeChatGPT(source, target);
    }
}

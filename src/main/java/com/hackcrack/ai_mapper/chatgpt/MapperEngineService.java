package com.hackcrack.ai_mapper.chatgpt;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MapperEngineService {

    private ChatGptService chatGptService;

    @Autowired
    public MapperEngineService(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }

    public JSONObject createMappingSchema(JSONObject source, JSONObject target) {
        return new JSONObject();
    }
}

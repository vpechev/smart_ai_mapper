package com.hackcrack.ai_mapper.io;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Component
public class IoService {

    private JSONParser parser = new JSONParser();

    public JSONObject readJsonFile(String fileName) {
        try {
            // Read the JSON file and parse it
            Object obj = parser.parse(new FileReader(fileName));

            // Convert the parsed object to a JSONObject
            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException("Error trying to parse the file", e);
        }
    }

    public void writeJsonFile(String directory, String fileName, JSONObject jsonObject) {
        File file = createFile(directory, fileName);
        writeFile(file, jsonObject);
    }

    private void writeFile(File file, JSONObject jsonObject) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(jsonObject.toJSONString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException("Error writing json file to file system. File name: " + file.getName(), e);
        }
    }

    private File createFile(String directory, String fileName) {
        try {
            File dir = new File(directory);
            dir.mkdirs();

            File file = new File(directory + File.separator + fileName);
            file.createNewFile();

            return file;
        } catch (IOException e) {
            throw new RuntimeException("Error creating new json file to file system. File name: " + fileName, e);
        }
    }
}

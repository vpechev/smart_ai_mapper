package com.hackcrack.ai_mapper.tasks;

import com.hackcrack.ai_mapper.chatgpt.MapperEngineService;
import com.hackcrack.ai_mapper.io.IoService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages= {"com.hackcrack.ai_mapper.io", "com.hackcrack.ai_mapper.chatgpt"})
public class ConsoleRunner implements CommandLineRunner {

    private static final String TEST_DATA_DIRECTORY_PATTERN = "sample_test_data/example_";

    private static final String OUTPUT_DIRECTORY = "test_output_mapping_schemas";

    private static final int DATASET_SIZE = 5;

    private IoService ioService;

    private MapperEngineService mapperEngineService;

    @Autowired
    public ConsoleRunner(IoService ioService, MapperEngineService mapperEngineService) {
        this.ioService = ioService;
        this.mapperEngineService = mapperEngineService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsoleRunner.class, args);

    }

    @Override
    public void run(String... args) {
        for (int i = 1; i <= DATASET_SIZE; i++) {
            String directoryName = TEST_DATA_DIRECTORY_PATTERN + i;

            String sourceFileName = directoryName + "/source_schema.json";
            JSONObject sourceJsonObject = ioService.readJsonFile(sourceFileName);

            String targetFileName = directoryName + "/target_schema.json";
            JSONObject targetJsonObject = ioService.readJsonFile(targetFileName);

            System.out.println("Creating mapping schema for file " + i);
            JSONObject outputMappingSchema = mapperEngineService.createMappingSchema(sourceJsonObject, targetJsonObject);

            ioService.writeJsonFile(OUTPUT_DIRECTORY, " output_" + i + ".json", outputMappingSchema);
        }
    }
}

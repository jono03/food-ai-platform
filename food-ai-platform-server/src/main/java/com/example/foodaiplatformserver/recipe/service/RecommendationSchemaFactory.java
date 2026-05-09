package com.example.foodaiplatformserver.recipe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

final class RecommendationSchemaFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RecommendationSchemaFactory() {
    }

    static JsonNode responseSchema() {
        ObjectNode properties = OBJECT_MAPPER.createObjectNode();
        properties.set("available_now", recipeArraySchema());
        properties.set("need_few_ingredients", recipeArraySchema());

        ObjectNode schema = OBJECT_MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.set("properties", properties);
        schema.set("required", OBJECT_MAPPER.valueToTree(List.of("available_now", "need_few_ingredients")));
        return schema;
    }

    private static JsonNode recipeArraySchema() {
        ObjectNode itemProperties = OBJECT_MAPPER.createObjectNode();
        itemProperties.set("recipe_id", OBJECT_MAPPER.createObjectNode().put("type", "integer"));
        itemProperties.set("recipe_name", OBJECT_MAPPER.createObjectNode().put("type", "string"));
        itemProperties.set("category", OBJECT_MAPPER.createObjectNode().put("type", "string"));
        itemProperties.set("expiring_ingredients_used", stringArraySchema());
        itemProperties.set("all_ingredients", stringArraySchema());
        itemProperties.set("missing_ingredients", stringArraySchema());
        itemProperties.set("instructions", stringArraySchema());

        ObjectNode itemSchema = OBJECT_MAPPER.createObjectNode();
        itemSchema.put("type", "object");
        itemSchema.set("properties", itemProperties);
        itemSchema.set("required", OBJECT_MAPPER.valueToTree(List.of(
                "recipe_id",
                "recipe_name",
                "category",
                "expiring_ingredients_used",
                "all_ingredients",
                "missing_ingredients",
                "instructions"
        )));

        ObjectNode arraySchema = OBJECT_MAPPER.createObjectNode();
        arraySchema.put("type", "array");
        arraySchema.set("items", itemSchema);
        return arraySchema;
    }

    private static JsonNode stringArraySchema() {
        return OBJECT_MAPPER.createObjectNode()
                .put("type", "array")
                .set("items", OBJECT_MAPPER.createObjectNode().put("type", "string"));
    }
}

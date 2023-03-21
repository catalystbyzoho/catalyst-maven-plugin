package com.zoho.catalyst.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class CatalystResponseDeserializer<T> extends StdDeserializer<T> {

    private final Class<T> targetClass;
    public CatalystResponseDeserializer(Class<T> vc) {
        super(vc);
        targetClass = vc;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if(!node.has("data")) {
            return null;
        }
        String dataContent = node.get("data").toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(dataContent, this.targetClass);
    }
}

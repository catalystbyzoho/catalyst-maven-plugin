package com.zoho.catalyst.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import okhttp3.Response;

import java.io.IOException;

public class ResponseUtil {
    public static <T> T deserializeCatalystResponse(Response response, Class<T> targetClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(targetClass, new CatalystResponseDeserializer<>(targetClass));
        mapper.registerModule(module);
        return mapper.readValue(response.body().byteStream(), targetClass);
    }
}

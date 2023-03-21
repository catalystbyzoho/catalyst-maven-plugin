package com.zoho.catalyst.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.java.Log;
import okhttp3.Response;

@Log
public class ResponseUtil {

    public static <T> T deserializeResponse(Response response, Class<T> targetClass, StdDeserializer<T> deserializer) throws Exception {
        if(!response.isSuccessful()) {
            log.severe("Unsuccessful response from server : " + response.body().string());
            throw new Exception("API request to " + response.request().url() + " failed with status : " + response.code());
        }
        ObjectMapper mapper = new ObjectMapper();
        if(deserializer != null) {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(targetClass, deserializer);
            mapper.registerModule(module);
        }
        return mapper.readValue(response.body().byteStream(), targetClass);
    }

    public static <T> T deserializeResponse(Response response, Class<T> targetClass) throws Exception {
        return deserializeResponse(response, targetClass, null);
    }
}

package org.example.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    // ObjectMapper có thể được cấu hình thêm nếu cần
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Chuyển đổi Java object thành chuỗi JSON.
     * @param obj đối tượng cần chuyển đổi.
     * @param <T> kiểu của đối tượng.
     * @return JSON string.
     * @throws JsonProcessingException nếu có lỗi khi convert.
     */
    public static <T> String toJson(T obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Chuyển đổi chuỗi JSON thành đối tượng của lớp cụ thể.
     * @param json chuỗi JSON.
     * @param clazz lớp của đối tượng mong muốn.
     * @param <T> kiểu của đối tượng.
     * @return đối tượng được convert.
     * @throws JsonProcessingException nếu có lỗi khi convert.
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Chuyển đổi chuỗi JSON thành đối tượng theo kiểu generic (ví dụ: List<CartItemDto>).
     * @param json chuỗi JSON.
     * @param typeReference TypeReference của đối tượng mong muốn.
     * @param <T> kiểu của đối tượng.
     * @return đối tượng được convert.
     * @throws JsonProcessingException nếu có lỗi khi convert.
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) throws JsonProcessingException {
        return objectMapper.readValue(json, typeReference);
    }
}

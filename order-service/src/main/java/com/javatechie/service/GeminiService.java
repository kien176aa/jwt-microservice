package com.javatechie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.client.ProductClient;
import com.javatechie.dto.OrderFilter;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CommonResponse;
import org.example.dtos.ProductDto;
import org.example.dtos.SearchProductRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductClient productClient;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();


    public String chat(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        return (String) parts.get(0).get("text");
    }

    public String processUserQuestion(String question, Long userId) {
        try {
            // 1. Gửi câu hỏi đến Gemini để phân tích
            String aiResponse = analyzeQuestionWithAI(question);
            log.info("AI Response: {}", aiResponse);
            // 2. Parse response từ AI để xem có cần call API không
            if (needsDataRetrieval(aiResponse)) {
                log.info("AI Response3: {}", aiResponse);
                // 3. Thực hiện API call để lấy dữ liệu
                String data = executeDataRetrieval(aiResponse, userId);
                log.info("AI Response4: {}", data);
                // 4. Gửi lại cho AI để xử lý và trả lời
                return generateFinalResponse(question, data);
            } else {
                // 5. Trả lời trực tiếp từ AI
                return extractDirectResponse(aiResponse);
            }
        } catch (Exception e) {
            return "Xin lỗi, tôi không thể xử lý yêu cầu này. Vui lòng thử lại!";
        }
    }

    private String analyzeQuestionWithAI(String question) {
        String prompt = buildAnalysisPrompt(question);
        return callGeminiAPI(prompt);
    }

    private String buildAnalysisPrompt(String question) {
        return String.format("""
            Bạn là một AI assistant thông minh. Phân tích câu hỏi của user và quyết định:
            
            Câu hỏi: "%s"
            
            Nếu câu hỏi liên quan đến:
            - Đơn hàng/orders (tìm đơn hàng, tính tổng tiền, thống kê đơn hàng, doanh thu, v.v.)
            - Sản phẩm/products (tìm sản phẩm, tìm theo tên, giá cả, số lượng, v.v.)
           
            Hãy trả lời theo format JSON:
            {
                "needsData": true,
                "type": "order" hoặc "product",
                "action": "mô tả hành động cần thực hiện",
                "parameters": {
                    // Các tham số cho việc tìm kiếm
                    // Với order: userId, status, fromDate, toDate, minTotalPrice, maxTotalPrice, voucherMess, fieldsToSelect
                    // Với product: name, nameOp, price, priceOp, quantity, quantityOp, sortBy, orderBy, status
                }
            }
            
            - Liên quan đến order:
                + userId: bạn yên tâm đã truyền sẵn userId của currentUser rồi
                + fromDate và toDate: phạm vi tìm kiếm ngày đặt đơn hàng, kiểu LocalDateTime trong java
                + status: only FAILED, COMPLETED, PENDING
                + minTotalPrice và maxTotalPrice: phạm vi giá tiền của đơn hàng
                + fieldsToSelect: [id userId orderDate totalPrice status voucherMess cartItemsJson] để bạn có thể 
                    tiết kiệm token, cần select ra trường nào thì cho vào cách nhau bởi dấu phẩy là được
                 
            - Liên quan đến product thì:
                + sortBy: name, price, quantity: only sort theo 3 field này(nếu có sort).
                + orderBy: asc, desc.
                + name: điều kiện tìm kiếm theo tên sản phẩm
                + nameOp: phép so sánh sẽ được thực hiện với name ví dụ: =, !=, like.
                + price: điều kiện tìm kiếm theo giá
                + priceOp: phép so sánh sẽ được thực hiện với price ví dụ: >, <, =.
                + tương tự cho các trường khác...
            
            Nếu câu hỏi KHÔNG liên quan đến orders/products, hãy trả lời trực tiếp câu hỏi:
            {
                "needsData": false,
                "response": "câu trả lời trực tiếp"
            }
            
            Thời gian hiện tại: %s
            """, question, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private boolean needsDataRetrieval(String aiResponse) {
        try {
            log.info("AI Response2: {}", aiResponse);
            String cleanedResponse = cleanJsonResponse(aiResponse);
            JsonNode node = objectMapper.readTree(cleanedResponse);
            log.info("Node: {}", node.toString());
            return node.has("needsData") && node.get("needsData").asBoolean();
        } catch (Exception e) {
            log.error("Error parsing AI response for needsDataRetrieval: {}", e.getMessage());
            return false;
        }
    }

    private String extractDirectResponse(String aiResponse) {
        try {
            // Clean JSON response (remove markdown code blocks)
            String cleanedResponse = cleanJsonResponse(aiResponse);
            JsonNode node = objectMapper.readTree(cleanedResponse);
            if (node.has("response")) {
                return node.get("response").asText();
            }
            return aiResponse;
        } catch (Exception e) {
            return aiResponse;
        }
    }

    private String cleanJsonResponse(String response) {
        // Remove markdown code blocks
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }

    private String executeDataRetrieval(String aiResponse, Long userId) {
        try {
            // Clean JSON response (remove markdown code blocks)
            String cleanedResponse = cleanJsonResponse(aiResponse);
            JsonNode node = objectMapper.readTree(cleanedResponse);
            String type = node.get("type").asText();
            JsonNode parameters = node.get("parameters");

            if ("order".equals(type)) {
                return executeOrderSearch(parameters, userId);
            } else if ("product".equals(type)) {
                return executeProductSearch(parameters);
            }

            return "{}";
        } catch (Exception e) {
            log.error("Error executing data retrieval: {}", e.getMessage());
            return "{}";
        }
    }

    private String executeOrderSearch(JsonNode parameters, Long userId) {
        try {
            // Tạo OrderFilter từ parameters
            OrderFilter filter = new OrderFilter();

            if (parameters.has("userId")) {
                filter.setUserId(userId);
            }
            if (parameters.has("status")) {
                filter.setStatus(parameters.get("status").asText());
            }
            if (parameters.has("fromDate")) {
                String fromDate = parameters.get("fromDate").asText();
                filter.setFromDate(formatDateForService(fromDate));
            }
            if (parameters.has("toDate")) {
                String toDate = parameters.get("toDate").asText();
                filter.setToDate(formatDateForService(toDate));
            }
            if (parameters.has("minTotalPrice")) {
                filter.setMinTotalPrice(parameters.get("minTotalPrice").asDouble());
            }
            if (parameters.has("maxTotalPrice")) {
                filter.setMaxTotalPrice(parameters.get("maxTotalPrice").asDouble());
            }
            if (parameters.has("voucherMess")) {
                filter.setVoucherMess(parameters.get("voucherMess").asText());
            }
            if (parameters.has("fieldsToSelect")) {
                filter.setFieldsToSelect(parameters.get("fieldsToSelect").asText());
            }

            // Gọi orderService.searchOrdersFlexible
            List<Map<String, Object>> result = orderService.searchOrdersFlexible(filter);
            log.info("order result: {}", result);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("Error executing order search: {}", e.getMessage());
            return "{}";
        }
    }

    private String formatDateForService(String dateStr) {
        try {
            // Nếu đã có format đầy đủ thì return nguyên
            if (dateStr.contains("T") || dateStr.length() > 10) {
                return dateStr;
            }

            // Nếu chỉ có yyyy-MM-dd thì thêm thời gian
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return dateStr + "T00:00:00";
            }

            return dateStr;
        } catch (Exception e) {
            log.warn("Error formatting date: {}", dateStr);
            return dateStr;
        }
    }

    private String executeProductSearch(JsonNode parameters) {
        try {
            // Tạo SearchProductRequest từ parameters
            SearchProductRequest request = new SearchProductRequest();

            if (parameters.has("name")) {
                request.setName(parameters.get("name").asText());
            }
            if (parameters.has("nameOp")) {
                request.setNameOp(parameters.get("nameOp").asText());
            }
            if (parameters.has("price")) {
                request.setPrice(parameters.get("price").asDouble());
            }
            if (parameters.has("priceOp")) {
                request.setPriceOp(parameters.get("priceOp").asText());
            }
            if (parameters.has("quantity")) {
                request.setQuantity(parameters.get("quantity").asInt());
            }
            if (parameters.has("quantityOp")) {
                request.setQuantityOp(parameters.get("quantityOp").asText());
            }
            if (parameters.has("sortBy")) {
                request.setSortBy(parameters.get("sortBy").asText());
            }
            if (parameters.has("orderBy")) {
                request.setOrderBy(parameters.get("orderBy").asText());
            }
            if (parameters.has("status")) {
                request.setStatus(parameters.get("status").asBoolean());
            }

            // Gọi productClient.searchProducts
            List<ProductDto> response = productClient.searchProducts(request);
            log.info("Search products response: {}", response);

            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Error executing products search: {}", e.getMessage());
            return "{}";
        }
    }

    private String generateFinalResponse(String originalQuestion, String data) {
        String prompt = String.format("""
            Câu hỏi gốc: "%s"
            
            Dữ liệu đã lấy được:
            %s
            
            Hãy phân tích dữ liệu và trả lời câu hỏi của user một cách chi tiết, rõ ràng.
            Nếu cần tính toán, hãy tính toán chính xác.
            Trả lời bằng tiếng Việt, thân thiện và hữu ích.
            Nếu không có dữ liệu hoặc dữ liệu rỗng, hãy thông báo không tìm thấy kết quả phù hợp.
            """, originalQuestion, data);

        return callGeminiAPI(prompt);
    }

    private String callGeminiAPI(String prompt) {
        try {
            WebClient client = WebClient.builder().build();

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            content.put("role", "user");
            content.put("parts", new Object[]{Map.of("text", prompt)});
            requestBody.put("contents", new Object[]{content});

            String response = client.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse response từ Gemini
            JsonNode responseNode = objectMapper.readTree(response);
            return responseNode.get("candidates").get(0)
                    .get("content").get("parts").get(0)
                    .get("text").asText();

        } catch (Exception e) {
            return "Xin lỗi, tôi không thể xử lý yêu cầu này lúc này.";
        }
    }
}
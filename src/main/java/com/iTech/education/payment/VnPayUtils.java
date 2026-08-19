package com.iTech.education.payment;

import com.iTech.education.config.PaymentProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VnPayUtils {

    public String buildPaymentUrl(Map<String, String> params, PaymentProperties.VnPay vnPay) {
        Map<String, String> data = filterPayable(params);
        String hashData = buildHashData(data);
        String secureHash = hmacSHA512(vnPay.getHashSecret(), hashData);
        String query = data.keySet().stream()
                .sorted()
                .map(key -> encode(key) + "=" + encode(data.get(key)))
                .collect(Collectors.joining("&"));
        return vnPay.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public boolean validateSignature(Map<String, String> params, String hashSecret) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        Map<String, String> copy = params.entrySet().stream()
                .filter(entry -> !"vnp_SecureHash".equals(entry.getKey()))
                .filter(entry -> !"vnp_SecureHashType".equals(entry.getKey()))
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String calculated = hmacSHA512(hashSecret, buildHashData(copy));
        return calculated.equalsIgnoreCase(receivedHash);
    }

    private Map<String, String> filterPayable(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        return fieldNames.stream()
                .map(name -> name + "=" + encode(params.get(name)))
                .collect(Collectors.joining("&"));
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo chữ ký VNPay", ex);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }
}

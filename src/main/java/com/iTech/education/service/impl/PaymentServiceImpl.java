package com.iTech.education.service.impl;

import com.iTech.education.config.PaymentProperties;
import com.iTech.education.dto.request.CreatePaymentRequest;
import com.iTech.education.dto.response.PaymentResponse;
import com.iTech.education.entity.Course;
import com.iTech.education.entity.Transaction;
import com.iTech.education.entity.TransactionDetail;
import com.iTech.education.entity.User;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.payment.VnPayUtils;
import com.iTech.education.repository.CourseRepository;
import com.iTech.education.repository.TransactionRepository;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.service.EnrollmentService;
import com.iTech.education.service.MailService;
import com.iTech.education.service.PaymentService;
import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.RoleType;
import com.iTech.education.utils.TransactionStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final PaymentProperties paymentProperties;
    private final VnPayUtils vnPayUtils;
    private final MailService mailService;

    public PaymentServiceImpl(TransactionRepository transactionRepository,
                              CourseRepository courseRepository,
                              UserRepository userRepository,
                              EnrollmentService enrollmentService,
                              PaymentProperties paymentProperties,
                              VnPayUtils vnPayUtils,
                              MailService mailService) {
        this.transactionRepository = transactionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
        this.paymentProperties = paymentProperties;
        this.vnPayUtils = vnPayUtils;
        this.mailService = mailService;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request, String currentUserEmail, String clientIp) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (user.getRole() != RoleType.STUDENT) {
            throw new AccessDeniedException("Chỉ học viên mới được thanh toán khóa học");
        }

        List<Course> courses = courseRepository.findAllById(request.getCourseIds());
        if (courses.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy khóa học hợp lệ");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setPaymentMethod(resolvePaymentMethod());
        transaction.setTransactionCode(generateTransactionCode());

        for (Course course : courses) {
            if (course.getStatus() != CourseStatus.PUBLISHED) {
                throw new IllegalArgumentException("Khóa học " + course.getTitle() + " chưa được publish");
            }
            if (course.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Khóa học miễn phí không cần thanh toán: " + course.getTitle());
            }

            if (course.getInstructor().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Không thể mua khóa học của chính mình: " + course.getTitle());
            }

            TransactionDetail detail = new TransactionDetail();
            detail.setCourse(course);
            detail.setPrice(course.getPrice());
            transaction.addDetail(detail);
            totalAmount = totalAmount.add(course.getPrice());
        }

        transaction.setTotalAmount(totalAmount);
        Transaction saved = transactionRepository.save(transaction);

        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(saved.getId());
        response.setTransactionCode(saved.getTransactionCode());
        response.setTotalAmount(saved.getTotalAmount());
        response.setStatus(saved.getStatus());

        if ("vnpay".equalsIgnoreCase(paymentProperties.getMode())) {
            response.setPaymentUrl(buildVnPayUrl(saved, clientIp));
        } else {
            completeSuccessfulPayment(saved);
            response.setStatus(TransactionStatus.SUCCESS);
            response.setPaymentUrl(null);
        }

        return response;
    }

    @Override
    @Transactional
    public Map<String, String> handleVnPayIpn(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        String result = processVnPayCallback(params);
        if ("invalid-signature".equals(result)) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid signature");
            return response;
        }
        if ("not-found".equals(result)) {
            response.put("RspCode", "01");
            response.put("Message", "Order not found");
            return response;
        }
        if ("invalid-amount".equals(result)) {
            response.put("RspCode", "04");
            response.put("Message", "Invalid amount");
            return response;
        }
        if ("success".equals(result) || "already".equals(result)) {
            response.put("RspCode", "00");
            response.put("Message", "already".equals(result) ? "Already confirmed" : "Confirm Success");
            return response;
        }
        response.put("RspCode", "01");
        response.put("Message", "Payment failed");
        return response;
    }

    @Override
    @Transactional
    public String handleVnPayReturn(Map<String, String> params) {
        String code = params.getOrDefault("vnp_ResponseCode", "99");
        String txn = params.getOrDefault("vnp_TxnRef", "");
        try {
            processVnPayCallback(params);
        } catch (Exception ignored) {
            code = code.isBlank() ? "99" : code;
        }
        String base = paymentProperties.getVnPay().getFrontendReturnUrl();
        return base
                + "?vnp_ResponseCode=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&vnp_TxnRef=" + URLEncoder.encode(txn, StandardCharsets.UTF_8);
    }

    private String processVnPayCallback(Map<String, String> params) {
        PaymentProperties.VnPay vnPay = paymentProperties.getVnPay();
        if (!vnPayUtils.validateSignature(params, vnPay.getHashSecret())) {
            return "invalid-signature";
        }

        String transactionCode = params.get("vnp_TxnRef");
        Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                .orElse(null);
        if (transaction == null) {
            return "not-found";
        }

        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            return "already";
        }

        String amountRaw = params.get("vnp_Amount");
        if (amountRaw != null) {
            long paid = Long.parseLong(amountRaw);
            long expected = transaction.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
            if (paid != expected) {
                return "invalid-amount";
            }
        }

        if ("00".equals(params.get("vnp_ResponseCode")) || "00".equals(params.get("vnp_TransactionStatus"))) {
            completeSuccessfulPayment(transaction);
            return "success";
        }

        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.save(transaction);
        return "failed";
    }

    private void completeSuccessfulPayment(Transaction transaction) {
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        for (TransactionDetail detail : transaction.getDetails()) {
            try {
                enrollmentService.createEnrollmentForUser(
                        transaction.getUser().getId(),
                        detail.getCourse().getId()
                );
            } catch (Exception ignored) {
                // đã enroll rồi thì bỏ qua
            }
        }

        sendPaymentMail(transaction);
    }

    private void sendPaymentMail(Transaction transaction) {
        try {
            User buyer = transaction.getUser();
            List<String> titles = transaction.getDetails().stream()
                    .map(detail -> detail.getCourse().getTitle())
                    .toList();
            Long firstCourseId = transaction.getDetails().isEmpty()
                    ? null
                    : transaction.getDetails().get(0).getCourse().getId();
            mailService.sendPaymentSuccess(
                    buyer.getEmail(),
                    buyer.getFullName(),
                    titles,
                    transaction.getTotalAmount(),
                    firstCourseId
            );
        } catch (Exception ignored) {
            // không chặn thanh toán nếu gửi mail lỗi
        }
    }

    private String resolvePaymentMethod() {
        return "vnpay".equalsIgnoreCase(paymentProperties.getMode()) ? "VNPAY" : "MOCK";
    }

    private String generateTransactionCode() {
        return "EDU" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String buildVnPayUrl(Transaction transaction, String clientIp) {
        PaymentProperties.VnPay vnPay = paymentProperties.getVnPay();
        if (vnPay.getTmnCode() == null || vnPay.getTmnCode().isBlank()
                || vnPay.getHashSecret() == null || vnPay.getHashSecret().isBlank()) {
            throw new IllegalStateException("Chưa cấu hình VNPAY_TMN_CODE / VNPAY_HASH_SECRET. Xem education/.env.example");
        }

        String ip = (clientIp == null || clientIp.isBlank()) ? "127.0.0.1" : clientIp;
        if ("https://example.net/id/garnet".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        LocalDateTime now = LocalDateTime.now();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPay.getTmnCode());
        params.put("vnp_Amount", String.valueOf(transaction.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", transaction.getTransactionCode());
        params.put("vnp_OrderInfo", "Thanh toan khoa hoc " + transaction.getTransactionCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPay.getReturnUrl());
        params.put("vnp_IpAddr", ip);
        params.put("vnp_CreateDate", now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        return vnPayUtils.buildPaymentUrl(params, vnPay);
    }
}

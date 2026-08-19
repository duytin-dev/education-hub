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
import com.iTech.education.service.PaymentService;
import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.RoleType;
import com.iTech.education.utils.TransactionStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    public PaymentServiceImpl(TransactionRepository transactionRepository,
                              CourseRepository courseRepository,
                              UserRepository userRepository,
                              EnrollmentService enrollmentService,
                              PaymentProperties paymentProperties,
                              VnPayUtils vnPayUtils) {
        this.transactionRepository = transactionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
        this.paymentProperties = paymentProperties;
        this.vnPayUtils = vnPayUtils;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request, String currentUserEmail) {
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
            response.setPaymentUrl(buildVnPayUrl(saved));
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

        PaymentProperties.VnPay vnPay = paymentProperties.getVnPay();
        if (!vnPayUtils.validateSignature(params, vnPay.getHashSecret())) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid signature");
            return response;
        }

        String transactionCode = params.get("vnp_TxnRef");
        Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch"));

        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            response.put("RspCode", "00");
            response.put("Message", "Already confirmed");
            return response;
        }

        if ("00".equals(params.get("vnp_ResponseCode"))) {
            completeSuccessfulPayment(transaction);
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            response.put("RspCode", "01");
            response.put("Message", "Payment failed");
        }

        return response;
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
    }

    private String resolvePaymentMethod() {
        return "vnpay".equalsIgnoreCase(paymentProperties.getMode()) ? "VNPAY" : "MOCK";
    }

    private String generateTransactionCode() {
        return "EDU" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String buildVnPayUrl(Transaction transaction) {
        PaymentProperties.VnPay vnPay = paymentProperties.getVnPay();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPay.getTmnCode());
        params.put("vnp_Amount", String.valueOf(transaction.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", transaction.getTransactionCode());
        params.put("vnp_OrderInfo", "Thanh toan khoa hoc");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPay.getReturnUrl());
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        return vnPayUtils.buildPaymentUrl(params, vnPay);
    }
}

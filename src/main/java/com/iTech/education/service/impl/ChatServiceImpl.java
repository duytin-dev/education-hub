package com.iTech.education.service.impl;

import com.iTech.education.dto.request.ChatMessageRequest;
import com.iTech.education.dto.request.StartChatRequest;
import com.iTech.education.dto.response.ChatMessageResponse;
import com.iTech.education.dto.response.ConversationResponse;
import com.iTech.education.entity.ChatMessage;
import com.iTech.education.entity.Conversation;
import com.iTech.education.entity.Course;
import com.iTech.education.entity.User;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.ChatMessageRepository;
import com.iTech.education.repository.ConversationRepository;
import com.iTech.education.repository.CourseRepository;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.service.ChatService;
import com.iTech.education.utils.ChatSenderType;
import com.iTech.education.utils.ConversationStatus;
import com.iTech.education.utils.RoleType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String WELCOME =
            "Xin chào! LearnHub sẵn sàng tư vấn khóa học, học phí và cách đăng ký. Bạn muốn hỏi gì?";

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public ChatServiceImpl(ConversationRepository conversationRepository,
                           ChatMessageRepository chatMessageRepository,
                           UserRepository userRepository,
                           CourseRepository courseRepository) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public ConversationResponse start(StartChatRequest request, String currentUserEmail) {
        User user = findVisitor(currentUserEmail);
        Conversation conversation = resolveConversation(request.getGuestToken(), user);

        if (conversation == null) {
            if (user == null) {
                if (!StringUtils.hasText(request.getGuestName()) || !StringUtils.hasText(request.getGuestEmail())) {
                    throw new IllegalArgumentException("Vui lòng nhập họ tên và email để bắt đầu chat");
                }
            }
            conversation = new Conversation();
            conversation.setGuestToken(UUID.randomUUID().toString());
            conversation.setStatus(ConversationStatus.OPEN);
            conversation.setUnreadForStaff(0);
            conversation.setUnreadForVisitor(0);
            if (user != null) {
                conversation.setUser(user);
                conversation.setGuestName(user.getFullName());
                conversation.setGuestEmail(user.getEmail());
            } else {
                conversation.setGuestName(request.getGuestName().trim());
                conversation.setGuestEmail(request.getGuestEmail().trim());
            }
            attachCourse(conversation, request.getCourseId());
            conversation = conversationRepository.save(conversation);
            addMessage(conversation, ChatSenderType.SYSTEM, WELCOME, null, false);
        } else if (conversation.getStatus() == ConversationStatus.CLOSED) {
            conversation.setStatus(ConversationStatus.OPEN);
        }

        addMessage(conversation, ChatSenderType.VISITOR, request.getContent().trim(), null, true);
        return toDetail(conversation, true, false);
    }

    @Override
    @Transactional
    public ConversationResponse getMine(String guestToken, String currentUserEmail) {
        User user = findVisitor(currentUserEmail);
        Conversation conversation = resolveConversation(guestToken, user);
        if (conversation == null) {
            return null;
        }
        conversation.setUnreadForVisitor(0);
        conversationRepository.save(conversation);
        return toDetail(conversation, true, false);
    }

    @Override
    @Transactional
    public ConversationResponse sendVisitorMessage(ChatMessageRequest request, String guestToken, String currentUserEmail) {
        User user = findVisitor(currentUserEmail);
        Conversation conversation = resolveConversation(guestToken, user);
        if (conversation == null) {
            throw new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện");
        }
        if (conversation.getStatus() == ConversationStatus.CLOSED) {
            conversation.setStatus(ConversationStatus.OPEN);
        }
        addMessage(conversation, ChatSenderType.VISITOR, request.getContent().trim(), null, true);
        return toDetail(conversation, true, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> listForStaff() {
        return conversationRepository.findAllForStaff().stream()
                .map(item -> {
                    ConversationResponse response = ConversationResponse.fromEntity(item, false);
                    List<ChatMessage> messages = chatMessageRepository
                            .findByConversationIdOrderByCreatedAtAsc(item.getId());
                    if (!messages.isEmpty()) {
                        response.setLastMessagePreview(messages.get(messages.size() - 1).getContent());
                    }
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional
    public ConversationResponse getForStaff(Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));
        conversation.setUnreadForStaff(0);
        conversationRepository.save(conversation);
        return toDetail(conversation, false, true);
    }

    @Override
    @Transactional
    public ConversationResponse replyAsStaff(Long id, ChatMessageRequest request, String staffEmail) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));
        User staff = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));
        if (conversation.getStatus() == ConversationStatus.CLOSED) {
            conversation.setStatus(ConversationStatus.OPEN);
        }
        addMessage(conversation, ChatSenderType.STAFF, request.getContent().trim(), staff, false);
        conversation.setUnreadForVisitor((conversation.getUnreadForVisitor() == null ? 0 : conversation.getUnreadForVisitor()) + 1);
        conversationRepository.save(conversation);
        return toDetail(conversation, false, true);
    }

    @Override
    @Transactional
    public ConversationResponse close(Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));
        conversation.setStatus(ConversationStatus.CLOSED);
        addMessage(conversation, ChatSenderType.SYSTEM, "Cuộc trò chuyện đã kết thúc. Bạn vẫn có thể gửi tin để mở lại.", null, false);
        return toDetail(conversation, false, true);
    }

    @Override
    public long unreadForStaff() {
        return conversationRepository.countByUnreadForStaffGreaterThan(0);
    }

    private User findVisitor(String currentUserEmail) {
        if (!StringUtils.hasText(currentUserEmail)) {
            return null;
        }
        return userRepository.findByEmail(currentUserEmail)
                .filter(user -> user.getRole() == RoleType.STUDENT)
                .orElse(null);
    }

    private Conversation resolveConversation(String guestToken, User user) {
        if (user != null) {
            return conversationRepository
                    .findFirstByUser_IdAndStatusOrderByLastMessageAtDesc(user.getId(), ConversationStatus.OPEN)
                    .or(() -> conversationRepository.findFirstByUser_IdAndStatusOrderByLastMessageAtDesc(user.getId(), ConversationStatus.CLOSED))
                    .orElse(null);
        }
        if (StringUtils.hasText(guestToken)) {
            return conversationRepository.findByGuestToken(guestToken).orElse(null);
        }
        return null;
    }

    private void attachCourse(Conversation conversation, Long courseId) {
        if (courseId == null) {
            return;
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));
        conversation.setCourse(course);
    }

    private void addMessage(Conversation conversation,
                            ChatSenderType senderType,
                            String content,
                            User staff,
                            boolean fromVisitor) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setStaff(staff);
        chatMessageRepository.save(message);
        conversation.setLastMessageAt(LocalDateTime.now());
        if (fromVisitor) {
            conversation.setUnreadForStaff((conversation.getUnreadForStaff() == null ? 0 : conversation.getUnreadForStaff()) + 1);
            conversation.setUnreadForVisitor(0);
        }
        conversationRepository.save(conversation);
    }

    private ConversationResponse toDetail(Conversation conversation, boolean includeToken, boolean forStaff) {
        ConversationResponse response = ConversationResponse.fromEntity(conversation, includeToken);
        List<ChatMessageResponse> messages = chatMessageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .map(ChatMessageResponse::fromEntity)
                .toList();
        response.setMessages(messages);
        if (!messages.isEmpty()) {
            response.setLastMessagePreview(messages.get(messages.size() - 1).getContent());
        }
        if (forStaff) {
            response.setGuestToken(null);
        }
        return response;
    }
}

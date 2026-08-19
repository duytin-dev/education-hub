package com.iTech.education.websocket;

import com.iTech.education.security.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class ChatAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public ChatAuthChannelInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String jwt = firstHeader(accessor, "Authorization");
        if (StringUtils.hasText(jwt) && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        if (!StringUtils.hasText(jwt)) {
            jwt = sessionValue(accessor, "token");
        }

        String chatToken = firstHeader(accessor, "X-Chat-Token");
        if (!StringUtils.hasText(chatToken)) {
            chatToken = sessionValue(accessor, "chatToken");
        }

        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            String email = jwtTokenProvider.getEmailFromJwt(jwt);
            boolean staff = false;
            try {
                List<String> roles = jwtTokenProvider.getRolesFromJwt(jwt);
                staff = roles != null && roles.stream().anyMatch(role ->
                        String.valueOf(role).contains("ADMIN") || String.valueOf(role).contains("INSTRUCTOR"));
            } catch (Exception ignored) {
                staff = false;
            }
            accessor.setUser(new ChatPrincipal(email, false, staff));
        } else if (StringUtils.hasText(chatToken)) {
            accessor.setUser(new ChatPrincipal(chatToken, true, false));
        } else {
            accessor.setUser(new ChatPrincipal("anonymous", true, false));
        }
        return message;
    }

    private String firstHeader(StompHeaderAccessor accessor, String name) {
        String value = accessor.getFirstNativeHeader(name);
        if (StringUtils.hasText(value)) {
            return value;
        }
        return accessor.getFirstNativeHeader(name.toLowerCase());
    }

    private String sessionValue(StompHeaderAccessor accessor, String key) {
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs == null) {
            return null;
        }
        Object value = attrs.get(key);
        return value == null ? null : String.valueOf(value);
    }
}

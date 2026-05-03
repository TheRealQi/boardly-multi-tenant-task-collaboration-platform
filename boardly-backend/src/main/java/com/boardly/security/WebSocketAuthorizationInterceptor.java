package com.boardly.security;

import com.boardly.security.model.AppUserDetails;
import com.boardly.service.AuthorizationSecurityService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WebSocketAuthorizationInterceptor implements ChannelInterceptor {

    private final AuthorizationSecurityService authorizationSecurityService;

    public WebSocketAuthorizationInterceptor(AuthorizationSecurityService authorizationSecurityService) {
        this.authorizationSecurityService = authorizationSecurityService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Authentication user = (Authentication) accessor.getUser();
            
            if (user != null) {
                SecurityContextHolder.getContext().setAuthentication(user);
            }

            try {
                String destination = accessor.getDestination();
                if (destination != null) {
                    if (destination.startsWith("/user/")) {
                        if (user == null) {
                            throw new SecurityException("User must be authenticated to subscribe to user topics");
                        }
                        if (user.getPrincipal() instanceof AppUserDetails userDetails) {
                            String userId = userDetails.getUserId().toString();
                            
                            // Check if the user is subscribing to their own queue
                            // The destination will be something like /user/queue/invites
                            // Or /user/{userId}/queue/invites if fully qualified
                            
                            boolean isGenericUserQueue = destination.startsWith("/user/queue/") || destination.startsWith("/user/topic/");
                            boolean isSpecificUserQueue = destination.contains(userId);
                            
                            if (!isGenericUserQueue && !isSpecificUserQueue) {
                                throw new SecurityException("You are not authorized to subscribe to this user's topic");
                            }
                        }
                    } else if (destination.startsWith("/topic/board/")) {
                        String boardIdStr = destination.substring("/topic/board/".length());
                        UUID boardId = UUID.fromString(boardIdStr);
                        authorizationSecurityService.canViewBoard(boardId);
                    } else if (destination.startsWith("/topic/workspace/")) {
                        String workspaceIdStr = destination.substring("/topic/workspace/".length());
                        UUID workspaceId = UUID.fromString(workspaceIdStr);
                        authorizationSecurityService.isWorkspaceMember(workspaceId);
                    }
                }
            } catch (IllegalArgumentException e) {
                // Handle invalid UUID format or other parsing errors
                throw new SecurityException("Invalid destination format");
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
        return message;
    }
}

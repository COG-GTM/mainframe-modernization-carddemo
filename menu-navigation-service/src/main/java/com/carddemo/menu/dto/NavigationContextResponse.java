package com.carddemo.menu.dto;

import com.carddemo.menu.entity.NavigationContext;
import java.time.LocalDateTime;

/**
 * DTO for returning the current navigation context for a session.
 */
public record NavigationContextResponse(
        String sessionId,
        String userId,
        String userType,
        String fromTranid,
        String fromProgram,
        String toTranid,
        String toProgram,
        int pgmContext,
        LocalDateTime lastUpdated
) {

    public static NavigationContextResponse from(NavigationContext ctx) {
        return new NavigationContextResponse(
                ctx.getSessionId(),
                ctx.getUserId(),
                ctx.getUserType(),
                ctx.getFromTranid(),
                ctx.getFromProgram(),
                ctx.getToTranid(),
                ctx.getToProgram(),
                ctx.getPgmContext(),
                ctx.getLastUpdated()
        );
    }
}

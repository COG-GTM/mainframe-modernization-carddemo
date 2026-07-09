package com.carddemo.session.web;

import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.ProgramRegistry;

/**
 * A snapshot of the {@link CardDemoCommarea} routing state returned after each navigation
 * turn — the REST analogue of the COMMAREA a CICS program would leave for the next turn.
 */
public record NavigationResponse(
        String fromTranId,
        String fromProgram,
        String toTranId,
        String toProgram,
        String currentProgram,
        String currentFunction,
        String userId,
        String userType,
        String context,
        boolean enter,
        String acctId,
        String cardNum,
        String custId,
        String message,
        Object model) {

    public static NavigationResponse from(CardDemoCommarea commarea, ProgramRegistry registry,
            String message, Object model) {
        CardDemoProgram current = registry.byProgramName(commarea.getToProgram()).orElse(null);
        return new NavigationResponse(
            commarea.getFromTranId(),
            commarea.getFromProgram(),
            commarea.getToTranId(),
            commarea.getToProgram(),
            current == null ? null : current.name(),
            current == null ? null : current.function(),
            commarea.getUserId(),
            commarea.getUserType() == null ? null : commarea.getUserType().name(),
            commarea.getProgramContext().name(),
            commarea.isEnter(),
            commarea.getAccountInfo().getAcctId(),
            commarea.getCardInfo().getCardNum(),
            commarea.getCustomerInfo().getCustId(),
            message,
            model);
    }
}

package com.carddemo.session;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * Collects every {@link ScreenHandler} bean and indexes it by {@link CardDemoProgram}, so
 * the {@link NavigationController} can dispatch a navigation turn to the handler that owns
 * the program currently in control. WAVE 3 waves plug in simply by declaring their handler
 * as a Spring bean — no change to this framework is required.
 */
@Component
public class ScreenRegistry {

    private final Map<CardDemoProgram, ScreenHandler> handlers = new EnumMap<>(CardDemoProgram.class);

    public ScreenRegistry(List<ScreenHandler> discovered) {
        for (ScreenHandler handler : discovered) {
            ScreenHandler existing = handlers.putIfAbsent(handler.program(), handler);
            if (existing != null) {
                throw new IllegalStateException(
                    "Duplicate ScreenHandler for program " + handler.program()
                        + ": " + existing.getClass().getName()
                        + " and " + handler.getClass().getName());
            }
        }
    }

    public Optional<ScreenHandler> forProgram(CardDemoProgram program) {
        return Optional.ofNullable(handlers.get(program));
    }

    public boolean hasHandler(CardDemoProgram program) {
        return handlers.containsKey(program);
    }
}

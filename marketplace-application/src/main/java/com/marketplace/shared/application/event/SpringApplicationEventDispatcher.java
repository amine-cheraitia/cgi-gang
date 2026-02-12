package com.marketplace.shared.application.event;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringApplicationEventDispatcher implements ApplicationEventDispatcher {
    private final List<ApplicationEventHandler<? extends ApplicationEvent>> handlers;

    public SpringApplicationEventDispatcher(List<ApplicationEventHandler<? extends ApplicationEvent>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void dispatch(ApplicationEvent event) {
        handlers.stream()
            .filter(handler -> handler.supports(event))
            .forEach(handler -> handleUnchecked(handler, event));
    }

    @SuppressWarnings("unchecked")
    private <T extends ApplicationEvent> void handleUnchecked(ApplicationEventHandler<?> handler, ApplicationEvent event) {
        ((ApplicationEventHandler<T>) handler).handle((T) event);
    }
}

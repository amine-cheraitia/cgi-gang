package com.marketplace.shared.application.event;

public interface ApplicationEventHandler<T extends ApplicationEvent> {
    boolean supports(ApplicationEvent event);

    void handle(T event);
}

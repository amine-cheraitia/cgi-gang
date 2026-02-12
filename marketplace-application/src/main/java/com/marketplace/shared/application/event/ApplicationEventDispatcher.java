package com.marketplace.shared.application.event;

public interface ApplicationEventDispatcher {
    void dispatch(ApplicationEvent event);
}

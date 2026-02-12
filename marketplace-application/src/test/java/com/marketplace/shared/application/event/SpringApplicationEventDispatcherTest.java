package com.marketplace.shared.application.event;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SpringApplicationEventDispatcherTest {

    @Test
    void shouldDispatchOnlyToSupportingHandlers() {
        AtomicInteger handled = new AtomicInteger();
        ApplicationEvent event = new TestEvent();
        ApplicationEventHandler<TestEvent> supporting = new ApplicationEventHandler<>() {
            @Override
            public boolean supports(ApplicationEvent candidate) {
                return candidate instanceof TestEvent;
            }

            @Override
            public void handle(TestEvent ignored) {
                handled.incrementAndGet();
            }
        };
        ApplicationEventHandler<TestEvent> nonSupporting = new ApplicationEventHandler<>() {
            @Override
            public boolean supports(ApplicationEvent candidate) {
                return false;
            }

            @Override
            public void handle(TestEvent ignored) {
                handled.incrementAndGet();
            }
        };

        SpringApplicationEventDispatcher dispatcher = new SpringApplicationEventDispatcher(List.of(supporting, nonSupporting));

        dispatcher.dispatch(event);

        assertThat(handled.get()).isEqualTo(1);
    }

    private record TestEvent() implements ApplicationEvent {
    }
}

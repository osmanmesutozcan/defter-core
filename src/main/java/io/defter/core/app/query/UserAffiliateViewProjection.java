package io.defter.core.app.query;

import io.defter.core.app.api.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@XSlf4j
@RequiredArgsConstructor
@Profile("query")
@ProcessingGroup("query")
public class UserAffiliateViewProjection {
    private final EntityManager entityManager;

    @EventHandler
    public void on(ExpenseGroupCreated event) {
        log.debug("projecting {}", event);
        List<String> members = event.getMembers();
        members.forEach(member -> {
            List<String> friends = members
                    .stream()
                    .filter(m -> !member.contains(m))
                    .collect(Collectors.toList());

            friends.stream().parallel().forEach(friend -> {
                String id = UUID.randomUUID().toString();
                entityManager.persist(new UserAffiliateView(id, member, friend));
            });
        });
    }
}

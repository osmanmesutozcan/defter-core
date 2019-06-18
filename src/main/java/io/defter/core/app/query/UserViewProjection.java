package io.defter.core.app.query;

import io.defter.core.app.api.FetchUserViewsByIds;
import io.defter.core.app.api.FetchUserViewsQuery;
import io.defter.core.app.api.UserCreated;
import io.defter.core.app.api.UserView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Component
@XSlf4j
@RequiredArgsConstructor
@Profile("query")
@ProcessingGroup("query")
public class UserViewProjection {
    private final EntityManager entityManager;

    @EventHandler
    public void on(UserCreated event) {
        log.debug("projecting {}", event);
        entityManager.persist(new UserView(event.getId(), event.getUsername(), ""));
    }

    @QueryHandler
    public List<UserView> handle(FetchUserViewsQuery query) {
        log.trace("handling {}", query);
        TypedQuery<UserView> jpaQuery = entityManager.createNamedQuery("UserView.fetch", UserView.class);
        jpaQuery.setParameter("usernameStartsWith", query.getFilter().getUsernameStartsWith());
        jpaQuery.setFirstResult(query.getOffset());
        jpaQuery.setMaxResults(query.getLimit());
        return log.exit(jpaQuery.getResultList());
    }

    @QueryHandler
    public List<UserView> handle(FetchUserViewsByIds query) {
        log.trace("handling {}", query);
        TypedQuery<UserView> jpaQuery = entityManager.createNamedQuery("UserView.fetchWhereIdIn", UserView.class);
        jpaQuery.setParameter("idsList", query.getIds());
        return log.exit(jpaQuery.getResultList());
    }
}

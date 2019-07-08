package io.defter.core.app.user.query;

import io.defter.core.app.api.*;
import io.defter.core.app.user.UserConstants;
import java.util.stream.Collectors;
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

@XSlf4j
@Component
@Profile("query")
@RequiredArgsConstructor
@ProcessingGroup(UserConstants.USER_PROCESSOR)
public class UserViewProjection {

  private final EntityManager entityManager;

  @EventHandler
  public void on(UserCreated event) {
    log.debug("projecting {}", event);
    entityManager.persist(new UserView(event.getId(), event.getName(), event.getEmail(), event.getPasswordHash(),
        "https://api.adorable.io/avatars/" + event.getName(), event.isVerifiedUser()));
  }

  @QueryHandler
  public List<UserView> handle(FetchUserViewsQuery query) {
    log.trace("handling {}", query);
    TypedQuery<UserView> jpaQuery = entityManager
        .createNamedQuery("UserView.fetch", UserView.class)
        .setParameter("usernameStartsWith", query.getFilter().getUsernameStartsWith())
        .setFirstResult(query.getOffset())
        .setMaxResults(query.getLimit());

    return log.exit(jpaQuery.getResultList());
  }

  @QueryHandler
  public List<UserView> handle(FetchUserViewsByIds query) {
    log.trace("handling {}", query);
    TypedQuery<UserView> jpaQuery = entityManager
        .createNamedQuery("UserView.fetchWhereIdIn", UserView.class)
        .setParameter("idsList", query.getIds());

    return log.exit(jpaQuery.getResultList());
  }

  @QueryHandler
  public UserView handle(FetchUserViewById query) {
    log.trace("handling {}", query);
    return log.exit(entityManager.find(UserView.class, query.getId()));
  }

  @QueryHandler
  public UserView handle(FetchUserViewByEmail query) {
    log.trace("handling {}", query);
    TypedQuery<UserView> jpaQuery = entityManager
        .createNamedQuery("UserView.fetchByEmail", UserView.class)
        .setParameter("email", query.getEmail());
    return log.exit(jpaQuery.getSingleResult());
  }

  @QueryHandler
  public List<UserView> on(FetchUserAffiliatesQuery query) {
    log.trace("handling {}", query);
    TypedQuery<UserAffiliateView> affiliatesQuery = entityManager
        .createNamedQuery("UserAffiliateView.fetchByUserId", UserAffiliateView.class)
        .setParameter("userId", query.getUserId());

    List<String> affiliates = affiliatesQuery.getResultList()
        .stream()
        .map(UserAffiliateView::getFriendId)
        .collect(Collectors.toList());

    if (affiliates.size() == 0) {
      return log.exit(List.of());
    }

    TypedQuery<UserView> usersQuery = entityManager
        .createNamedQuery("UserView.fetchWhereIdIn", UserView.class)
        .setParameter("idsList", affiliates);

    return log.exit(usersQuery.getResultList());
  }
}

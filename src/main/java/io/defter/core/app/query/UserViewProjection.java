package io.defter.core.app.query;

import io.defter.core.app.api.*;
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
import java.util.UUID;
import java.util.stream.Collectors;

@XSlf4j
@Component
@Profile("query")
@RequiredArgsConstructor
@ProcessingGroup(QueryConstants.USER_PROCCESSOR)
public class UserViewProjection {

  private final EntityManager entityManager;

  @EventHandler
  public void on(UserCreated event) {
    log.debug("projecting {}", event);
    entityManager.persist(new UserView(event.getId(), event.getUsername(), ""));
  }

  @EventHandler
  public void on(ExpenseGroupCreated event) {
    log.debug("projecting {}", event);

    List<String> members = event.getMembers();
    members.forEach(member -> {
      List<String> friends = members
          .stream()
          .filter(m -> !member.contains(m))
          .collect(Collectors.toList());

      friends.forEach(friend -> saveAffiliation(member, friend));
    });
  }

  private void saveAffiliation(String member, String friend) {
    // Here we cannot use class level entityManager because we want to catch the
    // unique contraint error.
    String id = UUID.randomUUID().toString();

    TypedQuery<Long> jpaQuery = entityManager
        .createNamedQuery("UserAffiliateView.exists", Long.class);
    jpaQuery.setParameter("userId", member);
    jpaQuery.setParameter("friendId", friend);

    if (jpaQuery.getSingleResult().intValue() > 0) {
      log.warn("skipped affiliate relation from {} to {}", member, friend);
      return;
    }

    entityManager.persist(new UserAffiliateView(id, member, friend));
  }

  @QueryHandler
  public List<UserView> handle(FetchUserViewsQuery query) {
    log.trace("handling {}", query);
    TypedQuery<UserView> jpaQuery = entityManager
        .createNamedQuery("UserView.fetch", UserView.class);
    jpaQuery.setParameter("usernameStartsWith", query.getFilter().getUsernameStartsWith());
    jpaQuery.setFirstResult(query.getOffset());
    jpaQuery.setMaxResults(query.getLimit());
    return log.exit(jpaQuery.getResultList());
  }

  @QueryHandler
  public List<UserView> handle(FetchUserViewsByIds query) {
    log.trace("handling {}", query);
    TypedQuery<UserView> jpaQuery = entityManager
        .createNamedQuery("UserView.fetchWhereIdIn", UserView.class);
    jpaQuery.setParameter("idsList", query.getIds());
    return log.exit(jpaQuery.getResultList());
  }

  @QueryHandler
  public UserView handle(FetchUserViewById query) {
    log.trace("handling {}", query);
    TypedQuery<UserView> jpaQuery = entityManager
        .createNamedQuery("UserView.fetchById", UserView.class);
    jpaQuery.setParameter("userId", query.getId());
    return log.exit(jpaQuery.getSingleResult());
  }

  @QueryHandler
  public List<SplitView> handle(FetchExpenseGroupsSplitsQuery query) {
    log.trace("handling {}", query);
    TypedQuery<SplitView> jpaQuery = entityManager
        .createNamedQuery("SplitView.fetch", SplitView.class);
    jpaQuery.setParameter("groupId", query.getGroupId());
    return log.exit(jpaQuery.getResultList());
  }

  @QueryHandler
  public List<UserView> on(FetchUserAffiliatesQuery query) {
    log.trace("handling {}", query);
    TypedQuery<UserAffiliateView> affiliatesQuery = entityManager
        .createNamedQuery("UserAffiliateView.fetchByUserId", UserAffiliateView.class);
    affiliatesQuery.setParameter("userId", query.getUserId());

    List<String> affiliates = affiliatesQuery.getResultList()
        .stream()
        .map(UserAffiliateView::getFriendId)
        .collect(Collectors.toList());

    TypedQuery<UserView> usersQuery = entityManager
        .createNamedQuery("UserView.fetchWhereIdIn", UserView.class);
    usersQuery.setParameter("idsList", affiliates);

    return log.exit(usersQuery.getResultList());
  }
}

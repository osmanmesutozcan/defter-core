package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import io.defter.core.app.api.*;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@XSlf4j
@Component
@RequiredArgsConstructor
public class Query implements GraphQLQueryResolver {

  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;
  private final EntityManager entityManager;

  public List<ExpenseGroupView> groups() {
    // TODO: Get only groups of current user
    FetchExpenseGroupViewsQuery query = new FetchExpenseGroupViewsQuery(0, 50, new ExpenseGroupViewFilter(""));
    return queryGateway
        .query(query, ResponseTypes.multipleInstancesOf(ExpenseGroupView.class))
        .join();
  }

  public List<UserView> affiliations() {
    String currentUserId = getCurrentUser().getId();
    FetchUserAffiliatesQuery query = new FetchUserAffiliatesQuery(currentUserId);
    return queryGateway
        .query(query, ResponseTypes.multipleInstancesOf(UserView.class))
        .join();
  }

  public List<SplitView> splits(String groupId) {
    FetchExpenseGroupsSplitsQuery query = new FetchExpenseGroupsSplitsQuery(groupId);
    return queryGateway
        .query(query, ResponseTypes.multipleInstancesOf(SplitView.class))
        .join();
  }

  public SettlementResult settlement(String groupId) {
    String currentUserId = getCurrentUser().getId();
    FetchExpenseGroupSettlementQuery query = new FetchExpenseGroupSettlementQuery(
        currentUserId, groupId);

    SettlementView settlement = queryGateway
        .query(query, ResponseTypes.instanceOf(SettlementView.class))
        .join();

    return new SettlementResult(0, settlement.getBalances());
  }

  public List<ExpenseGroupInvitationView> invitations() {
    String currentUserId = getCurrentUser().getId();
    FetchInvitationsOfUser query = new FetchInvitationsOfUser(currentUserId);

    return queryGateway
        .query(query, ResponseTypes.multipleInstancesOf(ExpenseGroupInvitationView.class))
        .join();
  }

  public ExpenseGroupMember groupMemberByEmail(String email) {
    UserView user = getOrCreateUser(email);
    return new ExpenseGroupMember(user.getId(), user.getEmail());
  }

  private UserView getCurrentUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return ((UserView) principal);
  }

  // TODO: this should be moved into a repo
  private UserView getOrCreateUser(String email) {
    TypedQuery<Long> jpaQuery = entityManager
        .createNamedQuery("UserView.existsByEmail", Long.class)
        .setParameter("email", email);

    if (jpaQuery.getSingleResult().intValue() > 0) {
      return entityManager
          .createNamedQuery("UserView.fetchByEmail", UserView.class)
          .setParameter("email", email)
          .getSingleResult();
    }

    String id = UUID.randomUUID().toString();
    // Creates new anonymous user that will be replaced when this user logged in first time.
    CreateUser createUser = new CreateUser(id, "", email, "");
    // Invite this user to the application
    SendMemberInvitation sendMemberInvitation = new SendMemberInvitation("INV:" + id, id, email);

    commandGateway.sendAndWait(createUser);
    // TODO: Maybe we should let a saga manage this user invitation logic.
    // We could add a flag to user view like 'isAnanymous' and pick that up in saga.
    commandGateway.sendAndWait(sendMemberInvitation);

    return new UserView(id, "", email, "", "");
  }

  @Data
  @AllArgsConstructor
  public class SettlementResult {

    public Integer totalSplits;
    public List<SettlementBalance> members;
  }
}

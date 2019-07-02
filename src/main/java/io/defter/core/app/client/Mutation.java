package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.GraphQLException;
import io.defter.core.app.api.*;
import io.defter.core.app.saga.ExpenseGroupInvitationManagement.InvitationAnswer;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Mutation implements GraphQLMutationResolver {

  private final QueryGateway queryGateway;
  private final CommandGateway commandGateway;
  private final EntityManager entityManager;

  public String createUser(String name, String email, String password) {
    String id = UUID.randomUUID().toString();
    CreateUser command = new CreateUser(id, name, email, password);

    if (userExists(email)) {
      throw new GraphQLException("User already exists");
    }

    return commandGateway.sendAndWait(command);
  }

  public AuthPayload login(String email, String password) {
    if (!userExists(email)) {
      throw new GraphQLException("User does not exist");
    }

    FetchUserViewByEmail query = new FetchUserViewByEmail(email);
    UserView user = queryGateway.query(query, ResponseTypes.instanceOf(UserView.class)).join();
    return new AuthPayload(user, "");
  }

  public String createGroup(String name, Currency currency, List<ExpenseGroupMember> members) {
    String id = UUID.randomUUID().toString();
    CreateExpenseGroup command = new CreateExpenseGroup(id, name, currency, members);
    return commandGateway.sendAndWait(command);
  }

  public String answerToGroupInvitation(String invitationRequestId, InvitationAnswer answer) {
    AnswerExpenseGroupInvitation command = new AnswerExpenseGroupInvitation(invitationRequestId, "", answer);
    commandGateway.sendAndWait(command);
    return "DONE";
  }

  public String addSplitToGroup(String groupId, String payedBy, Double total, String description,
      List<SplitMember> members) {
    Date createdAt = new Date();
    AddSplitToGroup command = new AddSplitToGroup(groupId, total, payedBy, description, "",
        createdAt, members);
    commandGateway.sendAndWait(command);
    return groupId;
  }

  // TODO: We are going to directly query the user projection in order to make this check a little easier.
  private Boolean userExists(String email) {
    TypedQuery<Long> jpaQuery = entityManager
        .createNamedQuery("UserView.existsByEmail", Long.class);
    jpaQuery.setParameter("email", email);

    return jpaQuery.getSingleResult().intValue() > 0;
  }

  @Data
  @AllArgsConstructor
  public static class AuthPayload {

    private final UserView me;
    private final String token;
  }
}

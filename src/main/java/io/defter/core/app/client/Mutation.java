package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import graphql.servlet.GraphQLContext;
import io.defter.core.app.api.*;
import io.defter.core.app.saga.ExpenseGroupInvitationManagement.InvitationAnswer;
import io.defter.core.app.security.Unsecured;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@XSlf4j
@Component
@RequiredArgsConstructor
public class Mutation implements GraphQLMutationResolver {

  private final QueryGateway queryGateway;
  private final CommandGateway commandGateway;
  private final EntityManager entityManager;

  @Unsecured
  public String createUser(String name, String email, String password) {
    String id = UUID.randomUUID().toString();
    CreateUser command = new CreateUser(id, name, email, password);

    if (userExists(email)) {
      throw new GraphQLException("User already exists");
    }

    return commandGateway.sendAndWait(command);
  }

  @Unsecured
  public AuthPayload login(String email, String password, DataFetchingEnvironment environment) {
    if (!userExists(email)) {
      throw new GraphQLException("User does not exist");
    }

    FetchUserViewByEmail query = new FetchUserViewByEmail(email);
    UserView user = queryGateway.query(query, ResponseTypes.instanceOf(UserView.class)).join();

    Authentication authentication = new UsernamePasswordAuthenticationToken(user, password,
        List.of(new SimpleGrantedAuthority("USER")));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    GraphQLContext context = environment.getContext();
    HttpServletRequest request = context.getHttpServletRequest().get();
    HttpSession httpSession = request.getSession(true);
    httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

    return new AuthPayload(user, httpSession.getId());
  }

  public Boolean logout(DataFetchingEnvironment environment) {
    SecurityContextHolder.clearContext();

    GraphQLContext context = environment.getContext();
    HttpServletRequest request = context.getHttpServletRequest().get();
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    try {
      request.logout();
      return true;

    } catch (ServletException exception) {
      log.error("Failed to logout", exception);
      return false;
    }
  }

  public String createGroup(String name, Currency currency, List<ExpenseGroupMember> members) {
    String id = UUID.randomUUID().toString();
    CreateExpenseGroup command = new CreateExpenseGroup(id, name, currency, members);
    return commandGateway.sendAndWait(command);
  }

  public String answerToGroupInvitation(String invitationRequestId, InvitationAnswer answer) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String currentUserId = ((UserView) principal).getId();

    AnswerExpenseGroupInvitation command = new AnswerExpenseGroupInvitation(invitationRequestId, currentUserId, answer);
    commandGateway.sendAndWait(command);
    return "DONE";
  }

  public String addSplitToGroup(
      String groupId,
      String payedBy,
      Double total,
      String description,
      List<SplitMember> members
  ) {

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String currentUserId = ((UserView) principal).getId();

    Date createdAt = new Date();
    AddSplitToGroup command = new AddSplitToGroup(
        groupId, total, payedBy, description,
        currentUserId, createdAt, members
    );

    commandGateway.sendAndWait(command);
    return groupId;
  }

  // TODO: We are going to directly query the user projection in order to make this check a little easier.
  // TODO: Move this to a repository or something.
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

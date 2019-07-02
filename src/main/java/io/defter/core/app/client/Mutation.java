package io.defter.core.app.client;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import io.defter.core.app.api.*;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Mutation implements GraphQLMutationResolver {

  private final CommandGateway commandGateway;

  public String createUser(String name, String email) {
    String id = UUID.randomUUID().toString();
    CreateUser command = new CreateUser(id, name, email);
    return commandGateway.sendAndWait(command);
  }

  public String createGroup(String name, Currency currency, List<ExpenseGroupMember> members) {
    String id = UUID.randomUUID().toString();
    CreateExpenseGroup command = new CreateExpenseGroup(id, name, currency, members);
    return commandGateway.sendAndWait(command);
  }

  public String addSplitToGroup(String groupId, String payedBy, Double total, String description,
      List<SplitMember> members) {
    Date createdAt = new Date();
    AddSplitToGroup command = new AddSplitToGroup(groupId, total, payedBy, description, "",
        createdAt, members);
    commandGateway.sendAndWait(command);
    return groupId;
  }
}

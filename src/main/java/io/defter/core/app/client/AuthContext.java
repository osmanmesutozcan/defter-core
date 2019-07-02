package io.defter.core.app.client;

import graphql.servlet.GraphQLContext;
import io.defter.core.app.api.UserView;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;

@Getter
public class AuthContext extends GraphQLContext {

  private final UserView user;

  public AuthContext(UserView user, HttpServletRequest request) {
    super(request);
    this.user = user;
  }
}

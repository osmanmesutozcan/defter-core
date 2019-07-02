package io.defter.core.app.core;

import graphql.servlet.DefaultGraphQLContextBuilder;
import graphql.servlet.GraphQLContext;
import graphql.servlet.GraphQLContextBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.persistence.EntityManager;
import javax.websocket.server.HandshakeRequest;

@XSlf4j
@Component
@RequiredArgsConstructor
public class CustomGraphQLContextBuilder implements GraphQLContextBuilder {

  private final EntityManager entityManager;

  @Override
  public GraphQLContext build(HttpServletRequest request) {
    log.debug("Building {}", request);
    return new DefaultGraphQLContextBuilder().build(request);
  }

  @Override
  public GraphQLContext build() {
    return new DefaultGraphQLContextBuilder().build();
  }

  @Override
  public GraphQLContext build(HandshakeRequest request) {
    return new DefaultGraphQLContextBuilder().build(request);
  }
}


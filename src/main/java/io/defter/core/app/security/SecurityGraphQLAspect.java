package io.defter.core.app.security;

import lombok.extern.slf4j.XSlf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@XSlf4j
@Aspect
@Component
@Order(1)
public class SecurityGraphQLAspect {

  @Before("allGraphQLResolverMethods() && isDefinedInApplication() && !isMethodAnnotatedAsUnsecured()")
  public void doSecurityCheck() {

    if (
        SecurityContextHolder.getContext() == null ||
            SecurityContextHolder.getContext().getAuthentication() == null ||
            !SecurityContextHolder.getContext().getAuthentication().isAuthenticated() ||
            AnonymousAuthenticationToken.class
                .isAssignableFrom(SecurityContextHolder.getContext().getAuthentication().getClass())
    ) {

      throw new AccessDeniedException("User not authenticated");
    }
  }

  /**
   * Matches all beans that implement {@link com.coxautodev.graphql.tools.GraphQLResolver} note: {@code
   * GraphQLMutationResolver}, {@code GraphQLQueryResolver} etc extend base GraphQLResolver interface
   */
  @Pointcut("target(com.coxautodev.graphql.tools.GraphQLResolver)")
  private void allGraphQLResolverMethods() {
  }

  /**
   * Matches all beans in io.defter.core.app.client package resolvers must be in this package (subpackages)
   */
  @Pointcut("within(io.defter.core.app.client..*)")
  private void isDefinedInApplication() {
  }

  /**
   * Any method annotated with @Unsecured
   */
  @Pointcut("@annotation(io.defter.core.app.security.Unsecured)")
  private void isMethodAnnotatedAsUnsecured() {
  }
}

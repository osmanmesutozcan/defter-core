package io.defter.core.app.security;

import io.defter.core.app.api.UserView;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@XSlf4j
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

  private final EntityManager entityManager;

  @Override
  public JwtUser loadUserByUsername(String username) throws UsernameNotFoundException {
    try {
      TypedQuery<UserView> jpaQuery = entityManager
          .createNamedQuery("UserView.fetchByEmail", UserView.class);
      jpaQuery.setParameter("email", username);

      UserView user = jpaQuery.getSingleResult();
      return new JwtUser(user.getId(), user.getName(), user.getEmail());

      // TODO: Catch a less generic exception here.
    } catch (Exception exception) {
      log.info("user not found");
      throw new UsernameNotFoundException(String.format("User not found with username '%s'.", username));
    }
  }
}

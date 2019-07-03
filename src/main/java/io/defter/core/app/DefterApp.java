package io.defter.core.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication
public class DefterApp {

  public static void main(String[] args) {
    SpringApplication.run(DefterApp.class, args);
  }

}

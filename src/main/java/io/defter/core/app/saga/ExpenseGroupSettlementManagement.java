package io.defter.core.app.saga;

import io.defter.core.app.api.ExpenseGroupSettlementRequestAnswered;
import io.defter.core.app.api.ExpenseGroupSettlementRequested;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

@Saga
@XSlf4j
@RequiredArgsConstructor
public class ExpenseGroupSettlementManagement {

  @Inject
  private transient CommandGateway commandGateway;

  private String groupId;
  private RequestState requestState = RequestState.NOT_SENT;
  private Map<String, String> requestedUsers = new HashMap<>();

  @StartSaga
  @SagaEventHandler(associationProperty = "id")
  public void handle(ExpenseGroupSettlementRequested event) {
    //
  }

  private void sendRequest(String member, String group) {
    //
  }

  @SagaEventHandler(associationProperty = "answeredBy")
  public void handle(ExpenseGroupSettlementRequestAnswered event) {
    //
  }

  public enum RequestState {
    NOT_SENT,
    SENT,
  }

  public enum RequestAnswer {
    ACCEPTED,
    REJECTED,
  }
}

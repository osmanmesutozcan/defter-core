package io.defter.core.app.command;

import io.defter.core.app.api.AddSplitToGroup;
import io.defter.core.app.api.CreateExpenseGroup;
import io.defter.core.app.api.Currency;
import io.defter.core.app.api.ExpenseGroupCreated;
import io.defter.core.app.api.ExpenseGroupMember;
import io.defter.core.app.api.MemberAddedToGroup;
import io.defter.core.app.api.SplitAddedToGroup;
import io.defter.core.app.api.SplitMember;
import java.util.Date;
import java.util.List;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.Before;
import org.junit.Test;

public class ExpenseGroupTest {

  private AggregateTestFixture<ExpenseGroup> fixture;

  private List<ExpenseGroupMember> members = List.of(
      new ExpenseGroupMember("user1", ""),
      new ExpenseGroupMember("user2", "")
  );

  private List<SplitMember> splitMembers = List.of(
      new SplitMember("user1", false, 50),
      new SplitMember("user2", false, 50)
  );

  @Before
  public void setUp() {
    fixture = new AggregateTestFixture<>(ExpenseGroup.class);
  }

  @Test
  public void testCreateExpenseGroup() {
    fixture.givenNoPriorActivity()
        .when(new CreateExpenseGroup("a", "test", Currency.USD, "osman", members))
        .expectEvents(
            new ExpenseGroupCreated("a", "test", Currency.USD, "osman", members),
            new MemberAddedToGroup("a", members.get(0)),
            new MemberAddedToGroup("a", members.get(1))
        );
  }

  @Test
  public void testAddSplitToExpenseGroup() {
    Date date = new Date();

    fixture
        .given(
            new ExpenseGroupCreated("a", "test", Currency.USD, "osman", members),
            new MemberAddedToGroup("a", members.get(0)),
            new MemberAddedToGroup("a", members.get(1))
        )
        .when(
            new AddSplitToGroup("a", 100.0, "user1", "test", "user1", date, splitMembers, Currency.USD))
        .expectEvents(
            new SplitAddedToGroup("a", 100.0, "user1", "test", "user1", date, splitMembers, Currency.USD)
        );
  }
}

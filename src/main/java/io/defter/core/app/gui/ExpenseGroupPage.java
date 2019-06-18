package io.defter.core.app.gui;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.router.Route;
import io.defter.core.app.api.AddSplitToGroup;
import io.defter.core.app.api.CreateExpenseGroup;
import io.defter.core.app.api.Currency;
import io.defter.core.app.api.ExpenseGroupView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.XSlf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@XSlf4j
@Component
@Route(value = "", layout = MainView.class)
public class ExpenseGroupPage extends AppLayout {
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final Dialog addSplitDialog;

    private ExpenseGroupSummaryDataProvider expenseGroupSummaryDataProvider;

    @Autowired
    public ExpenseGroupPage(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;

        this.addSplitDialog = new Dialog();
        addSplitDialog.add(createAddSplitDialogContent());

        Grid summary = summaryGrid();

        HorizontalLayout commandBar = new HorizontalLayout();
        commandBar.setWidth("100%");
        commandBar.add(groupCreatePanel());

        VerticalLayout layout = new VerticalLayout();
        layout.add(commandBar, summary);
        layout.setSizeFull();

        setContent(layout);
    }

    private FormLayout groupCreatePanel() {
        TextField name = new TextField();
        name.setLabel("Name");
        name.setRequired(true);

        Select<Currency> currency = new Select<>(Currency.values());
        currency.setLabel("Currency");
        currency.setEmptySelectionAllowed(false);

        Button submit = new Button("Submit");
        submit.addClickListener(evt -> {
            if (currency.getValue() == null) {
                throw new IllegalStateException("Currency cannot be empty");
            }

            String id = UUID.randomUUID().toString();
            commandGateway.sendAndWait(new CreateExpenseGroup(id, name.getValue(), currency.getValue()));
            Notification.show("Success")
                    .addDetachListener(e -> expenseGroupSummaryDataProvider.refreshAll());
        });

        FormLayout form = new FormLayout();
        form.add(name, currency, submit);

        return form;
    }

    private FormLayout createAddSplitDialogContent() {
        TextField description = new TextField();
        description.setLabel("Description");

        NumberField amount = new NumberField();
        amount.setLabel("Amount");

        TextField payedBy = new TextField();
        payedBy.setLabel("Payed by ID");

        TextField submittedBy = new TextField();
        submittedBy.setLabel("Submitted by ID");

        Button submit = new Button("Submit");
        submit.addClickListener(evt -> {
            CurrentGroupId groupId = ComponentUtil.getData(addSplitDialog, CurrentGroupId.class);
            ComponentUtil.setData(addSplitDialog, CurrentGroupId.class, null);

            commandGateway.sendAndWait(new AddSplitToGroup(groupId.getId(), amount.getValue(), payedBy.getValue(), description.getValue(), submittedBy.getValue()));
            addSplitDialog.close();
            Notification.show("Success")
                    .addDetachListener(e -> expenseGroupSummaryDataProvider.refreshAll());
        });

        FormLayout form = new FormLayout();
        form.add(description, amount, payedBy, submittedBy, submit);
        return form;
    }

    private Grid summaryGrid() {
        expenseGroupSummaryDataProvider = new ExpenseGroupSummaryDataProvider(queryGateway);
        Grid<ExpenseGroupView> grid = new Grid<>();
        grid.addColumn(ExpenseGroupView::getId).setHeader("Group ID");
        grid.addColumn(ExpenseGroupView::getName).setHeader("Group Name");
        grid.addColumn(ExpenseGroupView::getCurrency).setHeader("Group Currency");
        grid.addColumn(ExpenseGroupView::getBalance).setHeader("Balance");
        grid.addColumn(ExpenseGroupView::getNumberOfSplits).setHeader("Number of Splits");
        grid.addColumn(new NativeButtonRenderer<>("AddSplit", item -> {
            ComponentUtil.setData(addSplitDialog, CurrentGroupId.class, new CurrentGroupId(item.getId()));
            addSplitDialog.open();
        }));

        grid.setSizeFull();
        grid.setDataProvider(expenseGroupSummaryDataProvider);
        return grid;
    }

    @Getter
    @Setter
    private class CurrentGroupId {
        public CurrentGroupId(String id) {
            this.id = id;
        }

        private final String id;
    }
}

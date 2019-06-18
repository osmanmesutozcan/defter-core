package io.defter.core.app.gui;

import com.vaadin.flow.component.applayout.AbstractAppRouterLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayoutMenu;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.PWA;

@Push
@Viewport(Viewport.DEFAULT)
@PWA(name = "Defter", shortName = "Defter")
public class MainView extends AbstractAppRouterLayout {
    public MainView() {
        //
    }

    @Override
    protected void configure(AppLayout appLayout, AppLayoutMenu appLayoutMenu) {
        appLayout.setBranding(new Span("Defter"));
    }
}

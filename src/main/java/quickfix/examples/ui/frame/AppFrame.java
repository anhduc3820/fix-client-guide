/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 ******************************************************************************/

package quickfix.examples.ui.frame;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import quickfix.examples.app.ApplicationRunner;
import quickfix.examples.Main;
import quickfix.examples.model.ObservableOrder;
import quickfix.examples.model.table.ExecutionTableModel;
import quickfix.examples.model.table.OrderTableModel;
import quickfix.examples.service.RoutingService;
import quickfix.examples.ui.panel.AppPanel;

/**
 * Main application window
 */
public class AppFrame extends JFrame {

    public AppFrame(OrderTableModel orderTableModel,
                    ExecutionTableModel executionTableModel,
                    final ApplicationRunner applicationRunner,
                    ObservableOrder observableOrder,
                    RoutingService routingService) {
        super();
        setTitle("Fix client guide !");
        setSize(600, 400);

        if (System.getProperties().containsKey("openfix")) {
            createMenuBar();
        }
        getContentPane().add(new AppPanel(orderTableModel, executionTableModel, applicationRunner, observableOrder, routingService),
                BorderLayout.CENTER);
        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menubar = new JMenuBar();

        JMenu sessionMenu = new JMenu("Session");
        menubar.add(sessionMenu);

        JMenuItem logonItem = new JMenuItem("Logon");
        logonItem.addActionListener(e -> Main.get().logon());
        sessionMenu.add(logonItem);

        JMenuItem logoffItem = new JMenuItem("Logoff");
        logoffItem.addActionListener(e -> Main.get().logout());
        sessionMenu.add(logoffItem);

        JMenu appMenu = new JMenu("Application");
        menubar.add(appMenu);

        setJMenuBar(menubar);
    }
}

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

package quickfix.examples.ui.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import quickfix.examples.model.Order;
import quickfix.examples.service.RoutingService;

public class CancelReplacePanel extends JPanel {
    private final JButton cancelButton = new JButton("Cancel");
    private Order order = null;

    private final GridBagConstraints constraints = new GridBagConstraints();

    private final RoutingService routingService;

    public CancelReplacePanel(final RoutingService routingService) {
        this.routingService = routingService;
        cancelButton.addActionListener(new CancelListener());

        setLayout(new GridBagLayout());
        createComponents();
    }

    public void addActionListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }

    private void createComponents() {
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        int x = 0;
        int y = 0;

        constraints.insets = new Insets(0, 0, 5, 5);
        add(cancelButton, x, y);
        constraints.weightx = 0;
    }

    public void setEnabled(boolean enabled) {
        cancelButton.setEnabled(enabled);
    }

    public void update() {
        setOrder(this.order);
    }

    public void setOrder(Order order) {
        if (order == null)
            return;

        this.order = order;
        setEnabled(order.getOpen() > 0);
    }

    private JComponent add(JComponent component, int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        add(component, constraints);
        return component;
    }

    private class CancelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            routingService.cancel(order);
        }
    }
}

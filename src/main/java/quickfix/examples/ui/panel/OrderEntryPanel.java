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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import quickfix.examples.enumerate.OrderSide;
import quickfix.examples.enumerate.OrderTIF;
import quickfix.examples.enumerate.OrderType;
import quickfix.examples.model.LogonEvent;
import quickfix.examples.model.Order;
import quickfix.examples.model.table.OrderTableModel;
import quickfix.examples.service.RoutingService;
import quickfix.examples.utils.CSVUtils;
import quickfix.examples.utils.DoubleNumberTextField;
import quickfix.examples.utils.IntegerNumberTextField;
import quickfix.SessionID;
import quickfix.examples.app.ApplicationRunner;

@SuppressWarnings("unchecked")
public class OrderEntryPanel extends JPanel implements Observer {
    private boolean quantityEntered = false;
    private boolean limitEntered = false;
    private boolean sessionEntered = false;

    private final JComboBox symbolComBox = new JComboBox(CSVUtils.symbols);
    private final IntegerNumberTextField quantityTextField = new IntegerNumberTextField();

    private final JComboBox sideComboBox = new JComboBox(OrderSide.toArray());
    private final JComboBox typeComboBox = new JComboBox(OrderType.toArray());
    private final JComboBox tifComboBox = new JComboBox(OrderTIF.toArray());

    private final DoubleNumberTextField limitPriceTextField = new DoubleNumberTextField();
    private final DoubleNumberTextField stopPriceTextField = new DoubleNumberTextField();

    private final JComboBox sessionComboBox = new JComboBox();

    private final JLabel limitPriceLabel = new JLabel("Limit");

    private final JLabel messageLabel = new JLabel(" ");
    private final JButton submitButton = new JButton("Submit");

    private final OrderTableModel orderTableModel;
    private final transient ApplicationRunner applicationRunner;
    private final RoutingService routingService;

    private final GridBagConstraints constraints = new GridBagConstraints();

    public OrderEntryPanel(final OrderTableModel orderTableModel,
                           final ApplicationRunner applicationRunner,
                            final RoutingService routingService) {
        setName("OrderEntryPanel");
        this.orderTableModel = orderTableModel;
        this.applicationRunner = applicationRunner;
        this.routingService = routingService;

        applicationRunner.addLogonObserver(this);

        SubmitActivator activator = new SubmitActivator();
        symbolComBox.addKeyListener(activator);
        quantityTextField.addKeyListener(activator);
        limitPriceTextField.addKeyListener(activator);
        stopPriceTextField.addKeyListener(activator);
        sessionComboBox.addItemListener(activator);

        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        setLayout(new GridBagLayout());
        createComponents();
    }

    public void addActionListener(ActionListener listener) {
        submitButton.addActionListener(listener);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
        if (message == null || message.equals(""))
            messageLabel.setText(" ");
    }

    public void clearMessage() {
        setMessage(null);
    }

    private void createComponents() {
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        int x = 0;
        int y = 0;

        add(new JLabel("Symbol"), x, y);
        add(new JLabel("Quantity"), ++x, y);
        add(new JLabel("Side"), ++x, y);
        add(new JLabel("Type"), ++x, y);

        constraints.ipadx = 30;
        add(limitPriceLabel, ++x, y);
        constraints.ipadx = 0;

        add(new JLabel("TIF"), ++x, y);
        constraints.ipadx = 30;

        symbolComBox.setName("symbolComBox");
        add(symbolComBox, x = 0, ++y);
        constraints.ipadx = 0;

        quantityTextField.setName("QuantityTextField");
        add(quantityTextField, ++x, y);

        sideComboBox.setName("SideComboBox");
        add(sideComboBox, ++x, y);

        typeComboBox.setName("TypeComboBox");
        add(typeComboBox, ++x, y);

        limitPriceTextField.setName("LimitPriceTextField");
        add(limitPriceTextField, ++x, y);

        tifComboBox.setName("TifComboBox");
        add(tifComboBox, ++x, y);

        constraints.insets = new Insets(3, 0, 0, 0);
        constraints.gridwidth = GridBagConstraints.RELATIVE;

        sessionComboBox.setName("SessionComboBox");
        add(sessionComboBox, 0, ++y);
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        submitButton.setName("SubmitButton");
        add(submitButton, x, y);
        constraints.gridwidth = 0;
        add(messageLabel, 0, ++y);

        typeComboBox.addItemListener(new PriceListener());

        Font font = new Font(messageLabel.getFont().getFontName(), Font.BOLD, 12);
        messageLabel.setFont(font);
        messageLabel.setForeground(Color.red);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        submitButton.setEnabled(false);
        submitButton.addActionListener(new SubmitListener());
        activateSubmit();
    }

    private JComponent add(JComponent component, int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        add(component, constraints);
        return component;
    }

    private void activateSubmit() {
        OrderType type = (OrderType) typeComboBox.getSelectedItem();
        boolean activate = quantityEntered && sessionEntered;

        if (type == OrderType.LIMIT)
            submitButton.setEnabled(activate && limitEntered);
    }

    private class PriceListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            OrderType item = (OrderType) typeComboBox.getSelectedItem();
            if (item == OrderType.LIMIT) {
                enableLimitPrice(true);
            }
            activateSubmit();
        }

        private void enableLimitPrice(boolean enabled) {
            Color labelColor = enabled ? Color.black : Color.gray;
            Color bgColor = enabled ? Color.white : Color.gray;
            limitPriceTextField.setEnabled(enabled);
            limitPriceTextField.setBackground(bgColor);
            limitPriceLabel.setForeground(labelColor);
        }
    }

    public void update(Observable o, Object arg) {
        LogonEvent logonEvent = (LogonEvent) arg;
        if (logonEvent.isLoggedOn())
            sessionComboBox.addItem(logonEvent.getSessionID());
        else
            sessionComboBox.removeItem(logonEvent.getSessionID());
    }

    private class SubmitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Order order = new Order();
            order.setSide((OrderSide) sideComboBox.getSelectedItem());
            order.setType((OrderType) typeComboBox.getSelectedItem());
            order.setTIF((OrderTIF) tifComboBox.getSelectedItem());
            order.setSymbol(symbolComBox.getSelectedItem().toString());
            order.setQuantity(Integer.parseInt(quantityTextField.getText()));
            order.setOpen(order.getQuantity());

            OrderType type = order.getType();
            if (type == OrderType.LIMIT)
                order.setLimit(limitPriceTextField.getText());
            order.setSessionID((SessionID) sessionComboBox.getSelectedItem());

            orderTableModel.addOrder(order);
            routingService.send(order);
        }
    }

    private class SubmitActivator implements KeyListener, ItemListener {
        public void keyReleased(KeyEvent e) {
            Object obj = e.getSource();
            if (obj == quantityTextField) {
                quantityEntered = testField(obj);
            } else if (obj == limitPriceTextField) {
                limitEntered = testField(obj);
            }
            activateSubmit();
        }

        public void itemStateChanged(ItemEvent e) {
            sessionEntered = sessionComboBox.getSelectedItem() != null;
            activateSubmit();
        }

        private boolean testField(Object o) {
            String value = ((JTextField) o).getText();
            value = value.trim();
            return value.length() > 0;
        }

        public void keyTyped(KeyEvent e) {}

        public void keyPressed(KeyEvent e) {}
    }
}

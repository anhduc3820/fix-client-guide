package quickfix.examples.ui.panel;

import quickfix.SessionID;
import quickfix.examples.enumerate.OrderSide;
import quickfix.examples.enumerate.OrderTIF;
import quickfix.examples.enumerate.OrderType;
import quickfix.examples.model.Order;
import quickfix.examples.model.table.OrderBookTableModel;
import quickfix.examples.ui.table.OrderBookTable;
import quickfix.examples.utils.CSVUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class OrderBookPanel extends JPanel {

    private final JComboBox symbolComBox = new JComboBox(CSVUtils.symbols);
    private JTable orderBookTable = null;
    private final GridBagConstraints constraints = new GridBagConstraints();

    public OrderBookPanel(OrderBookTableModel orderBookTableModel) {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;

        orderBookTable = new OrderBookTable(orderBookTableModel);
        add(new JScrollPane(orderBookTable), constraints);

        createComponents();
    }

    public JTable orderBookTable() {
        return orderBookTable;
    }

    private class SymbolListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            String symbol = symbolComBox.getSelectedItem().toString();
            processOrderBook(symbol);
        }
    }

    private void processOrderBook(String symbol) {
//        OrderType type = (OrderType) typeComboBox.getSelectedItem();
//        boolean activate = quantityEntered && sessionEntered;
//
//        if (type == OrderType.LIMIT)
//            submitButton.setEnabled(activate && limitEntered);
    }

    private void createComponents() {
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        int x = 0;
        int y = 0;

        add(new JLabel("Symbol"), x, y);
        symbolComBox.setName("symbolComBox");
        add(symbolComBox, x = 0, ++y);
        constraints.ipadx = 0;

        symbolComBox.addItemListener(new OrderBookPanel.SymbolListener());
        constraints.insets = new Insets(3, 0, 0, 0);
        constraints.gridwidth = GridBagConstraints.RELATIVE;
    }

    private JComponent add(JComponent component, int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        add(component, constraints);
        return component;
    }
}

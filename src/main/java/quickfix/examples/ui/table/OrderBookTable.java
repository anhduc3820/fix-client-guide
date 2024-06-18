package quickfix.examples.ui.table;

import quickfix.examples.model.OrderBook;
import quickfix.examples.model.table.OrderBookTableModel;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class OrderBookTable extends JTable implements MouseListener {

    public OrderBookTable(OrderBookTableModel orderBookTableModel) {
        super(orderBookTableModel);
        addMouseListener(this);
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        OrderBook orderBook = ((OrderBookTableModel) dataModel).getOrderBook(row);

        String price = orderBook.getPrice();
        String volume = orderBook.getVolume();

        DefaultTableCellRenderer r = (DefaultTableCellRenderer) renderer;
        r.setForeground(Color.black);

        if (row == 4)
            r.setBackground(Color.red);
        else if (row == 5)
            r.setBackground(Color.green);
        else
            r.setBackground(Color.white);

        return super.prepareRenderer(renderer, row, column);
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2)
            return;

        int row = rowAtPoint(e.getPoint());
        OrderBook orderBook = ((OrderBookTableModel) dataModel).getOrderBook(row);
        String symbol = orderBook.getSymbol();
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}
}

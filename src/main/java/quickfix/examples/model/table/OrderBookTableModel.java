package quickfix.examples.model.table;

import quickfix.examples.model.OrderBook;
import quickfix.examples.utils.CSVUtils;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderBookTableModel extends AbstractTableModel {
    private final static int BUY = 0;
    private final static int SELL = 1;

    private final HashMap<Integer, OrderBook> rowCurrentOrderBook;

    private final HashMap<String, List<OrderBook>> mapOrderBookListBuy;
    private final HashMap<String, List<OrderBook>> mapOrderBookListSell;

    private final HashMap<String, HashMap<Integer, OrderBook>> mapOrderBookBuy;
    private final HashMap<String, HashMap<Integer, OrderBook>> mapOrderBookSell;

    private final String[] headers;

    private HashMap<Integer, OrderBook> getRowCurrentOrderBook() {
        HashMap<Integer, OrderBook> orderBooks = new HashMap<>();

        int index = 0;
        for (Map.Entry<String, HashMap<Integer, OrderBook>> entry : mapOrderBookBuy.entrySet()) {
            HashMap<Integer, OrderBook> v = entry.getValue();
            for (Map.Entry<Integer, OrderBook> entryJ : v.entrySet()) {
                orderBooks.put(index, entryJ.getValue());
                index++;
                if (index == 5)
                    break;
            }

            if (index == 5)
                break;
        }

        for (Map.Entry<String, HashMap<Integer, OrderBook>> entry : mapOrderBookSell.entrySet()) {
            HashMap<Integer, OrderBook> v = entry.getValue();
            for (Map.Entry<Integer, OrderBook> entryJ : v.entrySet()) {
                orderBooks.put(index, entryJ.getValue());
                index++;
                if (index == 10)
                    break;
            }

            if (index == 10)
                break;
        }

        return orderBooks;
    }

    private HashMap<String, List<OrderBook>> createOrderBookBuyDefaults() {
        HashMap<String, List<OrderBook>> mapOrderBookListBuy = new HashMap<>();
        List<String> symbols = CSVUtils.symbolList;
        for (String symbol : symbols) {
            List<OrderBook> orderBookList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                OrderBook orderBook = new OrderBook(i, symbol, "-", "-");
                orderBookList.add(orderBook);
            }

            mapOrderBookListBuy.put(symbol, orderBookList);
        }

        return mapOrderBookListBuy;
    }

    private HashMap<String, List<OrderBook>> createOrderBookSellDefaults() {
        HashMap<String, List<OrderBook>> mapOrderBookListSell = new HashMap<>();
        List<String> symbols = CSVUtils.symbolList;
        for (String symbol : symbols) {
            List<OrderBook> orderBookList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                OrderBook orderBook = new OrderBook(i, symbol, "-", "-");
                orderBookList.add(orderBook);
            }

            mapOrderBookListSell.put(symbol, orderBookList);
        }

        return mapOrderBookListSell;
    }

    private HashMap<String, HashMap<Integer, OrderBook>> createOrderBookBuyMap() {
        HashMap<String, HashMap<Integer, OrderBook>> mapOrderBookBuy = new HashMap<>();
        mapOrderBookListBuy.forEach((k, v) -> {
            HashMap<Integer, OrderBook> bookHashMap = new HashMap<>();
            v.forEach(orderBook -> {
                bookHashMap.put(orderBook.getIndex(), orderBook);
            });

            mapOrderBookBuy.put(k, bookHashMap);
        });

        return mapOrderBookBuy;
    }

    private HashMap<String, HashMap<Integer, OrderBook>> createOrderBookSellMap() {
        HashMap<String, HashMap<Integer, OrderBook>> mapOrderBookSell = new HashMap<>();
        mapOrderBookListSell.forEach((k, v) -> {
            HashMap<Integer, OrderBook> bookHashMap = new HashMap<>();
            v.forEach(orderBook -> {
                bookHashMap.put(orderBook.getIndex(), orderBook);
            });

            mapOrderBookSell.put(k, bookHashMap);
        });

        return mapOrderBookSell;
    }

    public OrderBookTableModel() {
        mapOrderBookListBuy = createOrderBookBuyDefaults();
        mapOrderBookListSell = createOrderBookSellDefaults();
        mapOrderBookBuy = createOrderBookBuyMap();
        mapOrderBookSell = createOrderBookSellMap();
        rowCurrentOrderBook = getRowCurrentOrderBook();

        headers = new String[] {"              ", "              "};
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void addOrderBook(OrderBook orderBook) {
//        int row = rowToOrderBook.size();
//
//        rowToOrderBook.put(row, orderBook);
//        idToRow.put(order.getID(), row);
//        idToOrder.put(order.getID(), order);

//        fireTableRowsInserted(row, row);
    }

    public void updateOrderBook(OrderBook orderBook) {

//        if (!id.equals(order.getID())) {
//            String originalID = order.getID();
//            order.setID(id);
//            replaceOrder(order, originalID);
//            return;
//        }
//
//        Integer row = idToRow.get(order.getID());
//        if (row == null)
//            return;
//        fireTableRowsUpdated(row, row);
    }

    public void replaceOrder(OrderBook orderBook, String originalID) {

//        Integer row = idToRow.get(originalID);
//        if (row == null)
//            return;
//
//        rowToOrderBook.put(row, orderBook);
//        idToRow.put(order.getID(), row);
//        idToOrder.put(order.getID(), order);

//        fireTableRowsUpdated(row, row);
    }

    public void addID(OrderBook orderBook) {
//        orderBooks.add(orderBook);
    }

    public OrderBook getOrderBook(int row) {
        return rowCurrentOrderBook.get(row);
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) { }

    public Class<String> getColumnClass(int columnIndex) {
        return String.class;
    }

    public int getRowCount() {
        return rowCurrentOrderBook.size();
    }

    public int getColumnCount() {
        return headers.length;
    }

    public String getColumnName(int columnIndex) {
        return headers[columnIndex];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        OrderBook orderBook = rowCurrentOrderBook.get(rowIndex);
        switch (columnIndex) {
            case BUY:
                return orderBook.getPrice();
            case SELL:
                return orderBook.getVolume();
        }
        return "";
    }
}

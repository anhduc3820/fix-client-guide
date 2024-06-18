package quickfix.examples.model;

public class OrderBook implements Cloneable {
    private int index;
    private String symbol = "";
    private String price = "-";
    private String volume = "-";

    public OrderBook(int index) {
        this.index = index;
    }

    public OrderBook(int index,String symbol, String price, String volume) {
        this.index = index;
        this.price = price;
        this.volume = volume;
        this.symbol = symbol;
    }

    public Object clone() {
        try {
            OrderBook orderBook = (OrderBook) super.clone();
            orderBook.setPrice(getPrice());
            orderBook.setVolume(getVolume());
            return orderBook;
        } catch (CloneNotSupportedException ignored) {}
        return null;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}

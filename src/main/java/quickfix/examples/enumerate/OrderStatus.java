package quickfix.examples.enumerate;

import quickfix.field.OrdStatus;

import java.util.HashMap;
import java.util.Map;

public class OrderStatus {
    static private final Map<String, OrderStatus> known = new HashMap<>();
    static public final OrderStatus PENDING_NEW = new OrderStatus("Pending new");
    static public final OrderStatus NEW = new OrderStatus("New");
    static public final OrderStatus PENDING_CANCEL = new OrderStatus("Pending cancel");
    static public final OrderStatus CANCELED = new OrderStatus("Canceled");
    static public final OrderStatus PARTIALLY_FILLED = new OrderStatus("Partially filled");
    static public final OrderStatus FILLED = new OrderStatus("Filled");

    static private final OrderStatus[] array = {PENDING_NEW, NEW, PENDING_CANCEL, CANCELED};

    private final String status;

    private OrderStatus(String status) {
        this.status = status;
        synchronized (OrderStatus.class) {
            known.put(status, this);
        }
    }

    public String getStatus() {
        return status;
    }

    public String toString() {
        return status;
    }

    static public Object[] toArray() {
        return array;
    }

    public static OrderStatus parse(String status) throws IllegalArgumentException {
        OrderStatus result = known.get(status);
        if (result == null) {
            throw new IllegalArgumentException
                    ("OrderStatus: " + status + " is unknown.");
        }
        return result;
    }

    public static String parse(char status) throws IllegalArgumentException {
        switch (status) {
            case OrdStatus.NEW:
                return NEW.status;
            case OrdStatus.PENDING_NEW:
                return PENDING_NEW.status;
            case OrdStatus.CANCELED:
                return CANCELED.status;
            case OrdStatus.PENDING_CANCEL:
                return PENDING_CANCEL.status;
            case OrdStatus.PARTIALLY_FILLED:
                return PARTIALLY_FILLED.status;
            case OrdStatus.FILLED:
                return FILLED.status;
            default:
                return PENDING_NEW.status;
        }
    }
}

package quickfix.examples.utils;

import quickfix.examples.enumerate.OrderSide;
import quickfix.examples.enumerate.OrderTIF;
import quickfix.examples.enumerate.OrderType;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.field.TimeInForce;

public class ParseUtils {

    static private final TwoWayMap sideMap = new TwoWayMap();
    static private final TwoWayMap typeMap = new TwoWayMap();
    static private final TwoWayMap tifMap = new TwoWayMap();

    public static Side sideToFIXSide(OrderSide side) {
        return (Side) sideMap.getFirst(side);
    }

    public static OrderSide FIXSideToSide(Side side) {
        return (OrderSide) sideMap.getSecond(side);
    }

    public static OrdType typeToFIXType(OrderType type) {
        return (OrdType) typeMap.getFirst(type);
    }

    public static TimeInForce tifToFIXTif(OrderTIF tif) {
        return (TimeInForce) tifMap.getFirst(tif);
    }

    static {
        sideMap.put(OrderSide.BUY, new Side(Side.BUY));
        sideMap.put(OrderSide.SELL, new Side(Side.SELL));

        typeMap.put(OrderType.LIMIT, new OrdType(OrdType.LIMIT));

        tifMap.put(OrderTIF.DAY, new TimeInForce(TimeInForce.DAY));
    }
}

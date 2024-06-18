package quickfix.examples.service;

import quickfix.examples.enumerate.OrderType;
import quickfix.examples.model.Order;
import quickfix.examples.model.table.OrderTableModel;
import quickfix.examples.utils.LogUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import quickfix.*;
import quickfix.field.*;

import static quickfix.examples.utils.ParseUtils.*;

@Slf4j
@AllArgsConstructor
public class RoutingService {


    private final ExecutionReportFIX42Service executionReportFIX42Service;
    private final ExecutionReportFIX44Service executionReportFIX44Service;

    private final DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private final OrderTableModel orderTableModel;

    public void processFIXResponseMessage(Message message, SessionID sessionID) {
        try {
            String beginString = sessionID.getBeginString();
            switch (beginString) {
                case FixVersions.BEGINSTRING_FIX40:
                case FixVersions.BEGINSTRING_FIX41:
                case FixVersions.BEGINSTRING_FIX43:
                case FixVersions.BEGINSTRING_FIXT11:
                    // TODO handle
                    break;
                case FixVersions.BEGINSTRING_FIX42:
                    processFIX42((quickfix.fix42.Message) message, sessionID);
                    break;
                case FixVersions.BEGINSTRING_FIX44:
                    processFIX44((quickfix.fix44.Message) message, sessionID);
                    break;
            }
        } catch (Exception e) {
            log.error("processFIXResponseMessage message: {} Session: {} throws: {}", message, sessionID , e);
        }
    }

    private void processFIX42(quickfix.fix42.Message message, SessionID sessionID) {
        try {
            switch (message.getHeader().getString(MsgType.FIELD)) {
                case MsgType.EXECUTION_REPORT:
                    quickfix.fix42.ExecutionReport executionReport = (quickfix.fix42.ExecutionReport) message;
                    executionReportFIX42Service.handleExecutionReportFIX42Event(executionReport, sessionID);
                    break;
                case MsgType.ORDER_CANCEL_REJECT:
                    quickfix.fix42.OrderCancelReject orderReject = (quickfix.fix42.OrderCancelReject) message;
                    executionReportFIX42Service.handleOrderCancelReject(orderReject);
                    break;

                default:
                    log.warn("Unsupported MessageType with msg: {}", LogUtils.formatFixMessageLog(message));
                    sendBusinessReject(message, BusinessRejectReason.UNSUPPORTED_MESSAGE_TYPE,
                            "Unsupported Message Type");
            }
        } catch (Exception e) {
            log.error("Handle process FIXRequestMessage failed with msg: {}", LogUtils.formatFixMessageLog(message), e);
        }
    }

    private void processFIX44(quickfix.fix44.Message message, SessionID sessionID) {
        try {
            switch (message.getHeader().getString(MsgType.FIELD)) {
                case MsgType.EXECUTION_REPORT:
                    quickfix.fix44.ExecutionReport executionReport = (quickfix.fix44.ExecutionReport) message;
                    executionReportFIX44Service.handleExecutionReportFIX44Event(executionReport, sessionID);
                    break;

                case MsgType.ORDER_CANCEL_REJECT:
                    quickfix.fix44.OrderCancelReject orderReject = (quickfix.fix44.OrderCancelReject) message;
                    executionReportFIX44Service.handleOrderCancelReject(orderReject);
                    break;

                default:
                    log.warn("Unsupported MessageType with msg: {}", LogUtils.formatFixMessageLog(message));
                    sendBusinessReject(message, BusinessRejectReason.UNSUPPORTED_MESSAGE_TYPE,
                            "Unsupported Message Type");
            }
        } catch (Exception e) {
            log.error("Handle process FIXRequestMessage failed with msg: {}", LogUtils.formatFixMessageLog(message), e);
        }
    }

    private void sendBusinessReject(Message message, int rejectReason, String rejectText)
            throws FieldNotFound, SessionNotFound {

        Message reply = createMessage(message, MsgType.BUSINESS_MESSAGE_REJECT);
        reverseRoute(message, reply);

        String refSeqNum = message.getHeader().getString(MsgSeqNum.FIELD);
        reply.setString(RefSeqNum.FIELD, refSeqNum);
        reply.setString(RefMsgType.FIELD, message.getHeader().getString(MsgType.FIELD));
        reply.setInt(BusinessRejectReason.FIELD, rejectReason);
        reply.setString(Text.FIELD, rejectText);

        Session.sendToTarget(reply);
    }

    private Message createMessage(Message message, String msgType) throws FieldNotFound {
        return messageFactory.create(message.getHeader().getString(BeginString.FIELD), msgType);
    }

    private void reverseRoute(Message message, Message reply) throws FieldNotFound {
        reply.getHeader().setString(SenderCompID.FIELD,
                message.getHeader().getString(TargetCompID.FIELD));
        reply.getHeader().setString(TargetCompID.FIELD,
                message.getHeader().getString(SenderCompID.FIELD));
    }

    private void send(Message message, SessionID sessionID) {
        try {
            Session.sendToTarget(message, sessionID);
        } catch (SessionNotFound e) {
            log.error("sendToTarget throws: ", e);
        }
    }

    public void send(Order order) {
        String beginString = order.getSessionID().getBeginString();
        switch (beginString) {
            case FixVersions.BEGINSTRING_FIX40:
                send40(order);
                break;
            case FixVersions.BEGINSTRING_FIX41:
                send41(order);
                break;
            case FixVersions.BEGINSTRING_FIX42:
                send42(order);
                break;
            case FixVersions.BEGINSTRING_FIX43:
                send43(order);
                break;
            case FixVersions.BEGINSTRING_FIX44:
                send44(order);
                break;
            case FixVersions.BEGINSTRING_FIXT11:
                send50(order);
                break;
        }
    }

    public void send40(Order order) {
        quickfix.fix40.NewOrderSingle newOrderSingle = new quickfix.fix40.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), new OrderQty(order.getQuantity()),
                typeToFIXType(order.getType()));

        send(populateOrder(order, newOrderSingle), order.getSessionID());
    }

    public void send41(Order order) {
        quickfix.fix41.NewOrderSingle newOrderSingle = new quickfix.fix41.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), typeToFIXType(order.getType()));

        newOrderSingle.set(new OrderQty(order.getQuantity()));

        send(populateOrder(order, newOrderSingle), order.getSessionID());
    }

    public void send42(Order order) {
        quickfix.fix42.NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), new TransactTime(), typeToFIXType(order.getType()));

        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Account("1111111"));

        send(populateOrder(order, newOrderSingle), order.getSessionID());
    }

    public void send43(Order order) {
        quickfix.fix43.NewOrderSingle newOrderSingle = new quickfix.fix43.NewOrderSingle(
                new ClOrdID(order.getID()), new HandlInst('1'), sideToFIXSide(order.getSide()),
                new TransactTime(), typeToFIXType(order.getType()));

        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));

        send(populateOrder(order, newOrderSingle), order.getSessionID());
    }

    public void send44(Order order) {
        quickfix.fix44.NewOrderSingle newOrderSingle = new quickfix.fix44.NewOrderSingle(
                new ClOrdID(order.getID()), sideToFIXSide(order.getSide()),
                new TransactTime(), typeToFIXType(order.getType()));

        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));
        newOrderSingle.set(new HandlInst('1'));

        send(populateOrder(order, newOrderSingle), order.getSessionID());
    }

    public void send50(Order order) {
        quickfix.fix50.NewOrderSingle newOrderSingle = new quickfix.fix50.NewOrderSingle(
                new ClOrdID(order.getID()), sideToFIXSide(order.getSide()),
                new TransactTime(), typeToFIXType(order.getType()));

        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));
        newOrderSingle.set(new HandlInst('1'));

        send(populateOrder(order, newOrderSingle), order.getSessionID());
    }

    public Message populateOrder(Order order, Message newOrderSingle) {
        OrderType type = order.getType();
        if (type == OrderType.LIMIT)
            newOrderSingle.setField(new Price(order.getLimit().doubleValue()));

        newOrderSingle.setField(tifToFIXTif(order.getTIF()));

        return newOrderSingle;
    }

    public void cancel(Order order) {
        String beginString = order.getSessionID().getBeginString();
        switch (beginString) {
            case "FIX.4.0":
                cancel40(order);
                break;
            case "FIX.4.1":
                cancel41(order);
                break;
            case "FIX.4.2":
                cancel42(order);
                break;
        }
    }

    public void cancel40(Order order) {
        String id = order.generateID();
        quickfix.fix40.OrderCancelRequest message = new quickfix.fix40.OrderCancelRequest(
                new OrigClOrdID(order.getID()), new ClOrdID(id), new CxlType(CxlType.FULL_REMAINING_QUANTITY), new Symbol(order
                .getSymbol()), sideToFIXSide(order.getSide()), new OrderQty(order
                .getQuantity()));

        orderTableModel.addID(order, id);

        send(message, order.getSessionID());
    }

    public void cancel41(Order order) {
        String id = order.generateID();
        quickfix.fix41.OrderCancelRequest message = new quickfix.fix41.OrderCancelRequest(
                new OrigClOrdID(order.getID()), new ClOrdID(id), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()));
        message.setField(new OrderQty(order.getQuantity()));

        orderTableModel.addID(order, id);

        send(message, order.getSessionID());
    }

    public void cancel42(Order order) {
        String id = order.generateID();
        quickfix.fix42.OrderCancelRequest message = new quickfix.fix42.OrderCancelRequest(
                new OrigClOrdID(order.getID()), new ClOrdID(id), new Symbol(order.getSymbol()),
                sideToFIXSide(order.getSide()), new TransactTime());

        message.setField(new OrderQty(order.getQuantity()));
        message.setField(new Account("1111111"));

        orderTableModel.addID(order, id);

        send(message, order.getSessionID());
    }
}

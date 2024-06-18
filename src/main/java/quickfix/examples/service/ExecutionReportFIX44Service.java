package quickfix.examples.service;

import quickfix.examples.model.Execution;
import quickfix.examples.model.ObservableOrder;
import quickfix.examples.model.Order;
import quickfix.examples.model.table.ExecutionTableModel;
import quickfix.examples.model.table.OrderTableModel;
import quickfix.examples.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;

import static quickfix.examples.utils.ParseUtils.FIXSideToSide;

@RequiredArgsConstructor
@Slf4j
public class ExecutionReportFIX44Service {

    private final ObservableOrder observableOrder;
    private final ExecutionTableModel executionTableModel;
    private final OrderTableModel orderTableModel;

    static private final HashMap<SessionID, HashSet<ExecID>> execIDs = new HashMap<>();

    @SneakyThrows
    public void handleExecutionReportTypeTradeFIX44(quickfix.fix44.ExecutionReport executionReport, SessionID sessionID) {
        switch (executionReport.getOrdStatus().getValue()) {
            case OrdStatus.PARTIALLY_FILLED:
                handlePartialFillOrderNotice(executionReport, sessionID);
                break;
            case OrdStatus.FILLED:
                handleFillOrderNotice(executionReport, sessionID);
                break;
            default:
                log.warn("handleExecutionReportTypeTradeFIX44 Unsupported OrdStatus {}", executionReport.getOrdStatus().getValue());
                break;
        }
    }

    @SneakyThrows
    public void handleExecutionReportFIX44Event(quickfix.fix44.ExecutionReport executionReport, SessionID sessionID) {
        switch (executionReport.getExecType().getValue()) {
            case ExecType.PENDING_NEW:
                handlePendingNewOrderNotice(executionReport, sessionID);
                break;
            case ExecType.NEW:
                handleNewOrderNotice(executionReport, sessionID);
                break;
            case ExecType.PENDING_CANCEL:
                handlePendingCancelOrderNotice(executionReport, sessionID);
                break;
            case ExecType.CANCELED:
                handleCancelOrderNotice(executionReport, sessionID);
                break;
            case ExecType.TRADE:
                handleExecutionReportTypeTradeFIX44(executionReport, sessionID);
                break;
            case ExecType.REJECTED:
                handleRejectOrderNotice(executionReport, sessionID);
                break;
            case ExecType.EXPIRED:
                handleExpiredOrderNotice(executionReport, sessionID);
                break;

            default:
                log.warn("handleExecutionReportFIX44Event Unsupported ExecType {}", executionReport.getExecType().getValue());
                break;
        }
    }

    public void handleNewOrderNotice(quickfix.fix44.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handleNewOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
    }

    public void handlePartialFillOrderNotice(quickfix.fix44.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handlePartialFillOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
    }

    public void handleFillOrderNotice(quickfix.fix44.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handleFillOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
    }

    public void handleCancelOrderNotice(quickfix.fix44.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handleCancelOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
    }

    public void handlePendingCancelOrderNotice(quickfix.fix44.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handlePendingCancelOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
    }

    public void handleRejectOrderNotice(quickfix.fix44.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handleRejectOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
    }

    public void handlePendingNewOrderNotice(quickfix.fix44.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handlePendingNewOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
    }

    public void handleExpiredOrderNotice(quickfix.fix44.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handleExpiredOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
    }

    private void executionReport(Message message, SessionID sessionID) throws FieldNotFound {

        ExecID execID = (ExecID) message.getField(new ExecID());
        if (alreadyProcessed(execID, sessionID))
            return;

        Order order = orderTableModel.getOrder(message.getField(new ClOrdID()).getValue());
        if (order == null) {
            return;
        }

        BigDecimal fillSize;

        if (message.isSetField(LastShares.FIELD)) {
            LastShares lastShares = new LastShares();
            message.getField(lastShares);
            fillSize = new BigDecimal("" + lastShares.getValue());
        } else {
            // > FIX 4.1
            LeavesQty leavesQty = new LeavesQty();
            message.getField(leavesQty);
            fillSize = new BigDecimal(order.getQuantity()).subtract(new BigDecimal("" + leavesQty.getValue()));
        }

        if (fillSize.compareTo(BigDecimal.ZERO) > 0) {
            order.setOpen(order.getOpen() - (int) Double.parseDouble(fillSize.toPlainString()));
            order.setExecuted(Double.parseDouble(message.getString(CumQty.FIELD)));
            order.setAvgPx(new BigDecimal(message.getString(AvgPx.FIELD)));
        }

        OrdStatus ordStatus = (OrdStatus) message.getField(new OrdStatus());

        if (ordStatus.valueEquals(OrdStatus.REJECTED)) {
            order.setRejected(true);
            order.setOpen(0);
        } else if (ordStatus.valueEquals(OrdStatus.CANCELED)
                || ordStatus.valueEquals(OrdStatus.DONE_FOR_DAY)) {
            order.setCanceled(true);
            order.setOpen(0);
        } else if (ordStatus.valueEquals(OrdStatus.NEW)) {
            if (order.isNew()) {
                order.setNew(false);
            }
        }

        try {
            order.setMessage(message.getField(new Text()).getValue());
        } catch (FieldNotFound e) {
        }

        orderTableModel.updateOrder(order, message.getField(new ClOrdID()).getValue());
        observableOrder.update(order);

        if (fillSize.compareTo(BigDecimal.ZERO) > 0) {
            Execution execution = new Execution();
            execution.setExchangeID(sessionID + message.getField(new ExecID()).getValue());

            execution.setSymbol(message.getField(new Symbol()).getValue());
            execution.setQuantity(fillSize.intValue());
            if (message.isSetField(LastPx.FIELD)) {
                execution.setPrice(new BigDecimal(message.getString(LastPx.FIELD)));
            }
            Side side = (Side) message.getField(new Side());
            execution.setSide(FIXSideToSide(side));
            execution.setAccount(message.getField(new Account()).getValue());
            execution.setOrder(Long.parseLong(message.getField(new ClOrdID()).getValue()));
            execution.setStatus(String.valueOf(message.getField(new OrdStatus()).getValue()));
            executionTableModel.addExecution(execution);
        }
    }

    private boolean alreadyProcessed(ExecID execID, SessionID sessionID) {
        HashSet<ExecID> set = execIDs.get(sessionID);
        if (set == null) {
            set = new HashSet<>();
            set.add(execID);
            execIDs.put(sessionID, set);
            return false;
        } else {
            if (set.contains(execID))
                return true;
            set.add(execID);
            return false;
        }
    }

    public void handleOrderCancelReject(quickfix.fix44.OrderCancelReject orderReject) {
        try {
            switch (orderReject.getCxlRejResponseTo().getValue()) {
                case CxlRejResponseTo.ORDER_CANCEL_REQUEST:
                    handleCancelOrderReject(orderReject);
                    break;
            }
        } catch (Exception e) {
            log.error("handleOrderCancelReject throws: ", e);
        }
    }

    @SneakyThrows
    private void handleCancelOrderReject(quickfix.fix44.OrderCancelReject orderReject) {
        log.info("handleCancelOrderReject {}", LogUtils.formatFixMessageLog(orderReject));
        cancelReject(orderReject);
    }

    private void cancelReject(Message message) throws FieldNotFound {
        String id = message.getField(new ClOrdID()).getValue();
        Order order = orderTableModel.getOrder(id);
        if (order == null)
            return;

        if (order.getOriginalID() != null)
            order = orderTableModel.getOrder(order.getOriginalID());

        order.setMessage(message.isSetField(new Text()) ? message.getField(new Text()).getValue() : "");
        orderTableModel.updateOrder(order, message.getField(new OrigClOrdID()).getValue());
    }
}

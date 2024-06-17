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
import quickfix.fix42.OrderCancelReject;

import java.math.BigDecimal;
import static quickfix.examples.utils.ParseUtils.FIXSideToSide;

@RequiredArgsConstructor
@Slf4j
public class ExecutionReportFIX42Service {

    private final ObservableOrder observableOrder;
    private final ExecutionTableModel executionTableModel;
    private final OrderTableModel orderTableModel;

    @SneakyThrows
    public void handleExecutionReportFIX42Event(quickfix.fix42.ExecutionReport executionReport, SessionID sessionID) {
        switch (executionReport.getOrdStatus().getValue()) {
            case OrdStatus.NEW:
                handleNewOrderNotice(executionReport, sessionID);
                break;
            case OrdStatus.PARTIALLY_FILLED:
                handlePartialFillOrderNotice(executionReport, sessionID);
                break;
            case OrdStatus.FILLED:
                handleFillOrderNotice(executionReport, sessionID);
                break;
            case OrdStatus.CANCELED:
                handleCancelOrderNotice(executionReport, sessionID);
                break;
            case OrdStatus.PENDING_CANCEL:
                handlePendingCancelOrderNotice(executionReport, sessionID);
                break;
            case OrdStatus.REJECTED:
                handleRejectOrderNotice(executionReport, sessionID);
                break;
            case OrdStatus.PENDING_NEW:
                handlePendingNewOrderNotice(executionReport, sessionID);
                break;
            case OrdStatus.EXPIRED:
                handleExpiredOrderNotice(executionReport, sessionID);
                break;

            default:
                log.warn("handleExecutionReportEventResponse Unsupported OrdStatus {}", executionReport.getOrdStatus().getValue());
                break;
        }
    }

    @SneakyThrows
    public void handleNewOrderNotice(quickfix.fix42.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handleNewOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
        Order order = orderTableModel.getOrder(executionReport.getClOrdID().getValue());
        if (order == null) {
            log.warn("Order {} not found", executionReport.getClOrdID().getValue());
            return;
        }

        order.setNew(true);
        order.setMessage(executionReport.isSetText() ? executionReport.getText().getValue() : "");

        orderTableModel.updateOrder(order, executionReport.getClOrdID().getValue());
        observableOrder.update(order);
        log.info("finish handleNewOrderNotice !");
    }

    @SneakyThrows
    public void handlePartialFillOrderNotice(quickfix.fix42.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handlePartialFillOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
        Order order = orderTableModel.getOrder(executionReport.getClOrdID().getValue());
        if (order == null) {
            log.warn("Order {} not found", executionReport.getClOrdID().getValue());
            return;
        }

        BigDecimal fillSize = new BigDecimal("" + executionReport.getLastShares().getValue());
        order.setOpen(order.getOpen() - (int) Double.parseDouble(fillSize.toPlainString()));
        order.setExecuted(Double.parseDouble(executionReport.getString(CumQty.FIELD)));
        order.setAvgPx(Double.parseDouble(executionReport.getString(AvgPx.FIELD)));
        order.setMessage(executionReport.isSetText() ? executionReport.getText().getValue() : "");

        orderTableModel.updateOrder(order, executionReport.getClOrdID().getValue());
        observableOrder.update(order);

        Execution execution = new Execution();
        execution.setExchangeID(executionReport.isSetSecurityExchange() ? executionReport.getSecurityExchange().getValue() : "");

        execution.setSymbol(executionReport.getSymbol().getValue());
        execution.setQuantity(fillSize.intValue());
        if (executionReport.isSetLastPx()) execution.setPrice(executionReport.getLastPx().getValue());
        Side side = executionReport.getSide();
        execution.setSide(FIXSideToSide(side));
        execution.setAccount(executionReport.getAccount().getValue());
        execution.setOrder(Long.parseLong(executionReport.getClOrdID().getValue()));
        execution.setStatus(String.valueOf(OrdStatus.PARTIALLY_FILLED));
        executionTableModel.addExecution(execution);
        log.info("finish handlePartialFillOrderNotice !");
    }

    @SneakyThrows
    public void handleFillOrderNotice(quickfix.fix42.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handleFillOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
        Order order = orderTableModel.getOrder(executionReport.getClOrdID().getValue());
        if (order == null) {
            log.warn("Order {} not found", executionReport.getClOrdID().getValue());
            return;
        }

        order.setOpen(0);
        order.setExecuted(executionReport.getLastShares().getValue());
        order.setAvgPx(executionReport.getAvgPx().getValue());
        order.setMessage(executionReport.isSetText() ? executionReport.getText().getValue() : "");

        orderTableModel.updateOrder(order, executionReport.getClOrdID().getValue());
        observableOrder.update(order);

        Execution execution = new Execution();
        execution.setExchangeID(executionReport.getOrderID().getValue());

        execution.setSymbol(executionReport.getSymbol().getValue());
        execution.setQuantity((int) executionReport.getLastShares().getValue());
        if (executionReport.isSetLastPx()) execution.setPrice(executionReport.getLastPx().getValue());
        Side side = executionReport.getSide();
        execution.setSide(FIXSideToSide(side));
        execution.setAccount(executionReport.getAccount().getValue());
        execution.setOrder(Long.parseLong(executionReport.getClOrdID().getValue()));
        execution.setStatus(String.valueOf(OrdStatus.FILLED));
        executionTableModel.addExecution(execution);
        log.info("finish handleFillOrderNotice !");
    }

    @SneakyThrows
    public void handleCancelOrderNotice(quickfix.fix42.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handleCancelOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
        Order order = orderTableModel.getOrder(executionReport.getClOrdID().getValue());
        if (order == null) {
            log.warn("Order {} not found", executionReport.getClOrdID().getValue());
            return;
        }

        BigDecimal fillSize;
        if (executionReport.isSetLastShares()) {
            LastShares lastShares = executionReport.getLastShares();
            fillSize = new BigDecimal("" + lastShares.getValue());
        } else {
            LeavesQty leavesQty = executionReport.getLeavesQty();
            fillSize = new BigDecimal(order.getQuantity()).subtract(new BigDecimal("" + leavesQty.getValue()));
        }

        if (fillSize.compareTo(BigDecimal.ZERO) > 0) {
            order.setOpen(order.getOpen() - (int) Double.parseDouble(fillSize.toPlainString()));
            order.setExecuted(Double.parseDouble(executionReport.getString(CumQty.FIELD)));
            order.setAvgPx(Double.parseDouble(executionReport.getString(AvgPx.FIELD)));
        }

        order.setCanceled(true);
        order.setOpen(0);
        order.setMessage(executionReport.isSetText() ? executionReport.getText().getValue() : "");

        orderTableModel.updateOrder(order, executionReport.getClOrdID().getValue());
        observableOrder.update(order);
        log.info("finish handleCancelOrderNotice !");
    }

    public void handlePendingCancelOrderNotice(quickfix.fix42.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handlePendingCancelOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
        // TODO handle logic
        log.info("finish handlePendingCancelOrderNotice !");
    }

    public void handleRejectOrderNotice(quickfix.fix42.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handleRejectOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
        // TODO handle logic
        log.info("finish handleRejectOrderNotice !");
    }

    public void handlePendingNewOrderNotice(quickfix.fix42.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handlePendingNewOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
        // TODO handle logic
        log.info("finish handlePendingNewOrderNotice !");
    }

    public void handleExpiredOrderNotice(quickfix.fix42.ExecutionReport executionReport, SessionID sessionID) {
        log.info("handleExpiredOrderNotice {}", LogUtils.formatFixMessageLog(executionReport));
        // TODO handle logic
        log.info("finish handleExpiredOrderNotice !");
    }

    @SneakyThrows
    public void handleOrderCancelReject(OrderCancelReject orderReject) {
        log.info("handleOrderCancelReject {}", LogUtils.formatFixMessageLog(orderReject));
        String clOrdId = orderReject.getClOrdID().getValue();
        Order order = orderTableModel.getOrder(clOrdId);
        if (order == null)
            return;

        if (order.getOriginalID() != null)
            order = orderTableModel.getOrder(order.getOriginalID());

        order.setMessage(orderReject.isSetText() ? orderReject.getText().getValue() : "");
        orderTableModel.updateOrder(order, clOrdId);
        log.info("finish handleOrderCancelReject !");
    }
}

package quickfix.examples;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.UIManager;
import quickfix.examples.app.ApplicationRunner;
import quickfix.examples.model.ObservableOrder;
import quickfix.examples.model.table.ExecutionTableModel;
import quickfix.examples.model.table.OrderBookTableModel;
import quickfix.examples.model.table.OrderTableModel;
import quickfix.examples.service.ExecutionReportFIX42Service;
import quickfix.examples.service.ExecutionReportFIX44Service;
import quickfix.examples.service.RoutingService;
import lombok.extern.slf4j.Slf4j;
import org.quickfixj.jmx.JmxExporter;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.examples.ui.frame.AppFrame;
import quickfix.examples.utils.CSVUtils;

/**
 * Entry point for the Banzai application.
 */
@Slf4j
public class Main {
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static Main main;
    private boolean initiatorStarted = false;
    private Initiator initiator = null;

    public void loadSymbols() {
        CSVUtils.symbols = CSVUtils
                .importSymbolCSV("D:\\progaram-language\\java\\fix-client-guide\\src\\main\\resources\\quickfix\\examples\\symbols.csv")
                .stream().map(CSVUtils.Symbol::getSymbolCode).toArray();

        CSVUtils.symbolList = CSVUtils
                .importSymbolCSV("D:\\progaram-language\\java\\fix-client-guide\\src\\main\\resources\\quickfix\\examples\\symbols.csv")
                .stream().map(CSVUtils.Symbol::getSymbolCode).collect(Collectors.toList());
    }

    public Main(String[] args) throws Exception {

        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = Main.class.getResourceAsStream("config.cfg");
        } else if (args.length == 1) {
            inputStream = Files.newInputStream(Paths.get(args[0]));
        }

        if (inputStream == null) {
            log.warn("usage: " + Main.class.getName() + " [configFile].");
            return;
        }

        loadSymbols();
        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();

        OrderTableModel orderTableModel = new OrderTableModel();
        OrderBookTableModel orderBookTableModel = new OrderBookTableModel();
        ExecutionTableModel executionTableModel = new ExecutionTableModel();
        ObservableOrder observableOrder = new ObservableOrder();
        ExecutionReportFIX42Service executionReportFIX42Service = new ExecutionReportFIX42Service(observableOrder, executionTableModel, orderTableModel);
        ExecutionReportFIX44Service executionReportFIX44Service = new ExecutionReportFIX44Service(observableOrder, executionTableModel, orderTableModel);
        RoutingService routingService = new RoutingService(
                executionReportFIX42Service,
                executionReportFIX44Service,
                orderTableModel
        );
        ApplicationRunner applicationRunner = new ApplicationRunner(routingService);
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(false, false, false, false);
        MessageFactory messageFactory = new DefaultMessageFactory();

        initiator = new SocketInitiator(applicationRunner, messageStoreFactory, settings, logFactory,
                messageFactory);

        JmxExporter exporter = new JmxExporter();
        exporter.register(initiator);

        JFrame frame = new AppFrame(
                orderTableModel,
                orderBookTableModel,
                executionTableModel,
                applicationRunner,
                observableOrder,
                routingService
        );
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public synchronized void logon() {
        try {
            if (!initiatorStarted) {
                initiator.start();
                initiatorStarted = true;
            } else {
                for (SessionID sessionId : initiator.getSessions()) {
                    Session.lookupSession(sessionId).logon();
                }
            }
        } catch (Exception e) {
            log.error("Logon failed", e);
        }
    }

    public void logout() {
        for (SessionID sessionId : initiator.getSessions()) {
            Session.lookupSession(sessionId).logout("user requested");
        }
    }

    public static Main get() {
        return main;
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

        main = new Main(args);
        if (!System.getProperties().containsKey("openfix")) {
            main.logon();
        }
        shutdownLatch.await();
    }
}

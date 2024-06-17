/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 ******************************************************************************/
package quickfix.examples.app;

import quickfix.examples.model.LogonEvent;
import quickfix.examples.service.RoutingService;
import quickfix.examples.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import quickfix.*;

import java.util.Observable;
import java.util.Observer;

@Slf4j
@RequiredArgsConstructor
public class ApplicationRunner extends MessageCracker implements quickfix.Application {

    private final RoutingService routingService;

    private final ObservableLogon observableLogon = new ObservableLogon();

    // Init connection ~ TCP/Socket
    @Override
    public void onCreate(SessionID sessionId) {
        log.info("Session create: {}", sessionId);
    }

    // After handshake initiators <--> acceptor
    @Override
    public void onLogon(SessionID sessionId) {
        log.info("Session logon: {}", sessionId);
        observableLogon.logon(sessionId);
    }

    // Close connection
    @Override
    public void onLogout(SessionID sessionId) {
        log.info("Session logout : {}", sessionId);
        observableLogon.logoff(sessionId);
    }

    // Admin message
    // Heartbeat,..
    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        if (message instanceof quickfix.fix42.Heartbeat ||
                message instanceof quickfix.fix44.Heartbeat) {
            log.debug("Message to Admin: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
        } else log.info("Message to Admin: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        if (message instanceof quickfix.fix42.Heartbeat ||
                message instanceof quickfix.fix44.Heartbeat) {
            log.debug("Message from Admin: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
        } else log.info("Message from Admin: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        log.info("Message to App: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionId);
    }

    public void onMessage(Message message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        routingService.processFIXResponseMessage(message, sessionID);
    }

    public void addLogonObserver(Observer observer) {
        observableLogon.addObserver(observer);
    }

    private static class ObservableLogon extends Observable {
        public void logon(SessionID sessionID) {
            setChanged();
            notifyObservers(new LogonEvent(sessionID, true));
            clearChanged();
        }

        public void logoff(SessionID sessionID) {
            setChanged();
            notifyObservers(new LogonEvent(sessionID, false));
            clearChanged();
        }
    }
}

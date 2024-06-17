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

package quickfix.examples.enumerate;

import java.util.HashMap;
import java.util.Map;

public class OrderTIF {
    static private final Map<String, OrderTIF> known = new HashMap<>();
    static public final OrderTIF DAY = new OrderTIF("Day");

    static private final OrderTIF[] array = { DAY };

    private final String name;

    private OrderTIF(String name) {
        this.name = name;
        synchronized (OrderTIF.class) {
            known.put(name, this);
        }
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    static public Object[] toArray() {
        return array;
    }

    public static OrderTIF parse(String type) throws IllegalArgumentException {
        OrderTIF result = known.get(type);
        if (result == null) {
            throw new IllegalArgumentException
            ("OrderTIF: " + type + " is unknown.");
        }
        return result;
    }
}

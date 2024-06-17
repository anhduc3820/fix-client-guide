package quickfix.examples.model;

import java.util.Observable;
import java.util.Observer;

public class ObservableOrder extends Observable {

    public void update(Order order) {
        setChanged();
        notifyObservers(order);
        clearChanged();
    }

    public void addOrderObserver(Observer observer) {
        addObserver(observer);
    }
}

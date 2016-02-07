package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Model extends Thread {
    private IntegerProperty intProperty;
    private StringProperty strProperty;

    public Model() {
      intProperty = new SimpleIntegerProperty(this, "int", 0);
        strProperty = new SimpleStringProperty(this, "String", "test");
      setDaemon(true);
    }

    public int getInt() {
      return intProperty.get();
    }

    public IntegerProperty intProperty() {
      return intProperty;
    }

    public StringProperty StringProperty() {
        return strProperty;
    }

    @Override
    public void run() {
      while (true) {
        intProperty.set(intProperty.get() + 1);
          strProperty.setValue("testing");
      }
    }
  }
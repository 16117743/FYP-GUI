package com.Interface;

import com.framework.ScreensController;

public interface ControlledScreen {
    //This method will allow the injection of the Parent ScreenPane
    public void setScreenParent(ScreensController screenPage);

}
package Interface;

/**
 * Created by user on 04/03/2016.
 */
public interface InterfaceDJ {
    public void DJplayMe();
    public void DJskipMe();
    public void DJpauseMe();
    /***************************/
    public void DJfadeMeIn(float deltaTime);
    public void DJfadeMeOut(float deltaTime);
    public void DJDoSomething();
}

package cn.laoshini.dk.robot.fsm;

/**
 * @author fagarine
 */
public abstract class AbstractFsmRobot<S extends IFsmState> implements IFsmRobot<S> {

    private S currentState;

    @Override
    public S currentState() {
        return currentState;
    }

    @Override
    public void changeState(S newState) {
        S state = currentState();
        if (state != null) {
            state.exit(this);
        }

        this.currentState = newState;

        this.currentState.enter(this);
    }
}

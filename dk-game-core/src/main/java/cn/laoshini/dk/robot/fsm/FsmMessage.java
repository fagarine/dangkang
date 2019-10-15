package cn.laoshini.dk.robot.fsm;

/**
 * @author fagarine
 */
public class FsmMessage<M> {

    private M message;

    public static <RealMessageType> FsmMessage<RealMessageType> build(RealMessageType message) {
        FsmMessage<RealMessageType> fsmMessage = new FsmMessage<>();
        fsmMessage.setMessage(message);
        return fsmMessage;
    }

    public M getMessage() {
        return message;
    }

    public void setMessage(M message) {
        this.message = message;
    }
}

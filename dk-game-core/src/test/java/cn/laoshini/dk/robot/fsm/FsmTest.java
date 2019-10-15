package cn.laoshini.dk.robot.fsm;

import org.junit.Before;
import org.junit.Test;

/**
 * @author fagarine
 */
public class FsmTest {

    private IStateMachine stateMachine;

    @Before
    public void init() {
        stateMachine = new MonsterStateMachine();
        stateMachine.initialize();
    }

    @Test
    public void testFsm() throws Exception {
        while (true) {
            stateMachine.tick();

            Thread.sleep(500);
        }
    }
}

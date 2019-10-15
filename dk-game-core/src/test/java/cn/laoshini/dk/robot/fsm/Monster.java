package cn.laoshini.dk.robot.fsm;

import org.apache.commons.lang3.RandomUtils;

import static cn.laoshini.dk.robot.fsm.MonsterState.BACK;
import static cn.laoshini.dk.robot.fsm.MonsterState.IDLE;

/**
 * @author fagarine
 */
public class Monster extends AbstractFsmRobot<MonsterState> {

    private String name;

    private int count;

    public Monster(String name) {
        this.name = name;
    }

    @Override
    public void tick() {
        if (currentState() == null) {
            changeState(IDLE);
            return;
        }

        MonsterState state = currentState();
        if (count == 0) {
            changeState(nextState());
            count = RandomUtils.nextInt(1, 5);
        } else {
            state.refresh(this);
            count--;
        }
    }

    private MonsterState nextState() {
        MonsterState state = currentState();
        if (BACK.equals(state)) {
            return IDLE;
        }

        int index = RandomUtils.nextInt(0, MonsterState.values().length);
        return MonsterState.values()[index];
    }

    public String name() {
        return name;
    }
}

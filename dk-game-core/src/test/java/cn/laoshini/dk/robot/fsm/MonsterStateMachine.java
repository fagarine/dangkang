package cn.laoshini.dk.robot.fsm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fagarine
 */
public class MonsterStateMachine implements IStateMachine {

    private static final String[] NAMES = { "妙蛙种子", "水箭龟", "小火龙", "绿毛虫", "大针蜂" };

    private List<Monster> monsters;

    @Override
    public void initialize() {
        monsters = new ArrayList<>(NAMES.length);

        for (String name : NAMES) {
            monsters.add(new Monster(name));
        }
    }

    @Override
    public void tick() {
        for (Monster monster : monsters) {
            monster.tick();
        }
    }
}

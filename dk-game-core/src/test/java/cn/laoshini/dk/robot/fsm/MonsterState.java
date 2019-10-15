package cn.laoshini.dk.robot.fsm;

/**
 * @author fagarine
 */
public enum MonsterState implements IFsmState<Monster> {

    IDLE("空闲"),
    PATROL("巡逻") {
        @Override
        public void refresh(Monster monster) {
            System.out.println(String.format("[%s]巡逻到了一个新地点，它好像发现了什么...", monster.name()));
        }
    },
    FOLLOW("跟随") {
        @Override
        public void refresh(Monster monster) {
            System.out.println(String.format("[%s]继续跟随目标移动，并大喊：狗贼休走，再战三百回合...", monster.name()));
        }
    },
    ATTACK("攻击") {
        @Override
        public void refresh(Monster monster) {
            System.out.println(String.format("[%s]发起攻击...", monster.name()));
        }
    },
    BACK("返回") {
        @Override
        public void refresh(Monster monster) {
            System.out.println(String.format("[%s]返回中...", monster.name()));
        }

        @Override
        public void exit(Monster monster) {
            super.exit(monster);
            System.out.println(String.format("[%s]返回原点，即将重新进入空闲状态", monster.name()));
        }
    };

    @Override
    public void enter(Monster monster) {
        System.out.println(String.format("[%s]进入[%s]状态", monster.name(), this.cnName));
    }

    @Override
    public void refresh(Monster monster) {
        System.out.println(String.format("[%s]处于[%s]状态中...", monster.name(), this.cnName));
    }

    @Override
    public void exit(Monster monster) {
        System.out.println(String.format("[%s]退出[%s]状态", monster.name(), this.cnName));
    }

    private String cnName;

    MonsterState(String cnName) {
        this.cnName = cnName;
    }

    public String getCnName() {
        return cnName;
    }
}

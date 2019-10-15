package cn.laoshini.dk.condition;

/**
 * 记录条件匹配信息
 *
 * @author fagarine
 */
public class ConditionRecord {

    private StringBuilder message;

    public ConditionRecord(String message) {
        this.message = new StringBuilder(message);
    }

    public static ConditionRecord empty() {
        return new ConditionRecord("");
    }

    public void append(ConditionRecord record) {
        if (record.notEmpty()) {
            if (notEmpty()) {
                message.append(", ");
            }
            message.append(record.getMessage());
        }
    }

    public boolean isEmpty() {
        return message == null || message.length() == 0;
    }

    public boolean notEmpty() {
        return !isEmpty();
    }

    @Override
    public String toString() {
        return isEmpty() ? "" : message.toString();
    }

    public StringBuilder getMessage() {
        return message;
    }
}

package cn.laoshini.dk.register;

import java.util.List;

import cn.laoshini.dk.domain.common.Tuple;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.MessageDtoClassHolder;

/**
 * 游戏消息DTO类注册器
 * <p>
 * 消息DTO，是指：游戏自定义消息类{@link cn.laoshini.dk.net.msg.ICustomMessage}的具体消息类型{@link cn.laoshini.dk.net.msg.ICustomDto}
 * </p>
 *
 * @author fagarine
 */
public interface IMessageDtoRegister extends IFunctionRegister {

    /**
     * 获取DTO消息类扫描器，该扫描器返回结果为消息id与其对应的DTO类
     * <p>
     * 注意：该方法是为DTO类的自动扫描与注册服务的，如果用户不需要该服务，又需要注册DTO类，
     * 可以直接调用{@link MessageDtoClassHolder#registerDtoClass(int, Class)}方法注册，并不需要使用本接口
     * </p>
     *
     * @return 如果要使用系统自动扫描与注册，该方法不应该返回null
     */
    IClassScanner<Tuple<Integer, Class<?>>> dtoScanner();

    @Override
    default void action(ClassLoader classLoader) {
        if (dtoScanner() == null) {
            throw new BusinessException("dto.scanner.empty", "DTO类扫描器不能为空");
        }

        // 需要将DTO类转换为Tuple<Integer, Class<?>>，所以converter不能为空
        if (dtoScanner().converter() == null) {
            throw new BusinessException("scanner.converter.empty", "DTO类扫描器的转换器不能为空");
        }

        List<Tuple<Integer, Class<?>>> customDtoTuples = dtoScanner().findClasses(classLoader);
        for (Tuple<Integer, Class<?>> tuple : customDtoTuples) {
            MessageDtoClassHolder.registerDtoClass(tuple.getV1(), tuple.getV2());
        }
    }

    @Override
    default String functionName() {
        return "消息DTO";
    }
}

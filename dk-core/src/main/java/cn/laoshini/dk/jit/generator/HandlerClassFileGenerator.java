package cn.laoshini.dk.jit.generator;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.BeansException;
import org.springframework.util.StringUtils;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.constant.BeanTypeEnum;
import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.dto.HandlerExpDescriptorDTO;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.JitException;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.jit.type.HandlerBean;
import cn.laoshini.dk.jit.type.ITypeBean;
import cn.laoshini.dk.manager.HandlerExpressionManager;
import cn.laoshini.dk.net.handler.IHttpMessageHandler;
import cn.laoshini.dk.net.handler.IMessageHandler;
import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
public class HandlerClassFileGenerator extends AbstractClassFileGenerator {

    private int messageId;

    private List<Class<?>> depends;

    private HandlerExpDescriptorDTO descriptor;

    public HandlerClassFileGenerator(HandlerBean handlerBean, ClassLoader classLoader,
            HandlerExpDescriptorDTO descriptor) {
        super(handlerBean, classLoader);
        this.descriptor = descriptor;
        messageId = handlerBean.getMessageId();
    }

    @Override
    protected StringBuilder buildImportText() {
        StringBuilder importStr = new StringBuilder();
        importStr.append(IMT).append(JSON.class.getName()).append(";").append(PG);
        importStr.append(IMT).append(MessageHandle.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(SpringContextHolder.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(GameCodeEnum.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(GameSubject.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(HandlerExpDescriptorDTO.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(MessageException.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(HandlerExpressionManager.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(IMessageHandler.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(IHttpMessageHandler.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(ReqMessage.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(RespMessage.class.getName()).append(";").append(LS);
        importStr.append(IMT).append(StringUtil.class.getName()).append(";").append(LS);
        return importStr.append(LS);
    }

    @Override
    protected StringBuilder buildJavaFileContent() {
        StringBuilder content = new StringBuilder();
        HandlerBean handlerBean = (HandlerBean) compositeBean;

        // 检查依赖项
        checkHandlerDepends(handlerBean);

        // handler注解信息
        content.append("@MessageHandle(").append("id = ").append(messageId);
        if (handlerBean.isAllowGuestRequest()) {
            content.append(", allowGuestRequest = true");
        }
        if (!handlerBean.isSequential()) {
            content.append(", sequential = false");
        }
        content.append(")").append(LS);

        // 类声明
        String protocol = handlerBean.getProtocol();
        boolean isHttpHandler = isHttpHandler(protocol);
        content.append("public class ").append(className).append(IMP);
        if (isHttpHandler) {
            content.append("IHttpMessageHandler");
        } else {
            content.append("IMessageHandler");
        }
        appendDataType(content, handlerBean.getDataType());
        content.append(" {").append(PG);

        // 脚本代码参数信息
        String jsonStr = JSON.toJSONString(descriptor).replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
        content.append("    private String jsonStr = \"").append(jsonStr).append("\";").append(PG)
                .append("    private HandlerExpDescriptorDTO descriptor;").append(PG);

        // 依赖组件
        if (depends != null) {
            for (Class<?> depend : depends) {
                String paramName = StringUtils.uncapitalize(depend.getSimpleName());
                content.append(INDENT).append("private ").append(depend.getName()).append(" ").append(paramName)
                        .append(";").append(PG);
            }
        }

        // handler方法实现
        if (isHttpHandler) {
            content.append(ORI).append("public RespMessage call(ReqMessage");
            appendDataType(content, handlerBean.getDataType());
            content.append(" req, ").append(GameSubject.class.getSimpleName())
                    .append(" subject) throws MessageException {").append(LS);
            content.append(buildLogicText(true)).append(END);
        }
        content.append(ORI).append("public void action(ReqMessage");
        appendDataType(content, handlerBean.getDataType());
        content.append(" req, ").append(GameSubject.class.getSimpleName()).append(" subject) throws MessageException {")
                .append(LS);
        if (isHttpHandler) {
            content.append(I2).append("throw new MessageException (GameCodeEnum.UNSUPPORTED_TCP_PROTOCOL,")
                    .append(" \"handler.protocol.unsupported\", \"该Handler不支持来自TCP的消息请求\");");
        } else {
            content.append(buildLogicText(false));
        }
        content.append(END).append("}");
        return content;
    }

    private StringBuilder buildLogicText(boolean isHttpHandler) {
        StringBuilder logic = new StringBuilder();
        logic.append(I2)
                .append("HandlerExpressionManager manager = SpringContextHolder.getBean(HandlerExpressionManager.class);")
                .append(LS);
        logic.append(I2).append("if (descriptor == null && StringUtil.isNotEmptyString(jsonStr)) {").append(LS);
        logic.append(I2).append(INDENT).append("descriptor = JSON.parseObject(jsonStr, HandlerExpDescriptorDTO.class);")
                .append(LS).append(I2).append(INDENT).append("manager.registerExpHandler(descriptor);").append(LS)
                .append(I2).append(INDENT).append("jsonStr = null;").append(LS).append(I2).append("}").append(PG);
        logic.append(I2).append("if (descriptor != null) {").append(LS).append(I2).append(INDENT);
        if (isHttpHandler) {
            logic.append("return manager.runExpHandlerCall(req, subject);");
        } else {
            logic.append("manager.runExpHandlerAction(req, subject);");
        }
        logic.append(LS).append(I2).append("}");

        if (isHttpHandler) {
            logic.append(LS).append(RT).append("null;");
        }
        return logic;
    }

    private void appendDataType(StringBuilder content, String dataType) {
        if (StringUtil.isNotEmptyString(dataType)) {
            content.append("<").append(dataType).append(">");
        }
    }

    private void checkHandlerDepends(HandlerBean handlerBean) {
        if (!handlerBean.getVal().isEmpty()) {
            depends = new ArrayList<>(handlerBean.getVal().size());

            for (ITypeBean typeBean : handlerBean.getVal()) {
                // 检查依赖项
                Class<?> clazz = typeBean.getValueType();
                if (!BeanTypeEnum.ORDINARY.equals(typeBean.getType())) {
                    throw new JitException("handler.depend.error", "Handler类的依赖类型错误:" + clazz);
                }

                if (clazz == null) {
                    throw new JitException("handler.depend.null", "Handler类的依赖类型不能为空:" + typeBean.getName());
                }

                try {
                    SpringContextHolder.getBean(clazz);
                } catch (BeansException e) {
                    throw new JitException("handler.depend.null", "未找到Handler类的依赖对象:" + clazz.getName());
                }

                depends.add(clazz);
            }
        }
    }

    private boolean isHttpHandler(String protocol) {
        return GameServerProtocolEnum.HTTP.name().equalsIgnoreCase(protocol);
    }

    @Override
    public String candidateClassNamePrefix() {
        return "Message" + messageId + "Handler";
    }
}

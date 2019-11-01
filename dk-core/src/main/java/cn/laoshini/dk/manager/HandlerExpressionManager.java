package cn.laoshini.dk.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.dao.IDefaultDao;
import cn.laoshini.dk.dao.query.BeanQueryCondition;
import cn.laoshini.dk.dao.query.QueryUtil;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.dto.ExpressionBlockDTO;
import cn.laoshini.dk.domain.dto.HandlerExpDescriptorDTO;
import cn.laoshini.dk.entity.HandlerExpDescriptor;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.expression.ExpressionLogicFactory;
import cn.laoshini.dk.expression.IExpressionLogic;
import cn.laoshini.dk.net.MessageHandlerHolder;
import cn.laoshini.dk.net.handler.ExpressionMessageHandler;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.net.msg.RespMessage;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * 消息处理Handler表达式管理
 *
 * @author fagarine
 */
@Component
public class HandlerExpressionManager {

    @FunctionDependent(nullable = true, afterExecute = "loadHandlerExpression")
    private IDefaultDao defaultDao;

    @Value("${dk.generate.handler:#{dangKangGenerateProperties.handler}}")
    private boolean generateHandler;

    /**
     * 表达式处理逻辑，key: 消息id
     */
    private Map<Integer, ExpressionMessageHandler> expLogicMap = new HashMap<>();

    private void loadHandlerExpression() {
        List<HandlerExpDescriptor> list = defaultDao.selectAllEntity(HandlerExpDescriptor.class);

        if (CollectionUtil.isNotEmpty(list)) {
            for (HandlerExpDescriptor hed : list) {
                registerExpHandler(entity2VO(hed));
            }
        }
    }

    public void registerExpHandler(HandlerExpDescriptorDTO descriptorVO) {
        IExpressionLogic logic = ExpressionLogicFactory.newExpressionLogic(descriptorVO);
        ExpressionMessageHandler expHandler = new ExpressionMessageHandler(descriptorVO, logic);
        expLogicMap.put(descriptorVO.getMessageId(), expHandler);

        // 注册到Handler管理类
        MessageHandlerHolder.registerExpHandler(descriptorVO.getMessageId(), expHandler);
    }

    private void unregisterExpHandler(int messageId) {
        expLogicMap.remove(messageId);

        MessageHandlerHolder.unregisterHandler(messageId);
    }

    public void runExpHandlerAction(ReqMessage req, GameSubject subject) {
        ExpressionMessageHandler messageHandler = getExpHandlerOnCheck(req.getId());
        messageHandler.action(req, subject);
    }

    public RespMessage runExpHandlerCall(ReqMessage req, GameSubject subject) {
        ExpressionMessageHandler messageHandler = getExpHandlerOnCheck(req.getId());
        return messageHandler.call(req, subject);
    }

    private ExpressionMessageHandler getExpHandlerOnCheck(int messageId) {
        ExpressionMessageHandler messageHandler = expLogicMap.get(messageId);
        if (messageHandler == null) {
            throw new BusinessException("handler.no.expression",
                    String.format("消息id为 [%d] 的Handler没有设置表达式", messageId));
        }
        return messageHandler;
    }

    /**
     * 已支持使用表达式的消息id
     *
     * @return 返回所有已支持的消息id
     */
    public Collection<Integer> supportedMessageIds() {
        return expLogicMap.keySet();
    }

    /**
     * 获取指定消息id的Handler的表达式描述信息
     *
     * @param messageId 消息id
     * @return 表达式描述信息VO
     */
    public HandlerExpDescriptorDTO getExpDescriptorByMessageId(int messageId) {
        BeanQueryCondition condition = QueryUtil.newBeanQueryCondition("messageId", messageId);
        return entity2VO(defaultDao.selectEntity(HandlerExpDescriptor.class, condition));
    }

    public void saveHandlerExpression(HandlerExpDescriptorDTO descriptorVO) {
        defaultDao.saveEntity(vo2Entity(descriptorVO));

        // 注册
        registerExpHandler(descriptorVO);
    }

    public void deleteHandlerExpression(int id) {
        BeanQueryCondition condition = QueryUtil.newBeanQueryCondition("id", id);
        HandlerExpDescriptor descriptor = defaultDao.selectEntity(HandlerExpDescriptor.class, condition);
        if (descriptor == null) {
            throw new BusinessException("exp.not.exists", String.format("找不到id为[%d]的Handler表达式记录", id));
        }

        defaultDao.deleteEntity(descriptor);

        // 移除注册信息
        unregisterExpHandler(descriptor.getMessageId());
    }

    private HandlerExpDescriptorDTO entity2VO(HandlerExpDescriptor descriptor) {
        if (descriptor != null) {
            HandlerExpDescriptorDTO vo = new HandlerExpDescriptorDTO();
            vo.setId(descriptor.getId());
            vo.setMessageId(descriptor.getMessageId());
            vo.setProtocol(descriptor.getProtocol());
            vo.setDataType(descriptor.getDataType());
            vo.setExpressionType(descriptor.getExpressionType());
            vo.setExpressionBlocks(JSON.parseArray(descriptor.getExpressions(), ExpressionBlockDTO.class));
            return vo;
        }
        return null;
    }

    private HandlerExpDescriptor vo2Entity(HandlerExpDescriptorDTO vo) {
        if (vo != null) {
            HandlerExpDescriptor descriptor = new HandlerExpDescriptor();
            descriptor.setId(vo.getId());
            descriptor.setMessageId(descriptor.getMessageId());
            descriptor.setProtocol(vo.getProtocol());
            descriptor.setDataType(vo.getDataType());
            descriptor.setExpressionType(vo.getExpressionType());
            descriptor.setExpressions(JSON.toJSONString(vo.getExpressionBlocks()));
            return descriptor;
        }
        return null;
    }
}

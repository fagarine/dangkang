package cn.laoshini.dk.expression;

import java.util.ArrayList;
import java.util.List;

import cn.laoshini.dk.constant.ExpressionConstant;
import cn.laoshini.dk.domain.dto.ExpressionBlockDTO;
import cn.laoshini.dk.domain.dto.ExpressionDescriptorDTO;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * @author fagarine
 */
public abstract class BaseExpressionLogic implements IExpressionLogic {

    private ExpressionConstant.ExpressionTypeEnum expressionType;

    protected List<ExpressionBlockDTO> blocks;

    private List<IExpressionProcessor> processors;

    public BaseExpressionLogic(ExpressionConstant.ExpressionTypeEnum expressionType) {
        this.expressionType = expressionType;

        initProcessors();
    }

    protected void initProcessors() {
        int size = calcProcessorSize();
        processors = new ArrayList<>(size);
        if (size > 0) {
            BaseExpressionProcessor processor;
            for (ExpressionBlockDTO blockVO : blocks) {
                if (blockVO.size() > 0) {
                    for (ExpressionDescriptorDTO descriptorVO : blockVO.getExpDescriptors()) {
                        processor = ExpressionLogicFactory.newExpressionProcessor(expressionType);
                        processor.setDescriptor(descriptorVO);
                        processors.add(processor);
                    }
                }
            }
        }
    }

    protected int calcProcessorSize() {
        int size = 0;
        if (CollectionUtil.isNotEmpty(blocks)) {
            for (ExpressionBlockDTO blockVO : blocks) {
                size += blockVO.size();
            }
        }
        return size;
    }

    @Override
    public List<IExpressionProcessor> processors() {
        return processors;
    }

    public ExpressionConstant.ExpressionTypeEnum getExpressionType() {
        return expressionType;
    }

    public List<ExpressionBlockDTO> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<ExpressionBlockDTO> blocks) {
        this.blocks = blocks;
    }
}

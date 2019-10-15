package cn.laoshini.dk.excel.constraint;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.DataValidationConstraint;

/**
 * excel带下拉列表的表头描述
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
public class ExcelListHeader extends ExcelHeader {

    /**
     * 下拉列表的选项
     */
    private String[] options;

    /**
     * 下拉列表选项对应的值，用来实现选项与值不同的功能，暂时还没有实现
     */
    private Object[] values;

    public ExcelListHeader(String name, String[] options) {
        super(name);
        this.options = options;

        if (ArrayUtils.isEmpty(options)) {
            throw new IllegalArgumentException("excel下拉列表选项不能为空");
        }
    }

    public ExcelListHeader(String name, boolean editable, String[] options) {
        super(name, editable);
        this.options = options;
    }

    public ExcelListHeader(String name, String[] options, Object[] values) {
        super(name);

        this.options = options;
        this.values = values;

        if (ArrayUtils.isEmpty(options) || ArrayUtils.isEmpty(values)) {
            throw new IllegalArgumentException("excel下拉列表选项不能为空");
        }
    }

    public ExcelListHeader(String name, LinkedHashMap<String, Object> optionMap) {
        super(name);

        if (optionMap == null || optionMap.size() == 0) {
            throw new IllegalArgumentException("excel下拉列表选项不能为空");
        }

        int index = 0;
        options = new String[optionMap.size()];
        values = new Object[optionMap.size()];
        for (Map.Entry<String, Object> entry : optionMap.entrySet()) {
            options[index] = entry.getKey();
            values[index++] = entry.getValue();
        }
    }

    @Override
    public int getValidationType() {
        return DataValidationConstraint.ValidationType.LIST;
    }

    public String[] getOptions() {
        return options;
    }

    public Object[] getValues() {
        return values;
    }
}

package cn.laoshini.dk.jit.generator;

import java.nio.ByteBuffer;

import org.springframework.util.StringUtils;

import cn.laoshini.dk.constant.BeanTypeEnum;
import cn.laoshini.dk.jit.type.CompositeBean;
import cn.laoshini.dk.jit.type.ITypeBean;

/**
 * 自定义格式消息的DTO类源文件生成器的抽象
 *
 * @author fagarine
 */
class AbstractCustomDtoClassFileGenerator extends AbstractClassFileGenerator {

    protected static final String JDK_BYTE_BUFF = ByteBuffer.class.getName();

    /**
     * 生成类使用的缓冲区的类名
     */
    private String byteBufferClassName;

    /**
     * 生成类实现的DTO接口的类名
     */
    private String dtoInterfaceClassName;

    /**
     * 记录生成类的write()方法
     */
    private StringBuilder writeMethodStr = new StringBuilder();

    /**
     * 记录生成类的read()方法
     */
    private StringBuilder readMethodStr = new StringBuilder();

    /**
     * 如果是JDK自带的缓冲区，需要生成byteSize()方法，记录该方法的内容
     */
    private StringBuilder byteSizeStr = new StringBuilder();

    /**
     * 记录当前正在生成的参数的详细类型（包含参数名称）
     */
    private String detailedParamStr;

    /**
     * 记录参数的类型（包含泛型信息）
     */
    private String paramTypeStr;

    /**
     * 记录参数名称
     */
    private String paramName;

    AbstractCustomDtoClassFileGenerator(CompositeBean compositeBean, ClassLoader classLoader,
            String byteBufferClassName, String dtoInterfaceClassName) {
        super(compositeBean, classLoader);
        this.byteBufferClassName = byteBufferClassName;
        this.dtoInterfaceClassName = dtoInterfaceClassName;
    }

    @Override
    protected StringBuilder buildImportText() {
        StringBuilder importStr = new StringBuilder(IMT).append(byteBufferClassName).append(";").append(PG);
        importStr.append(IMT).append(dtoInterfaceClassName).append(";").append(PG);
        return importStr;
    }

    /**
     * 生成源文件内容
     */
    @Override
    protected StringBuilder buildJavaFileContent() {
        StringBuilder content = new StringBuilder("public class ").append(className).append(" implements ICustomDto {")
                .append(PG);

        StringBuilder paramStr = new StringBuilder();
        StringBuilder methodStr = new StringBuilder();
        StringBuilder hashContent = new StringBuilder();
        StringBuilder toStringContent = new StringBuilder();
        int index = 1;
        readMethodStr.append(ORI).append("public void read(ByteBuffer b) {").append(LS);
        writeMethodStr.append(ORI).append("public void write(ByteBuffer b) {").append(LS);
        byteSizeStr.append(ORI).append("public int byteSize() {").append(LS).append(RT);
        hashCodeStr.append(ORI).append("public int hashCode() {").append(LS);
        toStringStr.append(ORI).append("public String toString() {").append(LS);
        for (ITypeBean typeBean : compositeBean.getVal()) {
            appendParam(typeBean, index++, paramStr);
            appendMethod(typeBean, methodStr);

            if (hashContent.length() > 0) {
                byteSizeStr.append(" + ");
                hashContent.append(" + ");
                toStringContent.append(" + \", \"");
            }
            byteSizeStr.append("byteSize(").append(paramName).append(")");
            hashContent.append("hashCode(").append(paramName).append(")");
            toStringContent.append(" + \"").append(paramName);
            if (BeanTypeEnum.STRING.equals(typeBean.getType())) {
                toStringContent.append("=\'\" + ").append(paramName).append(" + \'\\\'\'");
            } else {
                toStringContent.append("=\" + ").append(paramName);
            }
        }
        readMethodStr.append(CL);
        writeMethodStr.append(CL);
        byteSizeStr.append(";").append(END);
        hashCodeStr.append(RT).append(hashContent).append(";").append(END);
        toStringStr.append(RT).append("\"").append(className).append("{\"").append(toStringContent).append(" + \"}\"")
                .append(";").append(END);

        content.append(paramStr).append(readMethodStr).append(writeMethodStr);
        if (JDK_BYTE_BUFF.equals(byteBufferClassName)) {
            content.append(byteSizeStr);
        }
        content.append(hashCodeStr).append(toStringStr).append(methodStr).append("}");

        return content;
    }

    private void appendParam(ITypeBean typeBean, int index, StringBuilder paramStr) {
        StringBuilder detailedParam = new StringBuilder(typeBean.getValueClassName());
        String genericName = typeBean.getGenericClassName();
        if (genericName != null) {
            detailedParam.append("<").append(genericName).append(">");
        }
        this.paramTypeStr = detailedParam.toString();

        paramName = typeBean.getName();
        if (!isValidName(paramName)) {
            paramName = "param" + index;
        }
        detailedParam.append(" ").append(paramName);

        if (typeBean.getDescription() != null) {
            paramStr.append(CB).append(typeBean.getDescription()).append(CE);
        }
        paramStr.append("    protected ").append(detailedParam).append(";").append(PG);

        this.detailedParamStr = detailedParam.toString();
    }

    private void appendMethod(ITypeBean typeBean, StringBuilder methodStr) {
        String capitalize = StringUtils.capitalize(paramName);
        // getter
        methodStr.append("    public ").append(paramTypeStr).append(" get").append(capitalize).append("() {").append(LS)
                .append(RT).append("this.").append(paramName).append(";").append(END);

        // setter
        methodStr.append("    public void set").append(capitalize).append("(").append(detailedParamStr).append(") {");
        methodStr.append(LS).append(TH).append(paramName).append(" = ").append(paramName).append(";").append(END);

        // read() write()
        if (BeanTypeEnum.LIST.equals(typeBean.getType())) {
            readMethodStr.append(TH).append(paramName).append(" = readList(b, ").append(typeBean.getGenericClassName())
                    .append(".class);").append(LS);
            writeMethodStr.append(I2).append("writeList(b, ").append(paramName).append(");").append(LS);
        } else {
            readMethodStr.append(TH).append(paramName).append(" = (").append(paramTypeStr).append(") readByType(b, ")
                    .append(paramTypeStr).append(".class);").append(LS);
            writeMethodStr.append(I2).append("writeObj(b, ").append(paramName).append(");").append(LS);
        }
    }
}

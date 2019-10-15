package cn.laoshini.dk.jit;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.domain.ExecutorBean;
import cn.laoshini.dk.domain.dto.HandlerExpDescriptorDTO;
import cn.laoshini.dk.exception.JitException;
import cn.laoshini.dk.function.VariousWaysManager;
import cn.laoshini.dk.jit.compiler.DynamicCompiler;
import cn.laoshini.dk.jit.generator.CustomDtoClassFileGenerator;
import cn.laoshini.dk.jit.generator.HandlerClassFileGenerator;
import cn.laoshini.dk.jit.generator.IClassFileGenerator;
import cn.laoshini.dk.jit.generator.NettyCustomDtoClassFileGenerator;
import cn.laoshini.dk.jit.type.CompositeBean;
import cn.laoshini.dk.jit.type.HandlerBean;
import cn.laoshini.dk.manager.TypeUseManager;
import cn.laoshini.dk.net.IMessageHandlerManager;
import cn.laoshini.dk.net.handler.IMessageHandler;
import cn.laoshini.dk.net.msg.ICustomDto;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.FileUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * 动态类文件生成
 *
 * @author fagarine
 */
public class DynamicGenerator {

    public static final JitClassLoader JIT_CLASS_LOADER = new JitClassLoader(new URL[0],
            DynamicGenerator.class.getClassLoader());

    /**
     * 加载本地已生成的消息类和消息处理handler类
     */
    public static void loadAndRegisterGeneratedClass() {
        File root = new File(FileUtil.getProjectPath(IClassFileGenerator.GENERATED_PATH));
        File[] files;
        if (root.exists() && root.isDirectory() && (files = root.listFiles()) != null) {
            IMessageHandlerManager messageHandlerManager = VariousWaysManager
                    .getCurrentImpl(IMessageHandlerManager.class);
            for (File file : files) {
                if (file.getName().endsWith(Constants.CLASS_FILE_SUFFIX)) {
                    String fileName = file.getName();
                    String className =
                            IClassFileGenerator.GENERATED_PACKAGE + "." + fileName.substring(0, fileName.length() - 5);
                    Class<?> clazz = JIT_CLASS_LOADER.loadClass(file.getAbsolutePath(), className);
                    if (ICustomDto.class.isAssignableFrom(clazz)) {
                        TypeUseManager.registerCustomDtoClass((Class<? extends ICustomDto>) clazz);
                    } else if (messageHandlerManager != null && IMessageHandler.class.isAssignableFrom(clazz)) {
                        MessageHandle messageHandle = clazz.getAnnotation(MessageHandle.class);
                        ExecutorBean<MessageHandle> executorBean = new ExecutorBean<>(messageHandle, null, clazz);
                        messageHandlerManager.registerHandler(messageHandle.id(), executorBean);
                    }
                }
            }
        }
    }

    private static <T> Class<T> compileAndLoadClassBySrcFile(File javaFile, String className) {
        // 编译源文件
        DynamicCompiler.compile(javaFile);

        // 加载新编译的类
        String javaFilePath = javaFile.getPath();
        String classFilePath = javaFilePath.substring(0, javaFilePath.length() - 4) + "class";

        // 删除源文件
        if (!javaFile.delete()) {
            LogUtil.error("源文件[{}]编译后，删除失败", javaFilePath);
        }
        return JIT_CLASS_LOADER.loadClass(classFilePath, className);
    }

    public static Class<? extends IMessageHandler> generateAndCompileHandler(HandlerExpDescriptorDTO descriptor) {
        HandlerBean handlerBean = new HandlerBean();
        handlerBean.setMessageId(descriptor.getMessageId());
        handlerBean.setName("Message" + descriptor.getMessageId() + "Handler");
        handlerBean.setDescription(descriptor.getDescription());
        handlerBean.setDataType(descriptor.getDataType());
        handlerBean.setAllowGuestRequest(descriptor.getGuest());
        handlerBean.setSequential(descriptor.getSequential());
        handlerBean.setProtocol(descriptor.getProtocol());

        IClassFileGenerator generator = new HandlerClassFileGenerator(handlerBean, JIT_CLASS_LOADER, descriptor);
        File javaFile = generator.toJavaFile();
        if (javaFile == null || !javaFile.exists() || javaFile.isDirectory()) {
            throw new JitException("file.generator.fail", "Handler源文件生成失败:" + generator);
        }

        return compileAndLoadClassBySrcFile(javaFile, generator.getFullClassName());
    }

    public static List<Class<? extends IMessageHandler>> generateAndCompileHandlers(
            List<HandlerExpDescriptorDTO> descriptorVOS) {
        if (CollectionUtil.isEmpty(descriptorVOS)) {
            return Collections.emptyList();
        }

        List<Class<? extends IMessageHandler>> classes = new ArrayList<>(descriptorVOS.size());
        for (HandlerExpDescriptorDTO descriptorVO : descriptorVOS) {
            classes.add(generateAndCompileHandler(descriptorVO));
        }
        return classes;
    }

    public static Class<? extends ICustomDto> generateAndCompileCustomDto(CompositeBean compositeBean, boolean netty) {
        IClassFileGenerator generator;
        if (netty) {
            generator = new NettyCustomDtoClassFileGenerator(compositeBean, JIT_CLASS_LOADER);
        } else {
            generator = new CustomDtoClassFileGenerator(compositeBean, JIT_CLASS_LOADER);
        }

        File javaFile = generator.toJavaFile();
        if (javaFile == null || !javaFile.exists() || javaFile.isDirectory()) {
            throw new JitException("file.generator.fail", "自定义消息DTO源文件生成失败:" + compositeBean);
        }

        return compileAndLoadClassBySrcFile(javaFile, generator.getFullClassName());
    }

}

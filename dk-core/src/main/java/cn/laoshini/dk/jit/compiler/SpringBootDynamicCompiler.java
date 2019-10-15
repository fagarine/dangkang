package cn.laoshini.dk.jit.compiler;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.laoshini.dk.exception.JitException;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ZipUtil;

/**
 * SpringBoot项目，对于以jar包形式部署的项目，提供动态编译功能支持
 *
 * @author fagarine
 */
public class SpringBootDynamicCompiler {
    private SpringBootDynamicCompiler() {
    }

    private static final String SPRING_BOOT_LOADER = "org.springframework.boot.loader";

    /**
     * spring boot项目打包jar包中，依赖包的包内路径
     */
    private static final String SPRING_BOOT_LIB = "BOOT-INF/lib";

    /**
     * 依赖包解压出来后的存放路径（相对项目当前目录）
     */
    private static final String UNCOMPRESS_LIB = "sblib";

    private static final CompilerHolder HOLDER = new CompilerHolder();

    private static class CompilerHolder {
        private List<String> compileOptions = new ArrayList<>();

        private boolean isSpringBootStart;

        private AtomicBoolean initialized = new AtomicBoolean();

        private boolean available = true;

        private CompilerHolder() {
        }
    }

    private static synchronized void checkAndInit() {
        if (HOLDER.initialized.get()) {
            return;
        }

        HOLDER.initialized.set(true);

        LogUtil.info("SpringBootDynamicCompiler 开始初始化");
        String property = System.getProperty("java.protocol.handler.pkgs");
        HOLDER.isSpringBootStart = property != null && property.contains(SPRING_BOOT_LOADER);
        if (HOLDER.isSpringBootStart) {
            String separator = File.separator;
            String home = System.getProperty("user.dir");

            String path = SpringBootDynamicCompiler.class.getClassLoader()
                    .getResource("cn/laoshini/dk/constant/Constants.class").getPath();
            String startJarPath = path.substring(5, path.indexOf("!"));
            String libPath = home + separator + UNCOMPRESS_LIB + separator;
            File libDir = new File(libPath);
            if (!libDir.exists()) {
                if (!libDir.mkdir()) {
                    LogUtil.error("目录[{}]创建失败，无法为Spring Boot 项目提供动态编译支持", libPath);
                    HOLDER.available = false;
                }
            }

            // 解压启动jar包文件，释放出lib目录下的依赖包
            if (HOLDER.available) {
                LogUtil.info("SpringBootDynamicCompiler 开始解压lib包, jar:{}, lib:{}", startJarPath, libPath);
                try {
                    ZipUtil.uncompressFile(startJarPath, SPRING_BOOT_LIB, libPath);
                } catch (Exception e) {
                    LogUtil.error("解压项目启动jar文件时出错，无法为Spring Boot 项目提供动态编译支持", e);
                    HOLDER.available = false;
                }
                LogUtil.info("SpringBootDynamicCompiler 解压lib包结束");
            }

            // 构造classpath等编译项
            if (HOLDER.available) {
                StringBuilder cp = new StringBuilder();
                FileFilter filter = f -> {
                    if (f.isFile() && ZipUtil.isZipOrJarFile(f)) {
                        if (cp.length() > 0) {
                            cp.append(";");
                        }
                        cp.append(f.getAbsolutePath());
                    }
                    return false;
                };
                libDir.listFiles(filter);

                HOLDER.compileOptions.add("-classpath");
                HOLDER.compileOptions.add(cp.toString());
                HOLDER.compileOptions.add("-d");
                HOLDER.compileOptions.add(home);
            }
        } else {
            LogUtil.error("不是Spring Boot方式启动，SpringBootDynamicCompiler类初始化失败");
        }
        LogUtil.info("SpringBootDynamicCompiler 初始化完成");

    }

    public static boolean isSpringBootStart() {
        checkAndInit();

        return HOLDER.isSpringBootStart;
    }

    private static void checkEnvironment() {
        if (!isSpringBootStart()) {
            throw new JitException("method.call.error", "不是Spring Boot项目，不应该调用该方法");
        }

        if (!HOLDER.available) {
            throw new JitException("compiler.init.fail", "SpringBootDynamicCompiler类初始化失败，不能使用");
        }
    }

    public static void compileFile(File file) {
        checkEnvironment();

        DynamicCompiler.compile(HOLDER.compileOptions, file);
    }

    public static void compileFiles(List<File> srcFiles) {
        checkEnvironment();

        DynamicCompiler.compileSrcFiles(srcFiles, HOLDER.compileOptions);
    }
}

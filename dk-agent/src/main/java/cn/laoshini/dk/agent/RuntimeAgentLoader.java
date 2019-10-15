package cn.laoshini.dk.agent;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.spi.AttachProvider;

/**
 * 运行时动态加载java agent工具类
 *
 * @author fagarine
 */
class RuntimeAgentLoader {
    private RuntimeAgentLoader() {
    }

    private static final String DK_AGENT_NAME = "dk-agent";

    private static final String AGENT_LIB_DIR = "agentLib";

    public static boolean loadAgent() {
        return loadAgent(getAgentJarPath());
    }

    public static boolean loadAgent(String agentJarPath) {
        if (agentJarPath == null) {
            throw new RuntimeException("java agent jar包路径不能为空");
        }

        VirtualMachine vm = getVirtualMachine();
        try {
            vm.loadAgent(agentJarPath);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(
                    "动态加载java agent失败（请使用-javaagent:agentJarPath 通过命令行添加），agentJarPath:" + agentJarPath, e);
        } finally {
            try {
                vm.detach();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getAgentJarPath() {
        String classpath = System.getProperty("java.class.path");
        String pathSeparator = System.getProperty("path.separator");
        if (classpath.toLowerCase().contains(DK_AGENT_NAME)) {
            // dk-agent项目作为依赖包启动，返回dk-agent包文件路径
            for (String jarPath : classpath.split(pathSeparator)) {
                if (jarPath.contains(DK_AGENT_NAME)) {
                    File file = new File(jarPath);
                    if (!file.exists() || file.isDirectory()) {
                        System.err.println("dk-agent项目不能通过运行时加载到java agent:" + jarPath);
                    }
                    return jarPath;
                }
            }
        }

        // dk-agent项目被包含在项目启动包中
        String jarPath = RuntimeAgentLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            jarPath = URLDecoder.decode(jarPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(5);
        }

        int exclamationIndex;
        if ((exclamationIndex = jarPath.indexOf('!')) > -1) {
            jarPath = jarPath.substring(0, exclamationIndex);
        }

        try (JarFile jarFile = new JarFile(jarPath)){
            // 检查manifest文件，如果包含Agent-Class项，直接返回启动包路径
            Attributes attributes = jarFile.getManifest().getMainAttributes();
            if (DangKangAgent.class.getName().equals(attributes.getValue("Agent-Class"))) {
                return new File(jarPath).getAbsolutePath();
            }
        } catch (IOException e) {
            // ignore
        }

        // 获取Agent-Class失败，在agent依赖包路径查找是否存在
        String home = System.getProperty("user.dir");
        File agentLib = new File(home + File.separator + AGENT_LIB_DIR);
        if (agentLib.exists() && agentLib.isDirectory()) {
            File[] files = agentLib.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getName().contains(DK_AGENT_NAME)) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }

        throw new RuntimeException("找不到dk-agent项目jar包，无法向虚拟机动态加载agent");
    }

    public static VirtualMachine getVirtualMachine() {
        String pid = getCurrentPid();

        if (!VirtualMachine.list().isEmpty()) {
            try {
                return VirtualMachine.attach(pid);
            } catch (Exception e) {
                throw new RuntimeException("连接当前虚拟机失败, pid:" + pid, e);
            }
        }

        Class<? extends VirtualMachine> virtualMachineClass = findAndLoadVmImplementation();
        try {
            final AttachProviderPlaceHolder attachProvider = new AttachProviderPlaceHolder();
            Constructor<? extends VirtualMachine> vmConstructor = virtualMachineClass
                    .getDeclaredConstructor(AttachProvider.class, String.class);
            vmConstructor.setAccessible(true);
            return vmConstructor.newInstance(attachProvider, pid);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * 获取当前虚拟机的pid
     *
     * @return 字符串格式的pid
     */
    public static String getCurrentPid() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        return nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
    }

    /**
     * 查找并加载虚拟机实现类
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends VirtualMachine> findAndLoadVmImplementation() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("nix") || os.contains("nux")) {
                return (Class<? extends VirtualMachine>) Class.forName("sun.tools.attach.LinuxVirtualMachine");
            }
            if (os.contains("win")) {
                return (Class<? extends VirtualMachine>) Class.forName("sun.tools.attach.WindowsVirtualMachine");
            }
            if (os.contains("aix")) {
                return (Class<? extends VirtualMachine>) Class.forName("sun.tools.attach.AixVirtualMachine");
            }
            if (os.contains("freebsd") || os.contains("mac")) {
                return (Class<? extends VirtualMachine>) Class.forName("sun.tools.attach.BsdVirtualMachine");
            }
            if (os.contains("sunos") || os.contains("solaris")) {
                return (Class<? extends VirtualMachine>) Class.forName("sun.tools.attach.SolarisVirtualMachine");
            }
        } catch (Exception ex) {
            throw new RuntimeException("无法加载虚拟机实现类，请使用命令行形式（-javaagent:dk-agent.jar）加载java agent", ex);
        }
        throw new RuntimeException("找不到当前操作系统的虚拟机实现类: " + System.getProperty("os.name"));
    }

}

package cn.laoshini.dk.agent;

import java.io.IOException;
import java.util.List;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;

/**
 * @author fagarine
 */
class AttachProviderPlaceHolder extends AttachProvider {
    @Override
    public String name() {
        return null;
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public VirtualMachine attachVirtualMachine(String s) throws AttachNotSupportedException, IOException {
        return null;
    }

    @Override
    public List<VirtualMachineDescriptor> listVirtualMachines() {
        return null;
    }
}

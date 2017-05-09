package model.descriptors;


import model.packet.Payload;
import model.pes.PES;

import static model.config.MPEG.nil;

public class ServiceDescriptor extends Descriptor {

    private short serviceType;
    private short serviceNameProviderLength;
    private short serviceNameLength;
    private String serviceNameProvider;
    private String serviceName;


    public ServiceDescriptor(short descriptorTag, short descriptorTagLength, short serviceType, short serviceNameProviderLength,  String serviceNameProvider, short serviceNameLength, String serviceName) {
        super(descriptorTag, descriptorTagLength);
        this.serviceType = serviceType;
        this.serviceNameProviderLength = serviceNameProviderLength;
        this.serviceNameProvider = serviceNameProvider;
        this.serviceNameLength = serviceNameLength;
        this.serviceName = serviceName;
    }


    public short getServiceType() {
        return serviceType;
    }

    public short getServiceNameProviderLength() {
        return serviceNameProviderLength;
    }

    public short getServiceNameLength() {
        return serviceNameLength;
    }

    public String getServiceNameProvider() {
        return serviceNameProvider;
    }

    public String getServiceName() {
        return serviceName;
    }
}
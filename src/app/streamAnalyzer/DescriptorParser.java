package app.streamAnalyzer;


import model.descriptors.Descriptor;
import model.descriptors.ServiceDescriptor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static model.config.MPEG.descriptorLengthLength;
import static model.config.MPEG.descriptorTagLength;

public class DescriptorParser extends Parser {


    public List loadDescriptors(int serviceID, byte[] binaryFields, int size, int position) {

        List<Descriptor> descriptors = new ArrayList<>();

        for(size += position; position < size;) {
            short descriptorTag = (short) binToInt(binaryFields, position, position += descriptorTagLength );
            short descriptorLength = (short) binToInt(binaryFields, position, position += descriptorLengthLength);

            switch ( descriptorTag ) {
                case service_descriptor:
                    descriptors.add(analyzeServiceDescriptor(descriptorLength, serviceID, binaryFields, position));
                    break;
                case short_event_descriptor:
                    break;
                case extended_event_descriptor:
                    break;
                case network_name_descriptor:
                    break;
                default:
                    break;
                //TODO all descriptors
            }
            position += descriptorLength * byteBinaryLength;
        }
        return descriptors;
    }

    private Descriptor analyzeServiceDescriptor(short descriptorLength, int serviceID, byte[] binaryFields, int position) {

        short serviceType = (short) binToInt(binaryFields, position, position += serviceTypeLength);

        short serviceProviderNameLength = (short) binToInt(binaryFields, position, position += serviceNameLengthLength);
        String serviceProviderName = parseNchars(binaryFields, position, serviceProviderNameLength * charSize);

        short serviceNameLength = (short) binToInt(binaryFields, position += serviceProviderNameLength * charSize, position += serviceNameLengthLength);
        String serviceName = parseNchars(binaryFields, position, serviceNameLength * charSize);

        tables.updateProgramMap(serviceID,serviceName);
        tables.updateProviderMap(serviceID,serviceProviderName);
        return new ServiceDescriptor((short) service_descriptor, descriptorLength, serviceType, serviceProviderNameLength, serviceProviderName, serviceNameLength, serviceName);
    }
}

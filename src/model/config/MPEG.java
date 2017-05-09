package model.config;

import model.Stream;

import java.util.Map;


public class MPEG {

    public final static int nil = -1 ;

    public static final int intBinaryLength = 32;
    public static final int byteBinaryLength = 8;
    public static final int charSize = 8;

    public final static int tsPacketSize = 188;
    public final static int tsHeaderSize = 4;
    public final static int tsHeaderBinaryLength = tsHeaderSize * byteBinaryLength;
    public final static int tsAdaptationFieldHeaderSize = 2;
    public final static int tsAdaptationFieldHeaderBinaryLength = tsAdaptationFieldHeaderSize * byteBinaryLength;
    public final static int tsAdaptationFieldHeaderPosition = tsHeaderSize + tsAdaptationFieldHeaderSize;

    public final static int syncLost = 3;
    public final static int tsPayloadLength = tsPacketSize - tsHeaderSize;
    public final static int syncByte = 0x47;
    public final static int syncByteSize = 1;
    public final static int syncByteBinarySize = syncByteSize * byteBinaryLength;
    public final static int PIDfieldLength = 13;
    public final static int TSCfieldLength = 2;
    public final static int adaptationFieldControlLength = 2;
    public final static int continuityCounterLength = 4;

    public final static int adaptationFieldOnly = 2;
    public final static int adaptationFieldAndPayload = 3;

    public final static int fieldPresent = 0x01;
    public final static int fieldNotPresent = 0x00;

    public final static int PSImaxPID = 0x001F;

    public final static int PSIcommonFieldsBinaryLength = 24;
    public final static int PSIcommonFieldsLength = PSIcommonFieldsBinaryLength / byteBinaryLength;
    public final static int tableIDlength = 8;
    public final static int programNumberLength = 16;
    public final static int sectionLengthLength = 12;
    public final static int versionNumLength = 5;
    public final static int sectionNumLength = 8;
    public final static int transportStreamIDlength = 16;
    public final static int CRClength = 32;
    public final static int CRClengthByte = CRClength/byteBinaryLength;
    public final static int AFLlength = 1;

    public final static int mandatoryPATfields = 72;
    public final static int mandatoryPMTfields = 40;
    public final static int PATloopLength = 32;

    public final static int PCRsampleRate = 90;
    public final static int PCRextLength = 9;
    public final static int PCRstartBit = 15;
    public final static int PCRendBit = 48;

    public final static int PCRLegth = 48;
    public final static int PCR_PIDlength = 13;
    public final static int programInfoLengthLength = 12;
    public final static int streamTypeLength = 8;
    public final static int elementaryPIDlength = 13;
    public final static int ESinfoLengthLength = 12;

    public final static int serviceIDlength = 16;
    public final static int eventIDlength = 16;
    public final static int networkIDlength = 16;
    public final static int startTimeLength = 40;
    public final static int durationLength = 24;
    public final static int runningStatusLength = 3;
    public final static int descriptorsLengthLength = 12;

    public final static int serviceTypeLength = 8;
    public final static int serviceProviderLengthLength = 8;
    public final static int serviceNameLengthLength = 8;
    public final static int ISOlanguageCodeLength = 24;
    public final static int eventNameLengthLength = 8;
    public final static int textLengthLength = 8;

    public final static int packetStartCodePrefix = 0x000001;
    public final static int packetStartCodePrefixLength = 24;
    public final static int streamIDlength = 8;
    public final static int PESpacketLengthLength = 16;
    public final static int PESscramblingControlLength = 2;
    public final static int PESpriorityLength = 1;
    public final static int DataAlignmentIndicatorLength = 1;
    public final static int copyrightLength = 1;
    public final static int OriginalOrCopyLength = 1;
    public final static int PTSdtsFlagsLength = 2;
    public final static int PESCRflagLength = 1;
    public final static int ESrateFlagLength = 1;
    public final static int DSMtrickModeFlagLength = 1;
    public final static int AdditionalCopyInfoFlagLength = 1;
    public final static int PEScrcFlagLength = 1;
    public final static int PESextensionFlagLength = 1;
    public final static int PESheaderDataLengthLength = 8;
    public final static int PTSdtsLength = 40;
    public final static int ESCRlength = 42;
    public final static int ESrateLength = 22;
    public final static int DSMtrickModeLength = 8;
    public final static int AdditionalCopyInfoLength = 7;
    public final static int PEScrcLength = 16;
    public final static int PESextensionLength = 42;

    public final static int descriptorTagLength = 8;
    public final static int descriptorLengthLength = 8;

    public static final int PATpid = 0x00;
    public static final int CATpid = 0x01;
    public static final int TDSTpid = 0x02;
    public static final int NIT_STpid = 0x10;
    public static final int SDT_BAT_STpid = 0x11;
    public static final int EIT_STpid = 0x12;
    public static final int RST_STpid = 0x13;
    public static final int TDT_TOT_STpid = 0x14;
    public static final int netSyncPid = 0x15;
    public static final int RNTpid = 0x16;
    public static final int bandSignallingPID = 0x1C;
    public static final int measurementPID = 0x1D;
    public static final int DITpid = 0x1E;
    public static final int SITpid = 0x1F;
    public static final int PMTpid = nil;
    public static final int nullPacketPID = 0x1FFF;

    public static final int PAStableID = 0x00;
    public static final int CAStableID = 0x01;
    public static final int PMStableID = 0x02;
    public static final int TSDStableID = 0x3;
    public static final int NISactualTableID = 0x40;
    public static final int NISotherTableID = 0x41;
    public static final int SDSactualTableID = 0x42;
    public static final int SDSotherTableID = 0x46;
    public static final int BAStableID = 0x4A;
    public static final int EISactualPresentTableID = 0x4E;
    public static final int EISotherPresentTableID = 0x4F;
    public static final int TDStableID = 0x70;
    public static final int RSStableID = 0x72;
    public static final int TOStableID = 0x73;
    public static final int DIStableID = 0x7E;
    public static final int SIStableID = 0x7F;

    public static final int PSItype = 0xF0;
    public static final int videoType = 0xF1;
    public static final int audioType = 0xF2;
    public static final int CAStype = 0xF3;
    public static final int PSMtype = 0xF4;
    public static final int MHEGtype = 0xF5;
    public static final int privateType = 0xF6;
    public static final int defaultType = 0xF7;
    public static final int adaptationFieldIcon = 0xF8;
    public static final int PESheaderIcon = 0xF9;
    public static final int PMTicon = 0xFA;
    public static final int DVBicon = 0xFB;
    public static final int payloadStartIcon = 0xFC;
    public static final int timestampIcon = 0xFD;


    //MPEG ES descriptors
    public static final int	video_stream_descriptor	= 0x02;
    public static final int	audio_stream_descriptor	= 0x03;
    public static final int	hierarchy_descriptor = 0x04;
    public static final int registration_descriptor = 0x05;
    public static final int	data_stream_descriptor = 0x06;
    public static final int	target_background_grid_descriptor =	0x07;
    public static final int	video_window_descriptor	= 0x08;
    public static final int	CA_descriptor =	0x09;
    public static final int	ISO_639_language_descriptor	= 0x0A;
    public static final int	system_clock_descriptor	= 0x0B;
    public static final int	multiplex_buffer_utilization_descriptor	= 0x0C;
    public static final int	copyright_descriptor =	0x0D;
    public static final int	maximum_bitrate_descriptor = 0x0E;
    public static final int	private_data_indicator_descriptor =	0x0F;
    public static final int	smoothing_buffer_descriptor	= 0x10;
    public static final int	STD_descriptor = 0x11;
    public static final int	BP_descriptor =	0x12;

    //MPEG SI descriptors
    public static final int network_name_descriptor = 0x40;	//	NIT
    public static final int service_list_descriptor = 0x41;	//	NIT, BAT
    public static final int stuffing_descriptor = 0x42;	//	NIT, BAT, SDT, EIT, SIT
    public static final int satellite_delvery_system_descriptor = 0x43	;	//	NIT
    public static final int cable_delivery_system_descriptor = 0x44	;	//	NIT
    public static final int VBI_data_descriptor = 0x45;	//	PMT
    public static final int VBI_teletext_descriptor = 0x46;	//	PMT
    public static final int bouquet_name_descriptor = 0x47;	//	BAT, SDT, SIT
    public static final int service_descriptor = 0x48;	//	SDT, SIT
    public static final int country_availability_descriptor = 0x49	;	//	BAT, SDT< SIT
    public static final int linkage_descriptor	= 0x4A;	//	NIT, BAT, SDT, EIT, SIT
    public static final int NVOD_reference_descriptor = 0x4B;	//	SDT, SIT
    public static final int time_shifted_service_descriptor = 0x4C;	//	SDT, SIT
    public static final int short_event_descriptor	= 0x4D;	//	EIT, SIT
    public static final int extended_event_descriptor = 0x4E;	//	EIT, SIT
    public static final int time_shifted_event_descriptor = 0x4F;	//	EIT, SIT
    public static final int component_descriptor = 0x50;	//	EIT, SIT
    public static final int mosaic_descriptor = 0x51;	//	SDT, PMT
    public static final int stream_identifier_descriptor = 0x52;	//	PMT
    public static final int CA_identifier_descriptor = 0x53;	//	BAT, SDT, EIT, SIT
    public static final int content_descriptor	= 0x54;	//	EIT, SIT
    public static final int parental_rating_descriptor	= 0x55;	//	EIT, SIT
    public static final int teletext_descriptor = 0x56	;	//	PMT
    public static final int telephone_descriptor = 0x57;	//	PMT
    public static final int local_time_offset_descriptor = 0x58;	//	TOT
    public static final int subtitling_descriptor = 0x59;	//	NIT
    public static final int terrestrial_delivery_system_descriptor	= 0x5A;	//	NIT
    public static final int multilingual_network_name_descriptor = 0x5B;	//	NIT
    public static final int multilingual_bouquet_name_descriptor =	0x5C;	//	BAT
    public static final int multilingual_service_name_descriptor =	0x5D;	//	SDT, SIT
    public static final int multilingual_component_descriptor = 0x5E;	//	EIT, SIT
    public static final int private_data_specifier_descriptor = 0x5F;	//	NIT, BAT, SDT, EIT, PMT, SIT
    public static final int service_mode_descriptor = 0x60;	//	PMT
    public static final int short_smoothing_buffer_descriptor = 0x61;	//	EIT, SIT
    public static final int frequency_list_descriptor = 0x62;	//	NIT
    public static final int partial_tranport_stream_descriptor	= 0x63;	//	SIT
    public static final int data_broadcast_descriptor = 0x64;	//	PMT
    public static final int CA_system_descriptor = 0x65;   //	PMT, EIT
    public static final int data_broadcast_id_descriptor = 0x66;	//	PMT
    public static final int transport_stream_descriptor = 0x67;	//	 
    public static final int DSNG_descriptor = 0x68;	//	 
    public static final int PDC_descriptor	= 0x69;	//	EIT
    public static final int AC3_descriptor = 0x6A;	//	PMT
    public static final int ancilliary_data_descriptor	= 0x6B;	//	PMT
    public static final int cell_list_descriptor = 0x6C;	//	NIT
    public static final int cell_frequency_link_descriptor	= 0x6D;	//	NIT
    public static final int announcement_support_descriptor = 0x6E;	//	NIT
    public static final int user_defined_descriptor = 0x80-0xFE;	//TODO this is range, not subtraction

    public enum TimestampType {
        PCR, OPCR, PTS, DTS
    }

    public static String getElementaryStreamDescriptor(Integer descriptor) {
        if (descriptor == null){
            return "Unidentified ES descriptor";
        }
        else if ( descriptor>=0x1C && descriptor<=0x7F) {
            return "ITU-T Rec. H.222 | ISO/IEC 13818-1 Reserved";
        }
        else if ( descriptor>=0x80 && descriptor<=0xFF) {
            return "User defined";
        }
        else {
            switch (descriptor) {
                case 0x00:
                    return "Reserved";
                case 0x01:
                    return "ISO/IEC 11172-2 (MPEG-1 video)";
                case 0x02:
                    return "ITU-T Rec. H.262 and ISO/IEC 13818-2 (MPEG-2 higher rate interlaced video)";
                case 0x03:
                    return "ISO/IEC 11172-3 (MPEG-1 audio)";
                case 0x04:
                    return "ISO/IEC 13818-3 (MPEG-2 halved sample rate audio)";
                case 0x05:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1 (MPEG-2 tabled data) privately defined	";
                case 0x06:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1 (MPEG-2 packetized data) privately defined (i.e., MPEG subtitles/VBI and AC-3)";
                case 0x07:
                    return "ISO/IEC 13522 (MHEG)";
                case 0x08:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1 DSM CC ";
                case 0x09:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1/11172-1 auxiliary data ";
                case 0x0A:
                    return "ISO/IEC 13818-6 DSM CC multiprotocol encapsulation";
                case 0x0B:
                    return "ISO/IEC 13818-6 DSM CC U-N messages";
                case 0x0C:
                    return "ISO/IEC 13818-6 DSM CC stream descriptors";
                case 0x0D:
                    return "ISO/IEC 13818-6 DSM CC tabled data";
                case 0x0E:
                    return "ISO/IEC 13818-1 auxiliary data ";
                case 0x0F:
                    return "ISO/IEC 13818-7 ADTS AAC (MPEG-2 lower bit-rate audio)";
                case 0x10:
                    return "ISO/IEC 14496-2 (MPEG-4 H.263 based video)";
                case 0x11:
                    return "ISO/IEC 14496-3 (MPEG-4 LOAS multi-format framed audio) ";
                case 0x12:
                    return "ISO/IEC 14496-1 (MPEG-4 FlexMux) ";
                case 0x13:
                    return "ISO/IEC 14496-1 (MPEG-4 FlexMux) in ISO/IEC 14496 tables";
                case 0x14:
                    return "ISO/IEC 13818-6 DSM CC synchronized download protocol";
                case 0x15:
                    return "Packetized metadata";
                case 0x16:
                    return "Sectioned metadata";
                case 0x17:
                    return "ISO/IEC 13818-6 DSM CC Data Carousel metadata";
                case 0x18:
                    return "ISO/IEC 13818-6 DSM CC Object Carousel metadata";
                case 0x19:
                    return "ISO/IEC 13818-6 Synchronized Download Protocol metadata";
                case 0x1A:
                    return "ISO/IEC 13818-11 IPMP";
                case 0x1B:
                    return "ITU-T Rec. H.264 and ISO/IEC 14496-10 (lower bit-rate video)";
                default:
                    return "Unidentified ES descriptor";
            }
        }
    }


    public static String getLoopDescriptor(int descriptor) {
        if (descriptor == 0x00 || descriptor == 0x01) {
            return "Reserved";
        } else if (descriptor >= 0x13 || descriptor <= 0x3F) {
            return "ITU-T Rec. H.262 | ISO/IEC 13818-1 Reserved";
        } else if (descriptor >= 0x40 || descriptor <= 0xFF) {
            return "User private";
        } else {
            switch (descriptor) {
                case video_stream_descriptor:
                    return "video_stream_descriptor";
                case audio_stream_descriptor:
                    return "audio_stream_descriptor";
                case hierarchy_descriptor:
                    return "hierarchy_descriptor";
                case registration_descriptor:
                    return "registration_descriptor";
                case data_stream_descriptor:
                    return "data_stream_descriptor";
                case target_background_grid_descriptor:
                    return "target_background_grid_descriptor";
                case video_window_descriptor:
                    return "video_window_descriptor";
                case CA_descriptor:
                    return "CA_descriptor";
                case ISO_639_language_descriptor:
                    return "ISO_639_language_descriptor";
                case system_clock_descriptor:
                    return "system_clock_descriptor";
                case multiplex_buffer_utilization_descriptor:
                    return "multiplex_buffer_utilization_descriptoR";
                case copyright_descriptor:
                    return "copyright_descriptor";
                case maximum_bitrate_descriptor:
                    return "maximum_bitrate_descriptor";
                case private_data_indicator_descriptor:
                    return "private_data_indicator_descriptor";
                case smoothing_buffer_descriptor:
                    return "smoothing_buffer_descriptor";
                case STD_descriptor:
                    return "STD_descriptor";
                case BP_descriptor:
                    return "BP_descriptor";
                default:
                    return "Unidentified descriptor";
            }
        }
    }


    public static int getPEStype(int streamID){

        if ( streamID >= 0x0C0 && streamID <= 0x0DF ) {
            return audioType;
        }
        if ( streamID >= 0x0E0 && streamID <= 0x0EF ) {
            return videoType;
        }
        if ( streamID == 0x1F0 || streamID == 0x1F1 ){
            return CAStype;
        }
        if ( streamID == 0x17C || streamID == 0x1FF ){
            return PSMtype;
        }
        if ( streamID == 0x0F3 ){
            return MHEGtype;
        }
        if ( streamID == 0x0BD || streamID == 0x0BF){
            return privateType;
        }
        if( streamID >= 0x0FD4 && streamID <= 0x0F8){
            return defaultType;
        }
        return defaultType;
    }


    public static  String getStreamDescription(int streamID) {

        if (streamID >= 0x0C0 && streamID <= 0x0DF) {
            return "ISO/IEC 13818-3 or ISO/IEC 11172-3 or \n" +
                    "ISO/IEC 13818-7 or ISO/IEC 14496-3 audio stream number: " + (streamID << 4);
        }
        if (streamID >= 0x0E0 && streamID <= 0x0FF) {
            return "ITU-T Rec. H.262 | ISO/IEC 13818-2 or  \n" +
                    "ISO/IEC 11172-2or ISO/IEC 14496-2 video stream number: " + (streamID << 4);
        }
        if (streamID >= 0x0FA && streamID <= 0x0FE) {
            return "Reserved data stream";
        } else {
            switch (streamID) {
                case 0x0BC:
                    return "Program stream map";
                case 0x0BD:
                    return "Private stream 1";
                case 0x0BE:
                    return "Padding stream";
                case 0x0BF:
                    return "Private stream 2";
                case 0x0F0:
                    return "ECM stream";
                case 0x0F1:
                    return "EMM stream";
                case 0x0F2:
                    return "ITU-T Rec. H.222.0 | ISO/IEC 13818-1 \n" + "Annex A or ISO/IEC 13818-6_DSMCC_stream";
                case 0x0F3:
                    return "ISO/IEC_13522_stream";
                case 0x0F4:
                    return "ITU-T Rec. H.222.1 type A";
                case 0x0F5:
                    return "ITU-T Rec. H.222.1 type B";
                case 0x0F6:
                    return "ITU-T Rec. H.222.1 type C";
                case 0x0F7:
                    return "ITU-T Rec. H.222.1 type D";
                case 0x0F8:
                    return "ITU-T Rec. H.222.1 type E";
                case 0x0F9:
                    return "Ancillary stream";
                case 0x0FF:
                    return "Program stream directory";
            }
        }
        return "Undefined stream";
    }


    public static String getPacketName(int PID) {
        switch(PID){
            case PATpid :
                return "PAT";
            case CATpid :
                return "CAT";
            case TDSTpid :
                return "TDST";
            case NIT_STpid :
                return "NIT or ST";
            case SDT_BAT_STpid :
                return "SDT, BAT or ST";
            case EIT_STpid :
                return "EIT or ST";
            case RST_STpid :
                return "RST or ST";
            case TDT_TOT_STpid :
                return "TDT, TOT or ST";
            case netSyncPid :
                return "NetSync";
            case DITpid :
                return "DIT";
            case SITpid :
                return "SIT";
            case PMTpid :
                return "PMT";
            case RNTpid :
                return "RNT";
            case bandSignallingPID :
                return "InbandSignal.";
            case measurementPID :
                return "Measur.";
            default:
                return "PES";
        }
    }


    public static String getTableName(int PID) {
        //TODO finish if else
        // 0x04 - 0x3f "reserved"
        // 0x50 - 0x5f "event info actual schedule"
        // 0x60 - 0x6f "event info other schedule"
        // 0x7b - 0x7d "reserved"
        // 0x80 - 0xfe "user defined"

        switch (PID) {
            case 0x00:
                return "program association";
            case 0x01:
                return "conditional access";
            case 0x02:
                return "program map";
            case 0x03:
                return "transport stream description";
            case 0x40:
                return "actual network info";
            case 0x41:
                return "other network info";
            case 0x42:
                return "actual service description";
            case 0x46:
                return "other service description";
            case 0x4a:
                return "bouquet association";
            case 0x4e:
                return "actual event info now";
            case 0x4f:
                return "other event info now";
            case 0x70:
                return "time data";
            case 0x71:
                return "running status";
            case 0x72:
                return "stuffing";
            case 0x73:
                return "time offset";
            case 0x74:
                return "application information";
            case 0x75:
                return "container";
            case 0x76:
                return "related content";
            case 0x77:
                return "content id";
            case 0x78:
                return "MPE-FEC";
            case 0x79:
                return "resolution notification";
            case 0x7a:
                return "MPE-IFEC";
            case 0x7e:
                return "discontinuity info";
            case 0x7f:
                return "selection info";
            case 0xff:
                return "reserved";
        }
        return "reserved";
    }


    public static  String getProgramName(Stream stream, int pid) {
        Map map = stream.getTables().getProgramNameMap();
        Object obj = map.get(pid);
        return obj == null ? String.valueOf(pid) : obj.toString();
    }


    public static  int getType(int PID, Stream stream) {
        if (isPSI(PID)) {
            return PSItype;
        }
        if (PID == nullPacketPID){
            return nullPacketPID;
        }
        return getPEStype(stream.getPEScode(PID));
    }


    protected boolean isSDT(short tableID) {
        return tableID == SDSactualTableID || tableID == SDSotherTableID;
    }


    protected boolean isEIT(short tableID) {
        if(tableID == EISactualPresentTableID || tableID == EISotherPresentTableID){
            return true;
        }
        else if(tableID >= 0x50 || tableID <= 0x5F){ //EISactualPresentTableIDschedule
            return true;
        }
        else if(tableID >= 0x60 || tableID <= 0x6F){ //EISotherPresentTableIDschedule
            return true;
        }
        return false;
    }


    public static boolean isPSI(int PID) {
        if(getPacketName(PID) == "PES"){
            if( PID < 0x17 || PID > 0x1B || PID != nullPacketPID){
                return false;
            }
        }
        return true;
    }
}
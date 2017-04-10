package model.config;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import model.Stream;
import model.TSpacket;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DVB {

    public final static int tsPacketSize = 188;
    public final static int tsHeaderSize = 4;
    public final static int tsHeaderBinaryLength = tsHeaderSize * 8;
    public final static int tsAdaptationFieldHeaderSize = 2;
    public final static int tsAdaptationFieldHeaderBinaryLength = tsAdaptationFieldHeaderSize * 8;

    public final static int tsPayloadLength = tsPacketSize - tsHeaderSize;
    public final static int syncByte = 0x47;
    public final static int syncByteSize = 1;

    public final static int adaptationFieldOnly = 2;
    public final static int adaptationFieldAndPayload = 3;

    public final static int PSImaxPID = 0x001F;

    public final static int nil = -1 ;
    public final static int PSIcommonFieldsLength = 24;
    public final static int tableIDlength = 8;
    public final static int programNumberLength = 16;
    public final static int sectionLengthLength = 12;
    public final static int versionNumLength = 5;
    public final static int sectionNumLength = 8;
    public final static int tsIDlength = 16;
    public final static int CRClength = 32;
    public final static int AFLlength = 1;

    public final static int mandatoryPATfields = 72;
    public final static int mandatoryPMTfields = 40;

    public final static int PCR_PIDlength = 13;
    public final static int programInfoLengthLength = 12;
    public final static int streamTypeLength = 8;
    public final static int elementaryPIDlength = 13;
    public final static int ESinfoLengthLength = 12;

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


    public static final int PATpid = 0x00;
    public static final int CATpid = 0x01;
    public static final int TDSTpid = 0x02;
    public static final int NIT_STpid = 0x10;
    public static final int SDT_BAT_STpid = 0x11;
    public static final int EIT_STpid = 0x12;
    public static final int RST_STpid = 0x13;
    public static final int TDT_TOT_STpid = 0x14;
    public static final int netSyncPid = 0x15;
    public static final int DITpid = 0x1E;
    public static final int SITpid = 0x1F;
    public static final int PMTpid = 0xFF;
    public static final int nullPacket = 0x1FFF;

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

    public static final int intBitLength = 32;
    public static final int byteBinaryLength = 8;

    public static final int PSItype = 0xF0;
    public static final int videoType = 0xF1;
    public static final int audioType = 0xF2;
    public static final int CAStype = 0xF3;
    public static final int PSMtype = 0xF4;
    public static final int MHEGtype = 0xF5;
    public static final int adaptationFieldIcon = 0xF6;
    public static final int PESheaderIcon = 0xF7;
    public static final int privateType = 0xF8;
    public static final int defaultType = 0xFF;

    public static final int	video_stream_descriptor	= 0x02;
    public static final int	audio_stream_descriptor	= 0x03;
    public static final int	hierarchy_descriptor = 0x04;
    public static final int	tegistration_descriptor = 0x05;
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


    public static String getElementaryStreamDescriptor(int descriptor) {
        if ( descriptor>=0x1C && descriptor<=0x7F) {
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
                    return "ISO/IEC 11172-2 (MPEG-1 video) in a packetized stream	";
                case 0x02:
                    return "ITU-T Rec. H.262 and ISO/IEC 13818-2 (MPEG-2 higher rate interlaced video) in a packetized stream	";
                case 0x03:
                    return "ISO/IEC 11172-3 (MPEG-1 audio) in a packetized stream	";
                case 0x04:
                    return "ISO/IEC 13818-3 (MPEG-2 halved sample rate audio) in a packetized stream	";
                case 0x05:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1 (MPEG-2 tabled data) privately defined	";
                case 0x06:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1 (MPEG-2 packetized data) privately defined (i.e., DVB subtitles/VBI and AC-3)	";
                case 0x07:
                    return "ISO/IEC 13522 (MHEG) in a packetized stream	";
                case 0x08:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1 DSM CC in a packetized stream	";
                case 0x09:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1/11172-1 auxiliary data in a packetized stream	";
                case 0x0A:
                    return "ISO/IEC 13818-6 DSM CC multiprotocol encapsulation	";
                case 0x0B:
                    return "ISO/IEC 13818-6 DSM CC U-N messages	";
                case 0x0C:
                    return "ISO/IEC 13818-6 DSM CC stream descriptors	";
                case 0x0D:
                    return "ISO/IEC 13818-6 DSM CC tabled data	";
                case 0x0E:
                    return "ISO/IEC 13818-1 auxiliary data in a packetized stream	";
                case 0x0F:
                    return "ISO/IEC 13818-7 ADTS AAC (MPEG-2 lower bit-rate audio) in a packetized stream	";
                case 0x10:
                    return "ISO/IEC 14496-2 (MPEG-4 H.263 based video) in a packetized stream	";
                case 0x11:
                    return "ISO/IEC 14496-3 (MPEG-4 LOAS multi-format framed audio) in a packetized stream	";
                case 0x12:
                    return "ISO/IEC 14496-1 (MPEG-4 FlexMux) in a packetized stream	";
                case 0x13:
                    return "ISO/IEC 14496-1 (MPEG-4 FlexMux) in ISO/IEC 14496 tables	";
                case 0x14:
                    return "ISO/IEC 13818-6 DSM CC synchronized download protocol	";
                case 0x15:
                    return "Packetized metadata	";
                case 0x16:
                    return "Sectioned metadata	";
                case 0x17:
                    return "ISO/IEC 13818-6 DSM CC Data Carousel metadata	";
                case 0x18:
                    return "ISO/IEC 13818-6 DSM CC Object Carousel metadata	";
                case 0x19:
                    return "ISO/IEC 13818-6 Synchronized Download Protocol metadata	";
                case 0x1A:
                    return "ISO/IEC 13818-11 IPMP	";
                case 0x1B:
                    return "ITU-T Rec. H.264 and ISO/IEC 14496-10 (lower bit-rate video) in a packetized stream";
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
                case tegistration_descriptor:
                    return "tegistration_descriptor";
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


    public static  String getPacketName(int pid) {
        switch(pid){
            case PATpid : return "PAT";
            case CATpid : return "CAT";
            case TDSTpid : return "TDST";
            case NIT_STpid : return "NIT";
            case SDT_BAT_STpid : return "SDT or BAT";
            case EIT_STpid : return "EIT";
            case RST_STpid : return "RST";
            case TDT_TOT_STpid : return "TDT";
            case netSyncPid : return "NetSync";
            case DITpid : return "DIT";
            case SITpid : return "SIT";
            case PMTpid : return "PMT";
            default: return "PES";
        }
    }


    public static  String getProgramName(Stream stream, int pid) {
        Map map = stream.getPrograms();
        Object obj = map.get(pid);
        return obj == null ? "" : obj.toString();
    }


    public static  int getType(TSpacket packet, Stream stream) {
        if (isPSI(packet.getPID()))
            return PSItype;
        return getPEStype(stream.getPEScode(packet.getPID()));
    }


    public static  boolean isPSI(int pid) {
        return getPacketName(pid) != "PES";
    }
}
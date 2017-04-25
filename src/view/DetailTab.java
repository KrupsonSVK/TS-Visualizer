package view;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import model.Stream;

import java.util.HashMap;
import java.util.Map;

import static model.config.Config.windowHeigth;
import static model.config.Config.windowWidth;
import static model.config.MPEG.*;


public class DetailTab extends Window {

    TreeItem<String> nodes;
    Tab tab;


    DetailTab(){
        this.nodes = nodes;
        tab = new Tab("Stream details");
    }


    void createTreeTab(Stream streamDescriptor) {

        ScrollPane scrollPane = new ScrollPane(new TreeView<>(createTree(streamDescriptor)));
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefSize(windowWidth,windowHeigth);

        tab.setContent(scrollPane);
    }


    private TreeItem<String> createTree(Stream streamDescriptor) {
        //TODO functions and number of occurence of table, and types and number of tableIDs
        TreeItem<String> rootNode = createRootNode(streamDescriptor);

        TreeItem<String> PSInode = new TreeItem<>("PSI");
        {
            PSInode.getChildren().addAll( new TreeItem<>("Program Specific Information Tables"));

            TreeItem PATnode = createPATnode((HashMap<Integer, Integer>) streamDescriptor.getTables().getPATmap());
            TreeItem<String> CATnode = createCATnode(streamDescriptor.getTables().getPIDmap());
            TreeItem PMTnode = createPMTnode(
                    (HashMap<Integer, String>) streamDescriptor.getTables().getProgramMap(),
                    (HashMap<Integer, Integer>)streamDescriptor.getTables().getPMTmap(),
                    (HashMap<Integer, Integer>) streamDescriptor.getTables().getESmap()
            );
            TreeItem<String> NITnode = createNITnode(streamDescriptor.getTables().getPIDmap());
            TreeItem<String> SDTnode = createSDTnode(streamDescriptor.getTables().getPIDmap());
            TreeItem<String> TOTnode = createTOTnode(streamDescriptor.getTables().getPIDmap());
            TreeItem<String> SITnode = createSITnode(streamDescriptor.getTables().getPIDmap());
            TreeItem<String> SynNode = createSynNode(streamDescriptor.getTables().getPIDmap());

            PSInode.getChildren().addAll(PATnode, CATnode, PMTnode, NITnode, SDTnode, TOTnode, SITnode, SynNode);
        }
        TreeItem PIDnode = createPIDnode(streamDescriptor.getTables().getPIDmap(), streamDescriptor.getTables().getESmap());

        rootNode.getChildren().addAll(PSInode, PIDnode);

        return this.nodes = rootNode;
    }


    private TreeItem<String> createSynNode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("NetSync");
        node.getChildren().add(new TreeItem<>("Network Synchronization Table"));
        node.getChildren().add(new TreeItem<>("Number of packets: " + (PIDmap.get(netSyncPid)==null?0:PIDmap.get(netSyncPid))  + "x "));
        //node.getChildren().add(new TreeItem<>("Tables"));
        return node;
    }


    private TreeItem<String> createSITnode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("SIT");
        node.getChildren().add(new TreeItem<>("Service Information Table"));
        node.getChildren().add(new TreeItem<>("Number of packets: " + (PIDmap.get(SITpid)==null?0:PIDmap.get(SITpid)) + "x "));
        return node;
    }


    private TreeItem<String> createTOTnode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("TDT, TOT or ST");
        node.getChildren().add( new TreeItem<>("Time Offset Table"));
        node.getChildren().add( new TreeItem<>("Time and Date Table"));
        node.getChildren().add( new TreeItem<>("Service Table"));
        node.getChildren().add(new TreeItem<>("Number of packets: " + (PIDmap.get(TDT_TOT_STpid)==null?0:PIDmap.get(TDT_TOT_STpid))  + "x "));
        return node;
    }


    private TreeItem<String> createSDTnode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("SDT, BAT or ST");
        node.getChildren().add( new TreeItem<>("Service Description Table"));
        node.getChildren().add( new TreeItem<>("Bouquet Association Table"));
        node.getChildren().add( new TreeItem<>("Service Table"));
        node.getChildren().add(new TreeItem<>("Number of packets: " + (PIDmap.get(SDT_BAT_STpid)==null?0:PIDmap.get(SDT_BAT_STpid)) + "x "));
        return node;
    }


    private TreeItem<String> createNITnode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("NIT or ST");
        node.getChildren().add(new TreeItem<>("Network Information Table"));
        node.getChildren().add(new TreeItem<>("Service Table"));
        node.getChildren().add(new TreeItem<>("Number of packets: " + (PIDmap.get(NIT_STpid)==null?0:PIDmap.get(NIT_STpid)) + "x "));
        return node;
    }


    private TreeItem<String> createCATnode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("CAT");
        node.getChildren().addAll( new TreeItem<>("Conditional Access Table"));
        node.getChildren().add(new TreeItem<>("Number of packets: " + (PIDmap.get(CATpid)==null?0:PIDmap.get(CATpid)) + "x "));
        return node;
    }


    private TreeItem<String> createRootNode(Stream streamDescriptor) {
        TreeItem<String> rootNode = new TreeItem<>(streamDescriptor.getName());
        rootNode.setExpanded(true);

        rootNode.getChildren().addAll(
                new TreeItem<>("Path: " + streamDescriptor.getPath()),
                new TreeItem<>("Size: " + streamDescriptor.getSize()),
                new TreeItem<>("Created: " + streamDescriptor.getCreationTime()),
                new TreeItem<>("Last access: " + streamDescriptor.getLastAccessTime()),
                new TreeItem<>("Last modified: " + streamDescriptor.getLastModifiedTime()),
                new TreeItem<>("Regular file: " + streamDescriptor.isRegularFile()),
                new TreeItem<>("Read-only: " + streamDescriptor.isReadonly()),
                new TreeItem<>("Owner: " + streamDescriptor.getOwner()),
                new TreeItem<>("TS Packets: " + streamDescriptor.getNumOfPackets() + "x"),
                new TreeItem<>("TS packet size: " + streamDescriptor.getPacketSize() + " B"),
                new TreeItem<>("Error packets: " + streamDescriptor.getNumOfErrors() + "x"),
                new TreeItem<>("Duration: " + parseTimestamp(streamDescriptor.getDuration())),
                new TreeItem<>("Bitrate: " + String.format("%.3f",(streamDescriptor.getBitrate())/1024f/1024f*byteBinaryLength) + " Mbit/s")
        );
        return rootNode;
    }


    private TreeItem createPMTnode(HashMap<Integer, String> programMap,  HashMap<Integer, Integer> PMTmap, HashMap<Integer, Integer> ESmap) {
        TreeItem<String> PMTnode = new TreeItem<>("PMT");
        PMTnode.getChildren().add(new TreeItem<>("Program Map Table"));

        for (Map.Entry<Integer, String> programEntry : programMap.entrySet()) {
            TreeItem<String> programNode = new TreeItem<>("Program: " + toHex(programEntry.getKey()) + " (" + programEntry.getKey() + ")");
            int index = 0;

            for (Map.Entry<Integer, Integer> PMTentry : PMTmap.entrySet()) {

                if (programEntry.getKey().equals(PMTentry.getValue())) {

                    for (Map.Entry<Integer, Integer> ESentry : ESmap.entrySet()) {

                        if (ESentry.getKey().equals(PMTentry.getKey())) {
                            TreeItem<String> node = new TreeItem<>("Component " + index++ + ": ");
                            node.getChildren().add(new TreeItem<>("PID: " + toHex(ESentry.getKey()) + " (" + ESentry.getKey() + ")"));
                            node.getChildren().add(new TreeItem<>("Stream type: " + getElementaryStreamDescriptor(ESentry.getValue())));
                            programNode.getChildren().add(node);
                        }
                    }
                }
            }
            PMTnode.getChildren().add(programNode);
        }
        return PMTnode;
    }


    private TreeItem createPATnode(HashMap<Integer, Integer> PATmap) {
        TreeItem<String> PATnode = new TreeItem<>("PAT");
        PATnode.getChildren().add(new TreeItem<>("Program Association Table"));

        for (Map.Entry<Integer, Integer> pid : PATmap.entrySet()) {
            TreeItem<String> serviceNode = new TreeItem<>("Service: " + toHex(pid.getKey()) + " (" + pid.getKey() + ")");

            serviceNode.getChildren().add(new TreeItem<>("Program number: " + toHex(pid.getKey()) + " (" + pid.getKey() + ")"));
            serviceNode.getChildren().add(new TreeItem<>( "Program PMT PID: " + toHex(pid.getValue()) + " (" + pid.getValue() + ")"));

            PATnode.getChildren().add(serviceNode);
        }
        return PATnode;
    }


    private <K, V> TreeItem<String> createPIDnode(Map<K, V> PIDmap, Map ESmap) {
        TreeItem<String> PIDnode = new TreeItem<>("PIDs");

        for (Map.Entry<K, V> pid : PIDmap.entrySet()) {
            TreeItem<String> node = new TreeItem<>(toHex((Integer) pid.getKey()) + " (" + pid.getKey() + ")");

            StringBuilder name = new StringBuilder(getPacketName((Integer) pid.getKey()));
            if (name.toString().equals("PES")) {
                name.append(" (" + getElementaryStreamDescriptor((Integer) ESmap.get(pid.getKey())) + ")");
            } else {
                name = new StringBuilder("PSI (" + getPacketName((Integer) pid.getKey()) + ")");
            }
            node.getChildren().add(new TreeItem<>(name.toString()));
            node.getChildren().add(new TreeItem<>("Number of packets: " + pid.getValue() + "x "));

            PIDnode.getChildren().add(node);
        }
        return PIDnode;
    }


    private String toHex(int pid) {
        return String.format("0x%04X", pid & 0xFFFFF);
    }


    public void setScene(Scene scene) {
//        this.scene = scene;
    }


    //        TreeItem<String> hundredThousandNode, tenThousandNode, thousandNode, hundredNode, node;
//
//        ArrayList<TreeItem<String>> hundredThousandNodes = new ArrayList<>();
//        ArrayList<TreeItem<String>> tenThousandNodes = new ArrayList<>();
//        ArrayList<TreeItem<String>> thousandNodes = new ArrayList<>();
//        ArrayList<TreeItem<String>> hundredNodes = new ArrayList<>();
//        ArrayList<TreeItem<String>> oneNodes = new ArrayList<>();
//
//        if (packetList.size() > 100000) {
//            int index = 1;
//            for (int i = 0; i < packetList.size() / 100000 + 1; i++) {
//                hundredThousandNode = new TreeItem("(" + index + "..." + ((index + 99999 < packetList.size() ? index + 99999 : packetList.size())) + ")");
//                hundredThousandNodes.add(hundredThousandNode);
//                packetsRootNode.getChildren().add(hundredThousandNode);
//                index += 100000;
//            }
//        } else if (packetList.size() > 10000) {
//            int index = 0;
//            for (int i = 1; i < packetList.size() / 10000; i++) {
//                tenThousandNode = new TreeItem("(" + index + "..." + ((index + 9999 < packetList.size() ? index + 9999 : packetList.size())) + ")");
//                tenThousandNodes.add(tenThousandNode);
//                packetsRootNode.getChildren().add(tenThousandNode);
//                index += 10000;
//            }
//        } else if (packetList.size() > 1000) {
//            int index = 1;
//            for (int i = 1; i < packetList.size() / 1000; i++) {
//                thousandNode = new TreeItem("(" + index + "..." + ((index + 999 < packetList.size() ? index + 999 : packetList.size())) + ")");
//                thousandNodes.add(thousandNode);
//                packetsRootNode.getChildren().add(thousandNode);
//                index += 1000;
//            }
//        } else if (packetList.size() > 100) {
//            int index = 1;
//            for (int i = 1; i < packetList.size() / 100; i++) {
//                hundredNode = new TreeItem("(" + index + "..." + ((index + 99 < packetList.size() ? index + 99 : packetList.size())) + ")");
//                hundredNodes.add(hundredNode);
//                packetsRootNode.getChildren().add(hundredNode);
//                index += 100;
//            }
//        } else {
//            for (int i = 1; i < packetList.size(); i++) {
//                node = new TreeItem("(" + i + ")");
//                oneNodes.add(node);
//                packetsRootNode.getChildren().add(node);
//            }
//        }

        /*
            TreeItem<String> packetNode = new TreeItem("Packet no. " + i++);
            Packet paketik = packetList.get(i-2);
            packetNode.getChildren().addAll(
                    new TreeItem("PID: " + paketik.getPID())
                    //new TreeItem("Transport Error Indicator: " + paketik.getTransportErrorIndicator()),
                   // new TreeItem("Payload Start Indicator: " + paketik.getPayloadStartIndicator()),
                   // new TreeItem("Transport Scrambling Control: " + paketik.getTransportScramblingControl()),
                    //new TreeItem("Adaptaiton Field Control: " + paketik.getTransportPriority()),
                    //new TreeItem("Continuity counter: " + paketik.getContinuityCounter())
            );
            hundredNode.getChildren().add(packetNode);

*/
}


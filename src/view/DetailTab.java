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


public class DetailTab extends Other {

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

        TreeItem<String> rootNode = createRootNode(streamDescriptor);

        TreeItem<String> PSInode = new TreeItem<>("PSI");
        {
            TreeItem PATnode = createPATnode((HashMap<Integer, Integer>) streamDescriptor.getTables().getPATmap());

            TreeItem<String> CATnode = new TreeItem<>("CAT");
            TreeItem<String> BATnode = new TreeItem<>("BAT");

            TreeItem PMTnode = createPMTnode(
                    (HashMap<Integer, String>) streamDescriptor.getTables().getProgramMap(),
                    (HashMap<Integer, Integer>)streamDescriptor.getTables().getPMTmap(),
                    (HashMap<Integer, Integer>) streamDescriptor.getTables().getESmap()
            );

            TreeItem<String> NITnode = new TreeItem<>("NIT");
            TreeItem<String> SDTnode = new TreeItem<>("SDT");
            TreeItem<String> TDTnode = new TreeItem<>("TDT");
            TreeItem<String> TOTnode = new TreeItem<>("TOT");
            TreeItem<String> SITnode = new TreeItem<>("SIT");
            TreeItem<String> SynNode = new TreeItem<>("Sync");

            PSInode.getChildren().addAll(PATnode, CATnode, BATnode, PMTnode, NITnode, SDTnode, TDTnode, TOTnode, SITnode, SynNode);
        }
        TreeItem PIDnode = createPIDnode(streamDescriptor.getMapPIDs());

        rootNode.getChildren().addAll(PSInode, PIDnode);

        return this.nodes = rootNode;
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
                new TreeItem<>("Error packets: " + streamDescriptor.getNumOfErrors() + "x")
                );
        return rootNode;
    }


    private TreeItem createPMTnode(HashMap<Integer, String> programMap,  HashMap<Integer, Integer> PMTmap, HashMap<Integer, Integer> ESmap) {
        TreeItem<String> PMTnode = new TreeItem<>("PMT");

        for (Map.Entry<Integer, String> programEntry : programMap.entrySet()) {
            TreeItem<String> programNode = new TreeItem<>("Program: " + toHex(programEntry.getKey()) + " (" + programEntry.getKey() + ")");

            for (Map.Entry<Integer, Integer> PMTentry : PMTmap.entrySet()) {

                if (programEntry.getKey() == PMTentry.getValue()) {
                    for (Map.Entry<Integer, Integer> ESentry : ESmap.entrySet()) {

                        if (ESentry.getKey().equals(PMTentry.getKey())) {
                            TreeItem<String> node = new TreeItem<>("Component PID: " + toHex(ESentry.getValue()));
                            node.getChildren().add(new TreeItem<>("Stream type: " + getElementaryStreamDescriptor(ESentry.getValue())));
                            programNode.getChildren().add(node);
                        }
                    }
                    PMTnode.getChildren().add(programNode);
                }
            }
        }
        return PMTnode;
    }


    private TreeItem createPATnode(HashMap<Integer, Integer> PATmap) {
        TreeItem<String> PATnode = new TreeItem<>("PAT");

        for (Map.Entry<Integer, Integer> pid : PATmap.entrySet()) {
            TreeItem<String> serviceNode = new TreeItem<>("Service: " + toHex(pid.getKey()) + " (" + pid.getKey() + ")");

            serviceNode.getChildren().add(new TreeItem<>("Program number: " + toHex(pid.getKey()) + " (" + pid.getKey() + ")"));
            serviceNode.getChildren().add(new TreeItem<>( "Program PMT PID: " + toHex(pid.getValue()) + " (" + pid.getValue() + ")"));

            PATnode.getChildren().add(serviceNode);
        }
        return PATnode;
    }


    private TreeItem<String> createPIDnode(HashMap<Integer, Integer> PIDmap) {
        TreeItem<String> PIDnode = new TreeItem<>("PIDs");

        for (Map.Entry<Integer, Integer> pid : PIDmap.entrySet()) {
            TreeItem<String> node = new TreeItem<>(toHex(pid.getKey()) + " (" + pid.getKey() + ")");
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
            TreeItem<String> packetNode = new TreeItem("TSpacket no. " + i++);
            TSpacket paketik = packetList.get(i-2);
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


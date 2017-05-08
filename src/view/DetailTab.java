package view;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import model.Stream;

import java.util.HashMap;
import java.util.Map;

import static app.Main.localization;
import static model.config.Config.windowHeight;
import static model.config.Config.windowWidth;
import static model.config.MPEG.*;


public class DetailTab extends Window {

    TreeItem<String> nodes;
    Tab tab;
    private TreeView treeData;


    DetailTab(){
        this.nodes = nodes;
        tab = new Tab(localization.getDetailTabText());
    }


    void createTreeTab(Stream streamDescriptor) {

        treeData = new TreeView<>(createTree(streamDescriptor));
        ScrollPane scrollPane = new ScrollPane(treeData);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefSize(windowWidth, windowHeight);

        tab.setContent(scrollPane);
    }


    private TreeItem<String> createTree(Stream streamDescriptor) {
        //TODO functions and number of occurence of table, and types and number of tableIDs
        TreeItem<String> rootNode = createRootNode(streamDescriptor);

        TreeItem<String> PSInode = new TreeItem<>("PSI");
        {
            PSInode.getChildren().addAll( new TreeItem<>("Program Specific Information Tables"));

            TreeItem PATnode = createPATnode((HashMap<Integer, Integer>) streamDescriptor.getTables().getPATmap(), streamDescriptor.getTables().getPIDmap());
            TreeItem<String> CATnode = createCATnode(streamDescriptor.getTables().getPIDmap());
            TreeItem PMTnode = createPMTnode(
                    (HashMap<Integer, String>) streamDescriptor.getTables().getProgramMap(),
                    (HashMap<Integer, Integer>)streamDescriptor.getTables().getPMTmap(),
                    (HashMap<Integer, Integer>) streamDescriptor.getTables().getESmap(),
                    streamDescriptor.getTables().getPMTnumber()
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
        node.getChildren().add(new TreeItem<>(localization.getNumerOfPacketText() + (PIDmap.get(netSyncPid)==null?0:PIDmap.get(netSyncPid))  + "x "));
        //node.getChildren().add(new TreeItem<>("Tables"));
        return node;
    }


    private TreeItem<String> createSITnode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("SIT");
        node.getChildren().add(new TreeItem<>("Service Information Table"));
        node.getChildren().add(new TreeItem<>(localization.getNumerOfPacketText() + (PIDmap.get(SITpid)==null?0:PIDmap.get(SITpid)) + "x "));
        return node;
    }


    private TreeItem<String> createTOTnode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("TDT, TOT or ST");
        node.getChildren().add( new TreeItem<>("Time Offset Table"));
        node.getChildren().add( new TreeItem<>("Time and Date Table"));
        node.getChildren().add( new TreeItem<>("Service Table"));
        node.getChildren().add(new TreeItem<>(localization.getNumerOfPacketText() + (PIDmap.get(TDT_TOT_STpid)==null?0:PIDmap.get(TDT_TOT_STpid))  + "x "));
        return node;
    }


    private TreeItem<String> createSDTnode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("SDT, BAT or ST");
        node.getChildren().add( new TreeItem<>("Service Description Table"));
        node.getChildren().add( new TreeItem<>("Bouquet Association Table"));
        node.getChildren().add( new TreeItem<>("Service Table"));
        node.getChildren().add(new TreeItem<>(localization.getNumerOfPacketText() + (PIDmap.get(SDT_BAT_STpid)==null?0:PIDmap.get(SDT_BAT_STpid)) + "x "));
        return node;
    }


    private TreeItem<String> createNITnode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("NIT or ST");
        node.getChildren().add(new TreeItem<>("Network Information Table"));
        node.getChildren().add(new TreeItem<>("Service Table"));
        node.getChildren().add(new TreeItem<>(localization.getNumerOfPacketText() + (PIDmap.get(NIT_STpid)==null?0:PIDmap.get(NIT_STpid)) + "x "));
        return node;
    }


    private TreeItem<String> createCATnode(Map PIDmap) {
        TreeItem<String> node = new TreeItem<>("CAT");
        node.getChildren().addAll( new TreeItem<>("Conditional Access Table"));
        node.getChildren().add(new TreeItem<>(localization.getNumerOfPacketText() + (PIDmap.get(CATpid)==null?0:PIDmap.get(CATpid)) + "x "));
        return node;
    }


    private TreeItem<String> createRootNode(Stream streamDescriptor) {
        TreeItem<String> rootNode = new TreeItem<>(streamDescriptor.getName());
        rootNode.setExpanded(true);

        rootNode.getChildren().addAll(
                new TreeItem<>(localization.getPathText() + streamDescriptor.getPath()),
                new TreeItem<>(localization.getSizeText()+ streamDescriptor.getSize()),
                new TreeItem<>(localization.getCreatedText() + streamDescriptor.getCreationTime()),
                new TreeItem<>(localization.getAccessText() + streamDescriptor.getLastAccessTime()),
                new TreeItem<>(localization.getModifiedText() + streamDescriptor.getLastModifiedTime()),
                new TreeItem<>(localization.getRegularText() + streamDescriptor.isRegularFile()),
                new TreeItem<>(localization.getReadonlyText() + streamDescriptor.isReadonly()),
                new TreeItem<>(localization.getOwnerText() + streamDescriptor.getOwner()),
                new TreeItem<>(localization.getTsPacketsText() + streamDescriptor.getNumOfPackets() + "x"),
                new TreeItem<>(localization.getPacketSize() + streamDescriptor.getPacketSize() + " B"),
                new TreeItem<>(localization.getErrorPackets() + streamDescriptor.getNumOfErrors() + "x"),
                new TreeItem<>(localization.getStreamIntegrity() + (streamDescriptor.getTables().isSynchronizationLost() ? "Stream corrupted" : "OK")),
                new TreeItem<>(localization.getDurationText() + parseTimestamp(streamDescriptor.getDuration())),
                new TreeItem<>(localization.getBitrateText() + String.format("%.3f",(streamDescriptor.getBitrate())/1024f/1024f*byteBinaryLength) + " Mbit/s")
        );
        return rootNode;
    }


    private TreeItem createPMTnode(HashMap<Integer, String> programMap,  HashMap<Integer, Integer> PMTmap, HashMap<Integer, Integer> ESmap, Integer packets) {
        TreeItem<String> PMTnode = new TreeItem<>("PMT");
        PMTnode.getChildren().add(new TreeItem<>("Program Map Table"));
        PMTnode.getChildren().add(new TreeItem<>(localization.getNumerOfPacketText() + packets  + "x "));

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


    private TreeItem createPATnode(HashMap<Integer, Integer> PATmap, Map PIDmap) {
        TreeItem<String> PATnode = new TreeItem<>("PAT");
        PATnode.getChildren().add(new TreeItem<>("Program Association Table"));
        PATnode.getChildren().add(new TreeItem<>(localization.getNumerOfPacketText() + (PIDmap.get(PATpid)==null?0:PIDmap.get(PATpid))  + "x "));

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
            node.getChildren().add(new TreeItem<>(localization.getNumerOfPacketText() + pid.getValue() + "x "));

            PIDnode.getChildren().add(node);
        }
        return PIDnode;
    }


    private String toHex(int pid) {
        return String.format("0x%04X", pid & 0xFFFFF);
    }

    public TreeItem getTreeData() {
        return treeData.getRoot();
    }

    public void setScene(Scene scene) {
//        this.scene = scene;
    }
}


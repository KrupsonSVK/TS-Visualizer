package view;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import model.TSpacket;
import model.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DetailTab {

    TreeItem<String> nodes;
    Tab tab;

    DetailTab(TreeItem<String> nodes){
        this.nodes = nodes;
        tab = new Tab("Stream details");
    }

    //TODO prerobit... preco vobec toto vracia daku scroll pane
    //TODO tuto dotiahnut ten scrolltab ci co to je az po spodok windowu (sceny)
    public void createTreeTab(Stream streamDescriptor) {
        VBox vbox = new VBox(new TreeView<String>(createTree(streamDescriptor)));
        vbox.setFillWidth(true);
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        this.tab.setContent(scrollPane);
    }


    public TreeItem<String> createTree(Stream streamDescriptor) {

        TreeItem<String> rootNode = new TreeItem<>(streamDescriptor.getName());
        rootNode.setExpanded(true);

        TreeItem<String> psi = new TreeItem<>("PSI");
        TreeItem<String> pids = new TreeItem<>("PIDs");
        TreeItem<String> packetsRootNode = new TreeItem<>("PSI");

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
                new TreeItem<>("TSpacket size: " + streamDescriptor.getPacketSize() + " B"),
                new TreeItem<>("Error packets: " + streamDescriptor.getNumOfErrors() + "x"),
                pids, packetsRootNode
        );

        HashMap<Integer, Integer> PIDmap = streamDescriptor.getPIDs();
        ArrayList<TSpacket> packetList = streamDescriptor.getPackets();

        for (Map.Entry<Integer, Integer> x : PIDmap.entrySet()) {
            TreeItem<String> s = new TreeItem<>("0x" + Integer.toHexString(x.getKey()) + " (" + x.getKey() + ")");
            s.getChildren().add(new TreeItem<>("packets: " + x.getValue() + "x "));
            pids.getChildren().add(s);
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
        psi.getChildren().addAll(
                new TreeItem<>("PATcode"),
                new TreeItem<>("CAT"),
                new TreeItem<>("BAT"),
                new TreeItem<>("PMTs"),
                new TreeItem<>("NIT"),
                new TreeItem<>("SDT"),
                new TreeItem<>("TDT"),
                new TreeItem<>("TOT"),
                new TreeItem<>("SIT"),
                new TreeItem<>("Sync"));

        return this.nodes = rootNode;
    }
}

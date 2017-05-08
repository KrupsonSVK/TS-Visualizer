package app;


import javafx.scene.control.TreeItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Exporter {

    public static void export(TreeItem treeData, File file) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for(TreeItem node : getAllNodes(treeData)) {
                    writer.write(node.getValue().toString() + '\n');
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<TreeItem> getAllNodes(TreeItem root) {
        ArrayList<TreeItem> nodes = new ArrayList<>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    private static void addAllDescendents(TreeItem parent, ArrayList<TreeItem> nodes) {
        for (Object node : parent.getChildren()) {
            nodes.add((TreeItem) node);
            if (node instanceof TreeItem)
                addAllDescendents((TreeItem)node, nodes);
        }
    }
}


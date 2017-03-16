package app;

import javafx.concurrent.Task;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;


public class FileHandler {

    private Task<byte[]> task;

    FileHandler() throws IOException {
        this.task = null;
    }


    protected void createTask(File file) throws IOException {

        this.task = new Task<byte[]>() {
            @Override
            public byte[] call() throws InterruptedException, IOException {

                Path filepath = Paths.get(file.getAbsolutePath());
                if (filepath == null) {
                    throw new IOException("File not found!");
                }
                BasicFileAttributes attr = Files.readAttributes(filepath, BasicFileAttributes.class);

                if (!attr.isRegularFile()) {
                    throw new IOException("File not regular!");
                }
                if (file.length() > Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())) {//TODO dorobit
                    throw new IOException("File too large!");
                }
                else {
                    byte[] buffer;
                    FileInputStream inputStream = new FileInputStream(file);
                    buffer = new byte[Math.toIntExact(file.length())];

                    if (inputStream.read(buffer) == -1) {
                        throw new IOException("EOF reached while trying to read the whole file");
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    return buffer;
                }
            }
        };
    }

    public Task<byte[]> getTask () { return task; }
}



package com.drowndreamer.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String res =  fileOperationTool.readFile("test.txt");
        Assertions.assertNotNull(res);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        Assertions.assertNotNull(fileOperationTool.writeFile("test.txt", "This is a test"));
    }
}
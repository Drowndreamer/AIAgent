package com.drowndreamer.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.drowndreamer.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

public class FileOperationTool {
    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName) {
        String filePatn = FILE_DIR + "/" + fileName;
        try {
            String content = FileUtil.readUtf8String(filePatn);
            System.out.println(content);
           return content;
        } catch (IORuntimeException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    public String writeFile(@ToolParam(description = "Name of a file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        try {
            String filePath = FILE_DIR + "/" + fileName;
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File written Successfully to" + filePath;
        } catch (Exception e) {
            return "Error writing file: " + e.getMessage();
        }
    }
}

package com.xue.aiagent_me;
import com.xue.aiagent_me.tools.FileOperationTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileOperationToolTest {

    @Test
    void writeFile() {
        String result = new FileOperationTool().writeFile(
                "编程学习.txt",
                " https://github.com/Flower187 程序员编程学习交流社区"
        );
        System.out.println(result);
        assertNotNull(result);
    }

    @Test
    void readFile() {
        String result = new FileOperationTool().readFile("编程学习.txt");
        assertNotNull(result);
        System.out.println(result);
    }
}

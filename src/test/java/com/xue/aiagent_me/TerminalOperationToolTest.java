package com.xue.aiagent_me;

import com.xue.aiagent_me.tools.TerminalOperationTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TerminalOperationToolTest {
    @Test
    void executeTerminalCommand() {
        String result = new TerminalOperationTool().executeTerminalCommand("echo Hello World");
        System.out.println("result = " + result);
        Assertions.assertNotNull(result);
    }
}

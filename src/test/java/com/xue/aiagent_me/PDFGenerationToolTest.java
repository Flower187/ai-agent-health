package com.xue.aiagent_me;

import com.xue.aiagent_me.tools.PDFGenerationTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        String result = new PDFGenerationTool().generatePDF(
                "编程导航原创项目.pdf", "编程导航原创项目"
        );
        System.out.println("result = " + result);
        Assertions.assertNotNull(result);
    }
}

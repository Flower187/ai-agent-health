package com.xue.aiagent_me;

import com.xue.aiagent_me.tools.ResourceDownloadTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceDownloadToolTest {


    @Test
    public void downloadResource() {
        String result = new ResourceDownloadTool().downloadResource(
                "https://pic.code-nav.cn/jianqiezhushou/advertisement/1752524663997030402/zx3aIzSx-jianqiezhushou_advertisement.png", "jinqiezhushou.png"
        );
        System.out.println("result = " + result);
        Assertions.assertNotNull(result);
    }

}

package com.itheima.test;

import org.junit.jupiter.api.Test;

public class uploadFileTest {
    @Test
    public void test(){

        String filename = "raerfea.jpg";
        //根据索引获取文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        System.out.println(suffix);

    }

}

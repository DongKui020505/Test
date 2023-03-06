package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        // 生成文件名
        String fileName = UUID.randomUUID().toString()
                        + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
        // 创建目录
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 将文件转存到指定位置
        file.transferTo(new File(basePath + fileName));
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        // 文件输入流、输出流
        FileInputStream inputStream = new FileInputStream(new File(basePath + name));
        ServletOutputStream outputStream = response.getOutputStream();
        // 设置响应方式
        response.setContentType("image/jpeg");
        // 写回浏览器
        int len = 0;
        byte[] bytes = new byte[1024];
        while((len = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
            outputStream.flush();
        }
        inputStream.close();
        outputStream.close();
    }
}

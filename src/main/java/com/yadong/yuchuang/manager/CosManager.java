package com.yadong.yuchuang.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.yadong.yuchuang.config.CosClientConfig;
import com.yadong.yuchuang.exception.BusinessException;
import com.yadong.yuchuang.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传文件
     *
     * @param key  文件存储地址
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载文件
     *
     * @param key 文件路径
     * @return
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 删除COS中文件
     *
     * @param key 文件的key
     */
    public void deletePictureObject(String key) {
        // Bucket 的命名格式为 BucketName-APPID ，此处填写的存储桶名称必须为此格式
        String bucketName = cosClientConfig.getBucket();
        // 指定被删除的文件在 COS 上的路径，即对象键。例如对象键为 folder/picture.jpg，则表示删除位于 folder 路径下的文件 picture.jpg
        cosClient.deleteObject(bucketName, key);
    }

    /**
     * 上传图片到COS
     */
    public String uploadPicture(String key, File file) {
        // 上传文件
        PutObjectResult putObjectResult = putObject(key, file);
        if (putObjectResult == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        }
        // 构建可访问的url
        String url = String.format("%s/%s", cosClientConfig.getHost(), key);
        log.info("上传图片成功，访问地址为：{}", url);
        return url;
    }
}

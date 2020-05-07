package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.spring.web.json.Json;

import java.io.*;
import java.util.*;

@Service
@ConfigurationProperties(prefix = "media")
@Data
public class MediaUploadService {
    @Autowired
    MediaFileRepository mediaFileRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    String uploadLocation;

    private String getFileFolderPath(String fileMd5){
        return uploadLocation+fileMd5.substring(0,1)+"/"
                +fileMd5.substring(1,2)+"/"+fileMd5+"/";
    }

    private String getFilePath(String fileMd5,String fileExt){
        return uploadLocation+fileMd5.substring(0,1)+"/"
                +fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+"."+fileExt;
    }

    private String getChunkFileFolderPath(String fileMd5){
        return uploadLocation+fileMd5.substring(0,1)+"/"
                +fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/";
    }

    //文件上传前的准备，检查文件是否存在
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimeType, String fileExt) {
        //检查文件在磁盘上是否存在
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        String filePath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        boolean exists = file.exists();

        //检查文件信息在mongodb上是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (exists && optional.isPresent()){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }

        //检查文件所在目录是否存在，如果不存在则创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()){
            fileFolder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //分块检查，检查分块文件是否存在
    public CheckChunkResult checkChunk(String fileMd5, Integer chunk, Integer chunkSize) {
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFile = new File(chunkFileFolderPath+chunk);
        if (chunkFile.exists()){
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        }else {
            return new CheckChunkResult(CommonCode.SUCCESS,false);
        }
    }

    //上传分块
    public ResponseResult uploadChunk(MultipartFile file, String fileMd5, Integer chunk) {
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath+chunk;
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()){
            chunkFileFolder.mkdirs();
        }
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //合并文件
    public ResponseResult mergeChunks(String fileMd5, String fileName, Long fileSize, String mimeType, String fileExt) {
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);
        mergeFile = this.mergeFile(fileList, mergeFile);
        if (mergeFile == null){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if (!checkFileMd5){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        String filePath1 = fileMd5.substring(0,1)+"/"
                +fileMd5.substring(1,2)+"/"+fileMd5+"/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimeType);
        mediaFile.setFileType(fileExt);
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //发送视频处理消息
    public ResponseResult sendProcessVideoMsg(String mediaId){
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        Map<String,String> map = new HashMap<>();
        map.put("mediaId",mediaId);
        String jsonString = JSON.toJSONString(map);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonString);
        }catch (AmqpException e){
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private File mergeFile(List<File> chunkFileList,File mergeFile){
        try {
            if (mergeFile.exists()){
                mergeFile.delete();
            }else {
                mergeFile.createNewFile();
            }
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName())> Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;
                }
            });
            RandomAccessFile ref_write = new RandomAccessFile(mergeFile,"rw");
            byte[] b = new byte[1024];
            for (File chunkFile : chunkFileList) {
                RandomAccessFile ref_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while ((len = ref_read.read(b))!=-1){
                    ref_write.write(b,0,len);
                }
                ref_read.close();
            }
            ref_write.close();
            return mergeFile;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    //校验文件
    private boolean checkFileMd5(File mergeFile,String md5){
        try {
            FileInputStream inputStream = new FileInputStream(mergeFile);
            String md5Hex = DigestUtils.md5Hex(inputStream);
            if (md5.equalsIgnoreCase(md5Hex)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

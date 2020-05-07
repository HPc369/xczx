package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFile {



    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("D:\\BaiduNetdiskDownload\\lucene.mp4");
        String chunkFileFolder = "D:\\BaiduNetdiskDownload\\chunk\\";
        long chunkFileSize = 1*1024*1024;
        long chunkFileNum = (long) Math.ceil(sourceFile.length()*1.0/chunkFileSize);
        RandomAccessFile ranf_read = new RandomAccessFile(sourceFile,"r");
        byte[] b = new byte[1024];
        for (int i=0;i<chunkFileNum;i++){
            File chunkFile = new File(chunkFileFolder+i);
            RandomAccessFile raf_write = new RandomAccessFile(chunkFile,"rw");;
            int len = -1;
            while ((len = ranf_read.read(b)) != -1){
                raf_write.write(b,0,len);
                if (chunkFile.length()>=chunkFileSize){
                    break;
                }
            }
            raf_write.close();
        }
        ranf_read.close();

    }
    //测试文件合并
    @Test
    public void testMergeFile() throws IOException {
        //块文件目录
        String chunkFileFolderPath = "D:\\BaiduNetdiskDownload\\chunk\\";
        //块文件目录对象
        File chunkFileFolder = new File(chunkFileFolderPath);
        //块文件列表
        File[] files = chunkFileFolder.listFiles();
        //将块文件排序，按名称升序
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;

            }
        });

        //合并文件
        File mergeFile = new File("D:\\BaiduNetdiskDownload\\lucene_merge.mp4");
        //创建新文件
        boolean newFile = mergeFile.createNewFile();

        //创建写对象
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");

        byte[] b = new byte[1024];
        for(File chunkFile:fileList){
            //创建一个读块文件的对象
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
            int len = -1;
            while((len = raf_read.read(b))!=-1){
                raf_write.write(b,0,len);
            }
            raf_read.close();
        }
        raf_write.close();
    }
}

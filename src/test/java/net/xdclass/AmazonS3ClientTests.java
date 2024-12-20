package net.xdclass;

import cn.hutool.core.date.DateUtil;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

@SpringBootTest
@Slf4j
class AmazonS3ClientTests {
	
	@Autowired
	private AmazonS3Client amazonS3Client;
	
	
	//=========Bucket================

	/**
	 * 判断bucket是否存在
	 */
	@Test
	public void testBucketExists() {
		boolean bucketExist = amazonS3Client.doesBucketExist("ai-pan1");
		log.info("bucket是否存在:{}",bucketExist);
	}

	/**
	 * 创建bucket
	 */
	@Test
	public void testCreateBucket() {
		String bucketName = "ai-pan1";
		Bucket bucket = amazonS3Client.createBucket(bucketName);
		log.info("bucket:{}",bucket);
	}

	/**
	 * 删除bucket
	 */
	@Test
	public void testDeleteBucket() {
		String bucketName = "ai-pan1";
		amazonS3Client.deleteBucket(bucketName);
	}

	/**
	 * 获取全部bucket
	 */
	@Test
	public void testListBuckets() {
		for (Bucket bucket : amazonS3Client.listBuckets()) {
			log.info("bucket:{}",bucket.getName());
		}
	}

	/**
	 * 根据bucket名称获取bucket详情
	 */
	@Test
	public void testGetBucket() {
		String bucketName = "ai-pan1";
		Optional<Bucket> optionalBucket = amazonS3Client.listBuckets().stream().filter(bucket -> bucketName.equals(bucket.getName())).findFirst();
		if (optionalBucket.isPresent()) {
			log.info("bucket:{}",optionalBucket.get());
		}else {
			log.info("bucket不存在");
		}

	}


	//=====================操作文件相关===========================

	/**
	 * 上传单个文件，直接写入文本
	 */
	@Test
	public void testUploadFile() {
		PutObjectResult putObject = amazonS3Client.putObject("ai-pan", "test.txt", "hello world");
		log.info("putObject:{}",putObject);
	}

	/**
	 * 上传单个文件，采用本地文件路径
	 */
	@Test
	public void testUploadFileByPath() {
		PutObjectResult putObject = amazonS3Client.putObject("ai-pan", "/aa/bb/1111.png",
				new File("/Users/xdclass/Desktop/chunk/1.png"));
		log.info("putObject:{}",putObject);
	}


	/**
	 * 上传文件，输入流的方式，带上文件元数据
	 */
	@Test
	@SneakyThrows
	public void testUploadFileByInputStream() {
		try (FileInputStream inputStream = new FileInputStream("/Users/xdclass/Desktop/chunk/1.png")){
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType("image/png");
			PutObjectResult putObject = amazonS3Client.putObject("ai-pan", "/aa/1.png", inputStream, objectMetadata);
			log.info("putObject:{}",putObject.getContentMd5());
		}
	}


	/**
	 * 获取文件
	 */
	@Test
	@SneakyThrows
	public void testGetFile() {
		try (FileOutputStream outputStream = new FileOutputStream(new File("/Users/xdclass/Desktop/chunk/test111.txt"))){
			S3Object s3Object = amazonS3Client.getObject("ai-pan", "test.txt");
			s3Object.getObjectContent().transferTo(outputStream);
		}

	}


	/**
	 * 删除文件
	 */
	@Test
	public void testDeleteFile() {
		amazonS3Client.deleteObject("ai-pan", "/aa/1.png");
	}


	/**
	 * 生成访问地址
	 */
	@Test
	public void testGeneratePresignedUrl() {
		// 预签名url过期时间(ms)
		long PRE_SIGN_URL_EXPIRE = 60 * 10 * 1000L;
		// 计算预签名url的过期日期
		Date expireDate = DateUtil.offsetMillisecond(new Date(), (int) PRE_SIGN_URL_EXPIRE);
		// 创建生成预签名url的请求，并设置过期时间和HTTP方法, withMethod是生成的URL访问方式,是权限控制的一种方式
		GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest("ai-pan", "/aa/bb/1111.png")
				.withExpiration(expireDate).withMethod(HttpMethod.PUT);


		// 生成预签名url
		URL preSignedUrl = amazonS3Client.generatePresignedUrl(request);

		// 输出预签名url
		System.out.println(preSignedUrl.toString());
	}


}

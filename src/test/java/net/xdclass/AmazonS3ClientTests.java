package net.xdclass;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

}

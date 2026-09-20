package com.watchora.streamingservice.Service;

import com.watchora.streamingservice.dto.StreamingResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingService {

    private  final S3Client s3Client;
    private  final S3Presigner s3Presigner;
    private  final RedisTemplate<String, String> redisTemplate;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.presigned.url.expiry}")
    private long presignedUrlExpiry;

    // Redis key for caching Streaming Urls
    private  final static String STREAMING_URL_CACHE_PREFIX = "Streaming:url";


    /**
     *  Get steaming url for a movie
     *  flow ---->
     *  1. check redis cache for existing presigned url
     *  2. if cached -- return immediately
     *  if not Cached = generate new presigned Url from s3
     *  Cache the url in Redis
     *  Return Streaming url
     *  Any presigned  url ?
     *  -> S3 bucket is private locker  room : video are not accessible
     *  -----> Presigned URL give temporary access (X minutes )
     *  prevent unauthorized video downloads
     *
     */

    public StreamingResponse getStreamingUrl(String movieId, String playlistKey) {
        log.info("Getting Streaming url for movie : {}", movieId);

        String cacheKey = STREAMING_URL_CACHE_PREFIX + movieId;

        // check redis  cache first

        String cacheUrl = redisTemplate.opsForValue().get(cacheKey);
        if (cacheUrl != null) {
            log.info("Streaming url has been cached : {}", cacheUrl);
            return new StreamingResponse(movieId, cacheUrl,
                    "1080p, 720p, 480p, 360p", presignedUrlExpiry);
        }

        // Generate presigned url from S3
        log.info("Generate new presigned Url from movie  : {}", movieId);
        String presignedUrl = generatePresignedUrl(playlistKey);


        redisTemplate.opsForValue().set(cacheKey, presignedUrl, 55, TimeUnit.MINUTES);
        log.info("Presigned Url has been cached : {}", presignedUrl);


        return new StreamingResponse(movieId, presignedUrl, "1080p, 720p, 480p, 360p", presignedUrlExpiry);
    }

    /**
     *  This is key method that make every thing secure
     * @param movieId @discription
     * @param playlistPath @discription
     * @return @discription
     */

    public String getSignedPlaylist(
            String movieId,
            String playlistPath
    ) {
        String basePath = playlistPath.substring(0, playlistPath.lastIndexOf("/") + 1);

        // Read e3u8 content from s3

        String m3u8Content = readFromS3(playlistPath);

        // Rewrite each line that is a segment or playlist reference
        String signedContent = rewriteM3u8signedUrls(
                m3u8Content, basePath
        );

        return signedContent;
    }


    /**
     *
     * @param m3u8Content @description
     * @param basePath @description
     * @return String
     */
    private  String rewriteM3u8signedUrls(String m3u8Content, String basePath) {
        StringBuilder rewritten = new StringBuilder();

        for(String line : m3u8Content.split("\n")) {
            String trimmedLine = line.trim();

            // skip empty line and comments

            if(trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                rewritten.append(trimmedLine).append("\n");
                continue;
            }
            // This is a segment or playlist reference
            // build full s3 and signed it

            String fullKey = basePath + trimmedLine;
            String signedUrl = generatePresignedUrl(fullKey);

            rewritten.append(signedUrl).append("\n");
        }
        return rewritten.toString();
    }

    private String readFromS3(String s3key) {
        GetObjectRequest request =GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3key)
                .build();

        ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);

        return  new BufferedReader(new InputStreamReader(response)).lines().collect(Collectors.joining("\n"));
    }


    /**
     *  Generate a presigned url for s3 object
     *  URL expired after configured time
     */

    private String generatePresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpiry))
                .getObjectRequest(getObjectRequest).build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();

    }

    public void invalidateCache(String movieId) {
            String cacheKey = STREAMING_URL_CACHE_PREFIX + movieId;
            redisTemplate.delete(cacheKey);
            log.info("Removing cached Url from Streaming Cache : {}", cacheKey);
    }
}

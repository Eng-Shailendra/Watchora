package com.watchora.encodingservice.service;

import com.watchora.encodingservice.event.VideoEncodedEvent;
import com.watchora.encodingservice.event.VideoUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class EncodingService {

    private final S3Client s3Client;
    private final KafkaTemplate<String, VideoEncodedEvent> kafkaTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${encoding.basePath}")
    private String basePath;

    private static final  String VIDEO_ENCODED_EVENT_TOPIC = "video.encoded";

    // Video Qualities to encode
    // Format : resolution, bitrate, height

    private  static  final List<int[]> VIDEO_QUALITIES = Arrays.asList(
            new int[]{1920, 5000, 1080}, // 1080p - 5000k bitrate
            new int[]{1280, 2800, 720}, // 720 - 2800k bitrate
            new int[]{854, 1200, 480},  // 480 - 1200k bitrate
            new int[]{640, 800, 360} // 360 - 800k bitrate
    );

    /**
     * main Encoding pipeline
     * Step
     * 1. Download row video from S3
     * 2. Encode to multiple qualities using FFmpeg
     * 3. Generate master playlist
     * 4. Create Master playlist
     * 5. upload all encoded file back to s3
     * 6. Publish videoEncodedEvent to Kafka
     */

    public  void  encodeVideo(VideoUploadedEvent event) {
        log.info("Encoding Video : {} ", event.getMovieId());

        // Create a unique path for a movie
        String jobPath = basePath+"/" + event.getMovieId();

        try {
            // Create temp directories
            Files.createDirectories(Paths.get(jobPath));
            Files.createFile(Paths.get(jobPath + "/encoded" ));

            // Step 1 : Download row video from S3
            String localVideoPath = jobPath + "/raw_video.mp4";
            downloadFromS3(event.getVideoKey(), localVideoPath);
            log.info("Row video downloaded to {}", localVideoPath);


            // step 2 & 3 : Encode to multiple Qualities + generate HLS
            for(int [] qualities: VIDEO_QUALITIES ){
                int width = qualities[0];
                int height = qualities[2];
                int bitrate = qualities[1];

                String qualityDir = jobPath + "/encoded"+ height + "/" + height + "p" ;
                Files.createDirectories(Paths.get(qualityDir));

                encodeToHls(localVideoPath, qualityDir, width, height, bitrate);
                log.info("Encoded {}p successfully", height);

            }

            //Step 4 : Generate master playlist
            String masterPlayListPath = jobPath + "/encoded/master.m3u8";
            generateMasterPlayList(masterPlayListPath);
            log.info("Master playlist generated to {}", masterPlayListPath);

            // Step 5 : Upload all resources file to S3
            String encodedPrefix = "encoded/"+ event.getMovieId() + "/";
            uploadEncodedFileToS3(jobPath + "/encoded", encodedPrefix);
            log.info("Encoded files uploaded to s3 {}", encodedPrefix);

            // step 6 : Publish videoEncodedEvent
            String masterPlaylistKey = encodedPrefix+"master.m3u8";
            String hlsUrl = "https://"+bucketName+"s3.amazonaws.com"+ masterPlaylistKey;

            VideoEncodedEvent encodedEvent = new VideoEncodedEvent(
                    event.getMovieId(),
                    hlsUrl,
                    masterPlaylistKey ,
                    true,
                    null
            );

            kafkaTemplate.send(VIDEO_ENCODED_EVENT_TOPIC, event.getMovieId(), encodedEvent);
            log.info("VideoEncodedEvent Published for movie : {} ", event.getMovieId());
        }catch (Exception e){
            log.error("Encoding failed for movie : {} - {}", event.getMovieId(), e.getMessage());

            VideoEncodedEvent failerEncodedEvent = new VideoEncodedEvent(
                    event.getMovieId(),
                    null,
                    null,
                    false,
                    e.getMessage()
            );
            kafkaTemplate.send(VIDEO_ENCODED_EVENT_TOPIC, event.getMovieId() ,failerEncodedEvent);
        }
        finally {
            // cleanup temp files
            cleanupTempFile(jobPath);
        }
    }


    /**
     * Download file from S3 to local path
     */

    private  void downloadFromS3(String s3key, String localVideoPath){
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3key)
                .build();
        s3Client.getObject(getObjectRequest, Paths.get(localVideoPath));
    }




    /**
     * Encoding video to hsl FFmpeg.
     * FFmpeg command created:
     *  -multiple .ts segment  file (10 second each)
     *  - A .m3u8 play List file for this quality
     * @param inputPath @description
     * @param outputDir @description
     * @param width @description
     * @param height @description
     * @param bitrate @description
     * @throws IOException @description
     * @throws InterruptedException @description
     */
    private  void  encodeToHls(String inputPath, String outputDir, int width, int height, int bitrate) throws IOException , InterruptedException {
        String playlistPath = outputDir + "/playlist.m3u8";
        String segmentPattern = outputDir + "/segmaent_%03d.ts";


    // FFmpeg Command for hls encoding

        List<String> command = Arrays.asList(
                ffmpegPath,
                "-i", inputPath, // input file
                "-vf", "scale=" + width + ":" + height,  // scale tp resolution
                "-c:v", "libx264",                       // video coded
                "-b:v", bitrate+ "k",                    // video bitrate
                "-c:a", "aac",                           // Audio coded
                "-b:a", "128k",                          // Audio bitrate
                "-hls_time", "10",                       // 10 second all segments
                "-hls_list_size", "0",                   // keep all segments
                "-hls_segment_filename", segmentPattern, // segment  naming
                "-f", "hls",                             // output format HLS
                playlistPath                             // output playlist
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int exitCode = process.waitFor();
        if(exitCode != 0){
            throw  new RuntimeException("FFmpeg encoding failed");
        }
    }






    /**
     * Generate master HLS playlist that references all quality playlist.
     * This is the file the video player download first.
     * @param masterPlayListPath @description
     * @throws IOException@description
     */
    private void generateMasterPlayList(String masterPlayListPath) throws IOException {
        StringBuilder master = new StringBuilder();
        master.append("#EXTM#U\n");
        master.append("#EXT-X-VERSION: 3\n\n");

        // add each quality to master playlist

        int [][] qualities = {{1920, 5000, 1080}, {1280, 2800, 720}, {854, 1200, 480}, {640, 800, 360}};
        for (int [] q : qualities){
            int width = q[0];
            int bitrate = q[1];
            int  height = q[2];
            master.append("#EXT-X-STREAM-INF:BANDWIDTH=")
                    .append(bitrate*1000)
                    .append(", RESOLUTION=").append(width).append("X").append(height)
                    .append(",CODECS=\"avc1.42e01e,mp4a.40.2\"\n");
            master.append(height).append("p/playlist.m3u8\n\n");
        }
        Files.writeString(Paths.get(masterPlayListPath), master.toString());


    }



    /**
     * Upload all encoded file directory back to  s3
     */
    private void  uploadEncodedFileToS3(String localDir, String s3Prefix ){
        File directory = new File(localDir);
        uploadDirectoryToS3(directory, localDir, s3Prefix);

    }

    private  void uploadDirectoryToS3(File dir, String baseDir, String s3Prefix) throws NullPointerException{
        for  (File file: dir.listFiles()) {
            if (!file.isDirectory()){
                throw new NullPointerException("Not a directory");
            }
            else if (file.isDirectory()){
                uploadDirectoryToS3(file, baseDir, s3Prefix);
            }else{
                String relativePath = file.getAbsolutePath().substring(baseDir.length()+1)
                        .replace("\\", "/");

                String s3key = s3Prefix + relativePath;
                String contentType= file.getName().endsWith(".m3u8")
                        ? "application/x-mpegURL": "Video/MP2T";

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName).key(s3key).contentType(contentType).build();


                s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
                log.info("Uploaded {} to S3", s3key);
            }
        }
    }

    /**
     * Cleanup temp file after encoding
     * @param jobPath @Discription
     */
    private void cleanupTempFile(String jobPath){
        try {
            Path dirPath = Paths.get(jobPath);
            if (Files.exists(dirPath)){
                Files.walk(dirPath).sorted(Comparator.reverseOrder())
                        .map(Path::toFile).forEach(file -> file.delete());

                log.info("Deleted temp file {}", jobPath);

            }
        }
        catch (IOException e) {
            log.warn("Failed to cleanup temp file : {}", e.getMessage());
        }
    }
}

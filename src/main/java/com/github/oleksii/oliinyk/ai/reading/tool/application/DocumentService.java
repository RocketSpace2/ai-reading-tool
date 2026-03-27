package com.github.oleksii.oliinyk.ai.reading.tool.application;

import com.github.oleksii.oliinyk.ai.reading.tool.presentation.dto.DocumentResponse;
import com.github.oleksii.oliinyk.ai.reading.tool.presentation.dto.FolderDocumentsResponse;
import com.github.oleksii.oliinyk.ai.reading.tool.presentation.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    @Value("${storage.s3.bucket}")
    private String bucket;

    @Autowired
    private S3Client s3;

    @PostConstruct private void ensureBucketExists() {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    public record DocumentDownload(
            Resource resource,
            String documentName,
            String contentType,
            Long sizeBytes
    ) {}

    public void upload(String notes, String objectKey) {
        byte[] bytes = notes.getBytes(StandardCharsets.UTF_8);

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType("text/markdown; charset=utf-8")
                .build();

        s3.putObject(req, RequestBody.fromBytes(bytes));
    }

    public DocumentDownload download(String objectKey) {
        try{
            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Stream = s3.getObject(req);
            GetObjectResponse response = s3.getObject(req).response();

            return new DocumentDownload(
                    new InputStreamResource(s3Stream),
                    extractFileName(objectKey),
                    response.contentType(),
                    response.contentLength()
            );
        } catch (S3Exception e){
            throw new ResourceNotFoundException("File not found: " + objectKey);
        }
    }

    public DocumentResponse delete(String objectKey) {
        try {
            HeadObjectRequest headReq = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            HeadObjectResponse headRes = s3.headObject(headReq);

            DeleteObjectRequest req = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3.deleteObject(req);

            return new DocumentResponse(
                    objectKey,
                    extractFileName(objectKey),
                    headRes.contentType(),
                    headRes.contentLength()
            );
        }catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("File not found: " + objectKey);
        }
    }

    public List<String> listFolders() {
        ListObjectsRequest req = ListObjectsRequest.builder()
                .bucket(bucket)
                .delimiter("/")
                .build();

        ListObjectsResponse resp = s3.listObjects(req);

        return resp.commonPrefixes().stream()
                .map(CommonPrefix::prefix)
                .map(p -> p.endsWith("/") ? p.substring(0, p.length() - 1) : p)
                .toList();
    }

    public List<FolderDocumentsResponse> getListOfFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .build();

        ListObjectsV2Response response = s3.listObjectsV2(request);

        Map<String, List<DocumentResponse>> grouped = new LinkedHashMap<>();

        for (S3Object object : response.contents()) {
            String objectKey = object.key();

            if (objectKey.endsWith("/")) {
                continue;
            }

            String folderName = extractFolderName(objectKey);
            String fileName = extractFileName(objectKey);

            HeadObjectResponse head = s3.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build()
            );

            DocumentResponse document = new DocumentResponse(
                    objectKey,
                    fileName,
                    head.contentType() != null ? head.contentType() : "application/octet-stream",
                    head.contentLength()
            );

            grouped.computeIfAbsent(folderName, k -> new ArrayList<>()).add(document);
        }

        return grouped.entrySet().stream()
                .map(entry -> new FolderDocumentsResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private String extractFolderName(String objectKey) {
        int slashIndex = objectKey.indexOf('/');
        return objectKey.substring(0, slashIndex);
    }

    private String extractFileName(String objectKey) {
        int lastSlash = objectKey.indexOf('/');
        return objectKey.substring(lastSlash + 1);
    }
}


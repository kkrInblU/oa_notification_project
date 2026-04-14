package com.gdut.oanotification.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttachmentItem {

    private String fileId;
    private String filename;
    private String extension;
    private Long size;
}

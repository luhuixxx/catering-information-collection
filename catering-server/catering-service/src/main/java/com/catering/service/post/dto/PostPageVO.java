package com.catering.service.post.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostPageVO<T> {
    private long total;
    private long page;
    private long size;
    private List<T> records;
}

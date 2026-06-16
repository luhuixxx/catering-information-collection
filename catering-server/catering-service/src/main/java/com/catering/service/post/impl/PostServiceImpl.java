package com.catering.service.post.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.dao.mapper.PostMapper;
import com.catering.model.entity.Post;
import com.catering.service.post.PostService;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
}


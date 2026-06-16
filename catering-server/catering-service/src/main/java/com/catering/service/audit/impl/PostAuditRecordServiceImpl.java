package com.catering.service.audit.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.dao.mapper.PostAuditRecordMapper;
import com.catering.model.entity.PostAuditRecord;
import com.catering.service.audit.PostAuditRecordService;
import org.springframework.stereotype.Service;

@Service
public class PostAuditRecordServiceImpl extends ServiceImpl<PostAuditRecordMapper, PostAuditRecord> implements PostAuditRecordService {
}


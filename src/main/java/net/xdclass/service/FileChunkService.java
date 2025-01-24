package net.xdclass.service;

import net.xdclass.controller.req.FileChunkInitTaskReq;
import net.xdclass.dto.FileChunkDTO;

public interface FileChunkService {
    /**
     * 初始化分片上传
     * @param req
     * @return
     */
    FileChunkDTO initFileChunkTask(FileChunkInitTaskReq req);
}

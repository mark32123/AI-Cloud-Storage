package net.xdclass.service;

import net.xdclass.controller.req.FolderCreateReq;

public interface AccountFileService {
    /**
     * 创建文件夹
     * @param createRootFolderReq
     */
    void createFolder(FolderCreateReq createRootFolderReq);
}

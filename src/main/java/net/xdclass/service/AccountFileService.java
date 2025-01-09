package net.xdclass.service;

import net.xdclass.controller.req.*;
import net.xdclass.dto.AccountFileDTO;
import net.xdclass.dto.FolderTreeNodeDTO;

import java.util.List;

public interface AccountFileService {
    /**
     * 获取文件列表
     * @param accountId
     * @param parentId
     * @return
     */
    List<AccountFileDTO> listFile(Long accountId, Long parentId);

    /**
     * 创建文件夹
     * @param req
     */
    Long createFolder(FolderCreateReq req);

    /**
     * 重命名文件
     * @param req
     */
    void renameFile(FileUpdateReq req);

    /**
     * 文件树接口
     *
     * @param accountId
     * @return
     */
    List<FolderTreeNodeDTO> folderTree(Long accountId);

    /**
     * 文件树接口
     *
     * @param accountId
     * @return
     */
     List<FolderTreeNodeDTO> folderTreeV2(Long accountId);

    /**
     * 普通小文件上传
     * @param req
     */
    void fileUpload(FileUploadReq req);

    /**
     * 批量移动目标文件夹
     * @param req
     */
    void moveBatch(FileBatchReq req);

    /**
     * 文件的批量删除
     * @param req
     */
    void delBatch(FileDelReq req);

    /**
     * 文件复制
     * @param req
     */
    void copyBatch(FileBatchReq req);
}

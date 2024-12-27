package net.xdclass.service;

import net.xdclass.dto.AccountFileDTO;

import java.util.List;

public interface FileService {
    /**
     * 获取文件列表
     * @param accountId
     * @param parentId
     * @return
     */
    List<AccountFileDTO> listFile(Long accountId, Long parentId);
}

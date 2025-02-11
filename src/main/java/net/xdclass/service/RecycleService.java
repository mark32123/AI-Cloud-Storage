package net.xdclass.service;

import net.xdclass.dto.AccountFileDTO;

import java.util.List;

public interface RecycleService {
    List<AccountFileDTO> listRecycleFiles(Long accountId);
}

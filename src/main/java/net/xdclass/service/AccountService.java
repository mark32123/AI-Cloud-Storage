package net.xdclass.service;

import net.xdclass.controller.req.AccountRegisterReq;
import org.springframework.web.multipart.MultipartFile;

public interface AccountService {
    void register(AccountRegisterReq req);

    String uploadAvatar(MultipartFile file);
}

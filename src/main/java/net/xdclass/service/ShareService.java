package net.xdclass.service;

import net.xdclass.controller.req.ShareCreateReq;
import net.xdclass.dto.ShareDTO;

import java.util.List;

public interface ShareService {

    List<ShareDTO> listShare();

    ShareDTO createShare(ShareCreateReq req);
}

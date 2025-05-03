package net.xdclass.service;

import reactor.core.publisher.Flux;

public interface StreamService {

    public Flux<String> handleChatStream(String token,String message);

}

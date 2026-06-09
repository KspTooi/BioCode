package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.aacp.model.AacpMcpPo;
import com.ksptool.bio.biz.aacp.repository.AacpMcpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AacpEndpointService {

    /** 存放 SessionID 对应的 SSE 连接 */
    private final Map<String, SseEmitter> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    private AacpMcpRepository aacpMcpRepository;

    /**
     * 建立 SSE 连接：校验 MCP 编码 → 生成 SessionID → 创建 Emitter → 下发 endpoint URI
     *
     * @param code MCP 唯一编码
     * @return SSE 连接
     * @throws BizException 
     */
    public SseEmitter upstream(String code) throws BizException {

        AacpMcpPo po = aacpMcpRepository.findByCode(code);

        if (po == null) {
            throw new BizException("MCP服务器不存在:" + code);
        }

        if (po.getStatus() != 1) {
            throw new BizException("MCP服务器当前不接受连接请求:" + code);
        }

        String sessionId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(3600000L);

        sessionMap.put(sessionId, emitter);

        emitter.onCompletion(() -> sessionMap.remove(sessionId));
        emitter.onTimeout(() -> sessionMap.remove(sessionId));

        try {
            emitter.send(SseEmitter.event()
                    .name("endpoint")
                    .data("/aacpMcp/request?sessionId=" + sessionId));
        } catch (IOException e) {
            sessionMap.remove(sessionId);
        }
        return emitter;
    }
}

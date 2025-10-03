package com.spring.Live.Vehicle.Map.Delhi.controller;

import com.spring.Live.Vehicle.Map.Delhi.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {

    private final NotificationService notificationService;

    public SseController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(path = "/vehicles", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamVehicles() {
        // 0L -> infinite timeout (no automatic server-side timeout)
        SseEmitter emitter = new SseEmitter(0L);

        // register emitter to service so poller can push to it
        notificationService.addEmitter(emitter);

        // cleanup callbacks — always remove the emitter on finish/timeout/error
        emitter.onCompletion(() -> {
            notificationService.removeEmitter(emitter);
        });
        emitter.onTimeout(() -> {
            notificationService.removeEmitter(emitter);
            emitter.complete(); // ensure it's closed
        });
        emitter.onError((ex) -> {
            notificationService.removeEmitter(emitter);
            try { emitter.completeWithError(ex); } catch (Exception ignore) {}
        });

        // optional: immediately send a welcome/heartbeat so client knows it's connected
        try {
            SseEmitter.SseEventBuilder init = SseEmitter.event()
                    .name("connected")
                    .data("{\"type\":\"connected\",\"payload\":\"ok\"}");
            emitter.send(init);
        } catch (Exception e) {
            // ignore initial send issues
        }

        return emitter;
    }
}

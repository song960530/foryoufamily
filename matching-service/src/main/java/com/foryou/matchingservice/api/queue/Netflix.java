package com.foryou.matchingservice.api.queue;

import com.foryou.matchingservice.api.dto.response.Response;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Qualifier("Netflix")
@Component
public class Netflix implements QueueService {
    private final Queue<Long> memberQueue = new LinkedList<>();
    private final Queue<Long> ownerQueue = new LinkedList<>();

    public void offerMember(Long no) {
        memberQueue.offer(no);
    }

    public void offerOwner(Long no) {
        ownerQueue.offer(no);
    }

    public Response pollQueues() {
        synchronized (this) {
            if (!ownerQueue.isEmpty() && !memberQueue.isEmpty())
                return new Response(memberQueue.poll(), ownerQueue.poll());
            return null;
        }
    }
}

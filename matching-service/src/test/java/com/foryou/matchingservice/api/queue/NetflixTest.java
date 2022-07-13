package com.foryou.matchingservice.api.queue;

import com.foryou.matchingservice.api.dto.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class NetflixTest {

    @InjectMocks
    private Netflix netflix;

    @Test
    @DisplayName("poll도중에 poll이 실행될 수 없다")
    public void notPollWhenPoll() throws Exception {
        // given
        Response[] response1 = new Response[1];
        Response[] response2 = new Response[1];

        new Thread(() -> {
            netflix.offerOwner(1L);
            netflix.offerMember(1L);
            netflix.offerOwner(2L);
            netflix.offerMember(2L);
        }).start();

        // when
        Thread t1 = new Thread(() -> {
            response1[0] = netflix.pollQueues();
        });
        Thread t2 = new Thread(() -> {
            response2[0] = netflix.pollQueues();
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // then
        assertEquals(1L, response1[0].getMember());
        assertEquals(1L, response1[0].getOwner());
        assertEquals(2L, response2[0].getOwner());
        assertEquals(2L, response2[0].getOwner());
        assertNull(netflix.pollQueues());
    }

    @Test
    @DisplayName("poll 하는 도중엔 offer가 자유롭게 이뤄져야한다")
    public void canOfferWhenPoll() throws Exception {
        // given
        Thread t1 = new Thread(() -> {
            for (Long i = 0L; i < 100; i++) {
                netflix.offerOwner(i);
                netflix.offerMember(i);
            }
        });

        // when
        Thread t2 = new Thread(() -> {
            for (Long i = 0L; i < 100; i++) {
                netflix.pollQueues();
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        // then
        assertNull(netflix.pollQueues());
    }
}
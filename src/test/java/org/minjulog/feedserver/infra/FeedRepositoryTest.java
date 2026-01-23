package org.minjulog.feedserver.infra;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.minjulog.feedserver.domain.model.Feed;
import org.minjulog.feedserver.domain.repository.FeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FeedRepositoryTest {
    @Autowired
    FeedRepository feedRepository;

    @Test
    void 좋아요_동시_100개_요청의_결과는_100개이다() throws Exception {
        //given

        Feed feed = feedRepository.save(new Feed(1L, "content"));
        Long feedId = feed.getId();

        int threads = 100;
        ExecutorService pool = Executors.newFixedThreadPool(20);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                    feedRepository.incrementLike(feedId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        endGate.await();
        pool.shutdown();

        //then
        Feed reloaded = feedRepository.findById(feedId).orElseThrow();
        assertThat(reloaded.getLikeCount()).isEqualTo(100);
    }

    @Test
    void 좋아요_동시_100개_요청은_종종_100이_안된다_LostUpdate() throws Exception {
        // given
        Feed feed = feedRepository.save(new Feed(1L, "content"));
        Long feedId = feed.getId();

        int threads = 100;
        ExecutorService pool = Executors.newFixedThreadPool(20);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGate.await();

                    // ❌ 문제 패턴: read -> +1 -> save (Lost Update)
                    Feed f = feedRepository.findById(feedId).orElseThrow();
                    f.like(); // likeCount++
                    feedRepository.save(f); // 마지막 커밋이 덮어씀

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        endGate.await();
        pool.shutdown();

        // then
        Feed reloaded = feedRepository.findById(feedId).orElseThrow();
        System.out.println("likeCount = " + reloaded.getLikeCount());

        // 보통 100이 안 나오는 게 정상(환경에 따라 재현성은 달라질 수 있음)
        assertThat(reloaded.getLikeCount()).isNotEqualTo(100);
    }
}
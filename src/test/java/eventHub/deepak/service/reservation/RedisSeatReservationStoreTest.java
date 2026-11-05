package eventHub.deepak.service.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked","rawtypes"})
class RedisSeatReservationStoreTest {
    @Test void usesRedisSetIfAbsentWithTtlAndRequiresOwnerSpecificMetadataToRelease() {
        StringRedisTemplate redis=mock(StringRedisTemplate.class);ValueOperations<String,String> values=mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);when(values.setIfAbsent(anyString(),anyString(),any(Duration.class))).thenReturn(true);
        RedisSeatReservationStore store=new RedisSeatReservationStore(redis);
        var grant=store.tryReserve(3L,11L,List.of(8L),Duration.ofMinutes(5)).orElseThrow();
        org.mockito.ArgumentCaptor<Duration> ttl=org.mockito.ArgumentCaptor.forClass(Duration.class);
        verify(values).setIfAbsent(eq("eventhub:seat-lock:event:3:seat:8"),eq("11:"+grant.token()),ttl.capture());
        assertTrue(ttl.getValue().compareTo(Duration.ofMinutes(5))<=0&&ttl.getValue().isPositive());

        assertFalse(store.release(3L,22L,grant.token()));
        verify(redis,never()).execute(any(DefaultRedisScript.class),anyList(),anyString());
        when(values.get("eventhub:reservation:event:3:customer:11:token:"+grant.token()))
                .thenReturn(grant.expiresAt().toEpochMilli()+"|8");
        when(redis.execute(any(DefaultRedisScript.class),anyList(),anyString())).thenReturn(1L);
        assertTrue(store.release(3L,11L,grant.token()));
        verify(redis).execute(any(DefaultRedisScript.class),eq(List.of("eventhub:seat-lock:event:3:seat:8")),eq("11:"+grant.token()));
    }
}

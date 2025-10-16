package com.ziwan.ziwanpicturebackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisCrudTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testAddAndGetValue() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // ====== 新增 / 修改 ======
        ops.set("user:1001", "Alice");

        // ====== 查询 ======
        String value = ops.get("user:1001");
        System.out.println("查询结果: " + value);

        assertThat(value).isEqualTo("Alice");
    }

    @Test
    void testUpdateValue() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // 先设置
        ops.set("user:1002", "Bob");

        // 更新
        ops.set("user:1002", "Bob-updated");

        String value = ops.get("user:1002");
        System.out.println("更新结果: " + value);

        assertThat(value).isEqualTo("Bob-updated");
    }

    @Test
    void testDeleteValue() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

//        ops.set("user:1003", "Charlie");

        // 删除
        Boolean deleted = stringRedisTemplate.delete("user:1001");
        System.out.println("删除结果: " + deleted);

        String value = ops.get("user:1001");
        System.out.println("删除后查询: " + value);

        assertThat(value).isNull();
    }
}

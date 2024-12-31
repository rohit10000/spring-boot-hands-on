package com.example.springbootapplication.repository;

import com.example.springbootapplication.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class BooksRepository {

    @Autowired
    private @Qualifier("customRedisTemplate") RedisTemplate<Object, Object> redisTemplate;

    public void save(Book book) {
        redisTemplate.opsForValue()
            .set(book.getId(), book);
    }

    public Book findById(Long id) {
        return (Book) redisTemplate.opsForValue()
            .get(id);
    }

}

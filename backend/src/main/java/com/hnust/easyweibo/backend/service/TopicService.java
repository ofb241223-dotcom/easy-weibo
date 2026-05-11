package com.hnust.easyweibo.backend.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hnust.easyweibo.backend.domain.dto.topic.TopicResponse;
import com.hnust.easyweibo.backend.mapper.PostMapper;

@Service
public class TopicService {

    private final PostMapper postMapper;

    public TopicService(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public List<TopicResponse> getTrendingTopics(int limit) {
        Map<String, Integer> counts = new LinkedHashMap<>();

        postMapper.findAll().forEach(post -> {
            if (post.getContent() == null || post.getContent().isBlank()) {
                return;
            }

            for (String part : post.getContent().split("\\s+")) {
                if (!part.startsWith("#") || part.length() < 2) {
                    continue;
                }

                String tag = part.substring(1).replaceAll("[^\\p{L}\\p{N}_-]", "");
                if (tag.isBlank()) {
                    continue;
                }

                counts.merge(tag, 1, Integer::sum);
            }
        });

        return counts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry::getKey))
            .limit(Math.max(limit, 1))
            .map(entry -> new TopicResponse("topic-" + entry.getKey(), entry.getKey(), entry.getValue()))
            .toList();
    }
}

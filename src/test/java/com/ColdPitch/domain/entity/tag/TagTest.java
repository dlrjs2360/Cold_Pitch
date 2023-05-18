package com.ColdPitch.domain.entity.tag;

import com.ColdPitch.domain.entity.Tag;
import com.ColdPitch.domain.repository.TagRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TagTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    public void 태그_객체_생성_테스트() {
        // given
        Tag tag = Tag.builder()
                .name(TagName.LOGISTICS)
                .registrationDate(new Date())
                .lastUpdatedDate(new Date())
                .iconPath("icon/path")
                .description("logistics tag 는 물류와 관련된 스타트업을 나타내는 태그입니다.")
                .build();

        // when
        tagRepository.save(tag);

        // then
        Tag savedTag = tagRepository.findById(tag.getTagId()).get();
        assertThat(savedTag.getName()).isEqualTo(TagName.LOGISTICS);
    }

    @Test
    public void 모든_태그_조회_테스트() {
        // given
        for (TagName tagName : TagName.values()) {
            Tag tag = Tag.builder()
                    .name(tagName)
                    .registrationDate(new Date())
                    .lastUpdatedDate(new Date())
                    .iconPath("icon/path")
                    .description( tagName + " 를 가지는 스타트업입니다. ")
                    .build();
            tagRepository.save(tag);
        }

        // when
        List<Tag> allTags = tagRepository.findAll();

        // then
        assertThat(allTags).hasSize(TagName.values().length);
    }
}

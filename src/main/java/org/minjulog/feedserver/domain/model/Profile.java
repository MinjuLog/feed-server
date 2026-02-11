package org.minjulog.feedserver.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "profile")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    private static final List<String> ADJECTIVES = List.of(
            "졸린",
            "용감한",
            "행복한",
            "조용한",
            "수줍은",
            "똑똑한",
            "느긋한",
            "날쌘",
            "친절한",
            "엉뚱한"
    );

    private static final List<String> ANIMALS = List.of(
            "고양이",
            "강아지",
            "호랑이",
            "여우",
            "곰",
            "토끼",
            "수달",
            "부엉이",
            "사자",
            "펭귄"
    );

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long profileId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String username;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reaction> reactions = new ArrayList<>();

    public Profile(long userId) {
        this.userId = userId;
        this.username = randomName();
    }

    private String randomName() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        return adjective + " " + animal;
    }
}

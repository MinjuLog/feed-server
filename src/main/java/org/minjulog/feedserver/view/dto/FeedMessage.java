package org.minjulog.feedserver.view.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedMessage {
    private Long authorId;
    private String content;
}

package org.minjulog.feedserver.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.attachment.AttachmentService;
import org.minjulog.feedserver.presentation.request.*;
import org.minjulog.feedserver.presentation.response.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StaticResourceController {

    private final AttachmentService attachmentService;

    @GetMapping("/api/pre-signed-url")
    public AttachmentResponse.IssuePreSignedUrl sendPreSignedUrl(@ModelAttribute AttachmentRequest.IssuePreSignedUrl req) throws Exception {
        return attachmentService.issuePreSignedUrl(req);
    }
}

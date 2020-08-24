package com.youtube.channel.auto.comment.replier.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.youtube.channel.auto.comment.replier.service.YTubeAutoCommentReplierService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class YtubeAutoCommentReplierController {

	@Autowired
	private final YTubeAutoCommentReplierService yTubeAutoCommentReplierService;

	public YtubeAutoCommentReplierController(YTubeAutoCommentReplierService yTubeAutoCommentReplierService) {
		this.yTubeAutoCommentReplierService = yTubeAutoCommentReplierService;
	}

	@GetMapping
	@RequestMapping(value = "/ytube/comments/autoreply/")
	public ResponseEntity<?> performAutoReplyCommentForYtVideo(
			@RequestParam(required = true, name = "videourl") String videoUrl) {
		log.info("Entered YtubeAutoCommentReplierController::performAutoReplyCommentForYtVideo()");
		try {
			log.info("Video url:{}" + videoUrl);
			yTubeAutoCommentReplierService.performAutoCommentReply(videoUrl);
		} catch (Exception e) {
			log.error("Unable to add auto reply comment for youtube video." + e.getLocalizedMessage());
		}

		log.info("Exited YtubeAutoCommentReplierController::performAutoReplyCommentForYtVideo()");
		return ResponseEntity.ok().build();

	}
}

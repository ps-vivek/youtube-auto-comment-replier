package com.mbrothers.channel.youtubecommentor.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Setter
@Getter
@EnableConfigurationProperties
@ConfigurationProperties("config")
public class YTubeAutoCommentReplierConfig {
	private List<String> autoReplyComments = new ArrayList<>();

	private long maxResultsPerPage;

	private String apiKey;
}

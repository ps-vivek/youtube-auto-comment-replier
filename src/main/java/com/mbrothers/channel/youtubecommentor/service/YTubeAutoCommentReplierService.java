package com.mbrothers.channel.youtubecommentor.service;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.mbrothers.channel.youtubecommentor.config.YTubeAutoCommentReplierConfig;
import com.mbrothers.channel.youtubecommentor.constants.YTubeAutoCommentReplierConstants;
import com.mbrothers.channel.youtubecommentor.util.YouTubeServiceUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class YTubeAutoCommentReplierService {

	@Autowired
	private YTubeAutoCommentReplierConfig yTubeAutoCommentReplierConfig;
	private YouTube youtubeServiceWithApiKey;
	private YouTube youtubeService;

	public void performAutoCommentReply(String videoUrl) throws IOException {
		this.youtubeServiceWithApiKey = YouTubeServiceUtil.initializeYTubeServiceForApiKey();
		this.youtubeService = YouTubeServiceUtil.initializeYTubeServiceForOauth();
		String videoId = fetchVideoIdFromYtubeUrl(videoUrl);
		String channelId = YouTubeServiceUtil.fetchYTubeVideoInfoFromUrl(youtubeService, videoId);
		log.info("Channel Id:{},Video Id:{} ", channelId, videoId);
		YouTube.CommentThreads.List viewCommentsRequest = youtubeServiceWithApiKey.commentThreads()
				.list("snippet,replies");
		CommentThreadListResponse viewCommentsResponse = YouTubeServiceUtil.fetchYTubeCommentThreadsBasedOnVideoId(
				videoId, viewCommentsRequest, yTubeAutoCommentReplierConfig.getMaxResultsPerPage(),
				yTubeAutoCommentReplierConfig.getApiKey());
		String nextPageToken = null;
		int count = 0;
		do {
			nextPageToken = viewCommentsResponse.getNextPageToken();
			log.info("Next page token:" + nextPageToken + ",Page:" + (++count));
			viewCommentsResponse.getItems().forEach(item -> {
				try {
					String authorChannelIdOfTopLevelComment = item.getSnippet().getTopLevelComment().getSnippet()
							.getAuthorChannelId().toString();
					authorChannelIdOfTopLevelComment = authorChannelIdOfTopLevelComment.substring(7,
							authorChannelIdOfTopLevelComment.length() - 1);
					AtomicBoolean hasAuthorCommented = hasChannelOwnerReplied(channelId, item,
							authorChannelIdOfTopLevelComment);
					log.info("Has author Replied?" + hasAuthorCommented.get());
					addReplyComments(youtubeService, videoId, item, hasAuthorCommented);
				} catch (Exception e) {
					log.info("No reply comments" + e.toString());
				}
			});
			viewCommentsResponse = YouTubeServiceUtil.fetchYTubeCommentThreadsBasedOnVideoIdAndNextPageToken(videoId,
					viewCommentsRequest, viewCommentsResponse, nextPageToken, yTubeAutoCommentReplierConfig);
		} while (nextPageToken != null);

	}

	private void addReplyComments(YouTube youtubeService, final String videoId, CommentThread item,
			AtomicBoolean hasAuthorCommented) throws IOException {
		if (!hasAuthorCommented.get()) {
			log.info("Channel has received following comment:"
					+ item.getSnippet().getTopLevelComment().getSnippet().getTextOriginal());
			YouTubeServiceUtil.addYTubeComment(youtubeService, prepareComment(item, videoId));
		}
	}

	private Comment prepareComment(CommentThread item, final String videoId) {
		CommentSnippet snippet = new CommentSnippet();
		snippet.setParentId(item.getSnippet().getTopLevelComment().getId());
		String reply = yTubeAutoCommentReplierConfig.getAutoReplyComments()
				.get(new Random().nextInt(yTubeAutoCommentReplierConfig.getAutoReplyComments().size()));
		snippet.setTextDisplay(reply);
		snippet.setTextOriginal(reply);
		snippet.setVideoId(videoId);
		log.info("Added reply comment:" + snippet.getTextOriginal());
		return new Comment().setSnippet(snippet);
	}

	private AtomicBoolean hasChannelOwnerReplied(String channelId, CommentThread item,
			String authorChannelIdOfTopLevelComment) {
		AtomicBoolean hasAuthorCommented = new AtomicBoolean(false);
		if (authorChannelIdOfTopLevelComment.equals(channelId)) {
			hasAuthorCommented.set(true);
			log.info("Channel owner has added a parent comment:"
					+ item.getSnippet().getTopLevelComment().getSnippet().getTextOriginal());
			log.info("Not sending reply to channel owner:" + authorChannelIdOfTopLevelComment + "comment");
		} else if (item.getReplies() != null) {
			for (Comment replyComment : item.getReplies().getComments()) {
				String channelIdInResp = replyComment.getSnippet().getAuthorChannelId().toString();
				channelIdInResp = channelIdInResp.substring(7, channelIdInResp.length() - 1);
				if (channelIdInResp.equalsIgnoreCase(channelId)) {
					log.debug("Channelid in resp:" + channelIdInResp);
					log.debug("Channel owner's existing reply comment:" + replyComment.getSnippet().getTextOriginal());
					log.info("Channel owner: " + channelIdInResp + "has already replied. No need to reply again.");
					hasAuthorCommented.set(true);
				}
			}
		}
		return hasAuthorCommented;
	}

	private String fetchVideoIdFromYtubeUrl(String videoUrl) {
		Pattern pattern = Pattern.compile(YTubeAutoCommentReplierConstants.YOUTUBE_URL_REGEX_IDENTIFIER);
		Matcher matcher = pattern.matcher(videoUrl);
		String videoId = "";
		while (matcher.find()) {
			videoId = matcher.group(1);
		}
		return videoId;
	}

}

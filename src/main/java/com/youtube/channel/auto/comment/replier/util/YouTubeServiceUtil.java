package com.youtube.channel.auto.comment.replier.util;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import com.youtube.channel.auto.comment.replier.config.YTubeAutoCommentReplierConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YouTubeServiceUtil {

	public static YouTube initializeYTubeServiceForOauth() {
		YouTube youtube = null;
		try {
			youtube = GoogleOauthClientUtil.getService();
		} catch (GeneralSecurityException | IOException e) {
			log.error("Unable to intitialize ytube service for oauth" + e.getLocalizedMessage());
		}
		return youtube;
	}

	public static YouTube initializeYTubeServiceForApiKey() {
		YouTube youtube = null;
		try {
			youtube = GoogleOauthClientUtil.getServiceWithDevKey();
		} catch (GeneralSecurityException | IOException e) {
			log.error("Unable to intitialize ytube service for api key" + e.getLocalizedMessage());
		}
		return youtube;

	}

	public static CommentThreadListResponse fetchYTubeCommentThreadsBasedOnVideoId(final String videoId,
			YouTube.CommentThreads.List viewCommentsRequest, long maxResultsPerPage, String developerKey)
			throws IOException {
		CommentThreadListResponse viewCommentsResponse = viewCommentsRequest.setKey(developerKey).setVideoId(videoId)
				.setMaxResults(maxResultsPerPage).execute();
		log.debug(viewCommentsResponse.toPrettyString());
		return viewCommentsResponse;
	}

	public static CommentThreadListResponse fetchYTubeCommentThreadsBasedOnVideoIdAndNextPageToken(final String videoId,
			YouTube.CommentThreads.List viewCommentsRequest, CommentThreadListResponse viewCommentsResponse,
			String nextPageToken, YTubeAutoCommentReplierConfig yTubeAutoCommentReplierConfig) throws IOException {
		if (nextPageToken != null) {
			viewCommentsResponse = viewCommentsRequest.setKey(yTubeAutoCommentReplierConfig.getApiKey())
					.setVideoId(videoId).setMaxResults(yTubeAutoCommentReplierConfig.getMaxResultsPerPage())
					.setPageToken(nextPageToken).execute();
		}
		return viewCommentsResponse;
	}

	public static void addYTubeComment(YouTube youtubeService, Comment comment) {
		Comment execute;
		try {
			execute = youtubeService.comments().insert("snippet", comment).execute();
			log.debug(execute.toPrettyString());
		} catch (IOException e) {
			log.error("Error in updating youtube comment:" + e.getLocalizedMessage());
		}

	}

	public static String fetchYTubeVideoInfoFromUrl(YouTube youtubeService, final String videoId) throws IOException {
		YouTube.Videos.List request = youtubeService.videos().list("snippet");
		VideoListResponse response = request.setId(videoId).execute();
		return response.getItems().get(0).getSnippet().getChannelId();
	}

}

Imagine that you are owning a youtube channel where every video of yours is getting viral.
People are flooding you with 100's of comments. Lets assume that you decide to reply for every comment by thanking every one.
This might be a time taking process.

This app is used to automate this process. You can thank everyone automatically by picking a thank you message from a pre-defined set of messages.
All you need to do is configure the messages in application.yml under:
  autoReplyComments:
    - Thank you.
    - Thanks a lot.
    - Appreciate your feedback.
    - Thanks a lot for your support.
    - Thanks a ton
    - Namaste! Thanks a lot.
    - Thanks a lot for your comments.
    - Thanks a lot. Appreciate your feedback. All the feedback inspires us to work even harder.

Project Setup:
https://developers.google.com/youtube/v3/docs
This project uses google Youtube APIs. One needs to configure oauth and developer key for this project and add them.
Place the client_Secrets file under /src/main/resources path of this app and add the developer key to application.yml:
config:
  apiKey: --Generate your api key from google console

Once that is done, go ahead and run the below end point:
http://localhost:9595/ytube/comments/autoreply/?videourl=Enter_your_encoded_url_video_here

Assumptions:
It is assumed that your replying on behalf of the email id related to channel.
Reply comments will not be added in the following cases:
1)In case you(channel owner) have replied to a parent comment previously, app will sense that and won't add a reply comment.
2)Also, in case the parent comment is provided by you(channel owner), app will also not add any reply comment.
For all other parent comments, the app will add a reply comment from pre-defined set of messages.





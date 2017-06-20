package com.nilhcem.assistant.androidthings.pubsub;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;
import com.google.pubsub.v1.SubscriberGrpc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import io.grpc.ManagedChannel;
import io.grpc.auth.MoreCallCredentials;
import io.grpc.okhttp.NegotiationType;
import io.grpc.okhttp.OkHttpChannelBuilder;

public class EventLiveData extends LiveData<Action> {

    private static final String GOOGLE_PROJECT_ID = "your-google-project-id";

    private static final String TAG = EventLiveData.class.getSimpleName();
    private static final String SUBSCRIPTION_NAME = "projects/" + GOOGLE_PROJECT_ID + "/subscriptions/PubSubMessagesSub";
    private static final String CREDENTIALS_FILE = "service-account.json";

    private static final int PULL_DELAY_MS = 1000;
    private static final int HANDLER_MSG_PULL = 1;

    private final SubscriberGrpc.SubscriberBlockingStub subscriber;

    private Handler handler;
    private HandlerThread handlerThread;

    public EventLiveData(Context context) {
        GoogleCredentials credentials = createGoogleCredentials(context);

        ManagedChannel channel = OkHttpChannelBuilder
                .forAddress("pubsub.googleapis.com", 443)
                .negotiationType(NegotiationType.TLS)
                .build();

        subscriber = SubscriberGrpc
                .newBlockingStub(channel)
                .withCallCredentials(MoreCallCredentials.from(credentials));
    }

    @Override
    protected void onActive() {
        handlerThread = new HandlerThread("FrameThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == HANDLER_MSG_PULL) {
                    pullPubSub();
                    handler.sendEmptyMessageDelayed(HANDLER_MSG_PULL, PULL_DELAY_MS);
                } else {
                    super.handleMessage(msg);
                }
            }
        };
        handler.sendEmptyMessage(HANDLER_MSG_PULL);
    }

    @Override
    protected void onInactive() {
        handler.removeMessages(HANDLER_MSG_PULL);
        handlerThread.quitSafely();
        handler = null;
        handlerThread = null;
    }

    private GoogleCredentials createGoogleCredentials(Context context) {
        AssetManager am = context.getAssets();
        try {
            InputStream isCredentialsFile = am.open(CREDENTIALS_FILE);
            return GoogleCredentials
                    .fromStream(isCredentialsFile)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/pubsub"));
        } catch (IOException e) {
            throw new RuntimeException("Error getting Google credentials", e);
        }
    }

    private void pullPubSub() {
        PullResponse response = subscriber.pull(
                PullRequest.newBuilder()
                        .setSubscription(SUBSCRIPTION_NAME)
                        .setReturnImmediately(true)
                        .setMaxMessages(10)
                        .build()
        );

        for (ReceivedMessage received : response.getReceivedMessagesList()) {
            byte[] elementBytes = received.getMessage().getData().toByteArray();
            String actionName = new String(elementBytes).replaceAll("\"", "");
            Log.i(TAG, "Received message: " + actionName);

            try {
                postValue(Action.valueOf(actionName));
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Can't get action for name: " + actionName);
            } finally {
                acknowledgeRequest(received.getAckId());
            }
        }
    }

    private void acknowledgeRequest(String ackId) {
        subscriber.acknowledge(
                AcknowledgeRequest.newBuilder()
                        .setSubscription(SUBSCRIPTION_NAME)
                        .addAckIds(ackId)
                        .build()
        );
    }
}

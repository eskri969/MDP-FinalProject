package dte.masteriot.mdp.webloadasynctask;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

import static java.lang.Integer.parseInt;


public class MQTT_handler extends AppCompatActivity {

    MqttAndroidClient mqttAndroidClient;

    private final String serverURI = "tcp://mqtt.thingspeak.com:1883";
    private final String MQTTKEY = "7DEGZ7VUVSYT3NOR";
    private int clientId;
    private String subscriptionTopic;
    private  MQTTChannelObject channel;

    public MQTT_handler(int clientId) {
        this.clientId = clientId;
    }


    public int MQTT_handler_start(final MQTTChannelObject channel, Context context) {

        this.subscriptionTopic = "channels/" + channel.getId() + "/subscribe/fields/field1/" + channel.getReadKey();

        mqttAndroidClient = new MqttAndroidClient(context, serverURI, "client" + this.clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    Log.v("MQTTH", "Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic(subscriptionTopic);
                } else {
                    Log.v("MQTTH", "Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.v("MQTTH", "The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.v("MQTTH", "Incoming message for clientId"+clientId+": " + new String(message.getPayload()));
                channel.setLast_Entry(parseInt(new String(message.getPayload(),"UTF-8")));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        mqttConnectOptions.setUserName("user");
        mqttConnectOptions.setPassword(MQTTKEY.toCharArray());

        try {
            //Log.v("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic(subscriptionTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.v("MQTTH", "Failed to connect to: " + serverURI);
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
        return 0;

    }

    public void subscribeToTopic(String subscriptionTopic) {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.v("MQTTH", "Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.v("MQTTH", "Failed to subscribe");
                }
            });
        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

}

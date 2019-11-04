package dte.masteriot.mdp.emergencies;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTT_handler extends AppCompatActivity {

    MqttAndroidClient mqttAndroidClient;

    //   private final String serverURI = "tcp://mqtt.thingspeak.com:1883";
    //   private final String MQTTKEY = "7DEGZ7VUVSYT3NOR";
    String clientId;
    private String subscriptionTopic;
    MQTTChannelObject ch;
    MainActivity ma;

    public MQTT_handler(int clientIdN, MQTTChannelObject ch, MainActivity ma) {
        this.clientId = clientIdN+""+System.currentTimeMillis();
        this.ch = ch;
        this.ma= ma;
    }


    public int MQTT_handler_start(final MQTTChannelObject channel, Context context) {

        this.subscriptionTopic = "channels/" + channel.getId() + "/subscribe/fields/field1/" + channel.getReadKey();

        mqttAndroidClient = new MqttAndroidClient(context, ma.serverURI, "client" + this.clientId);
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
                //Log.v("MQTTH", "Incoming message for clientId"+clientId+": " + message.toString());
                //Integer aux = new Byte(message.getPayload()[0]).intValue();
                //Integer aux1= fromByteArray(message.getPayload());
                //Integer aux2 = Integer.parseInt(new    String (message.getPayload()));
                //Integer aux2 = byteArrayToInt(message.getPayload());

                String str = new String (message.getPayload());
                String str2=str.substring(str.length()-1, str.length());
                Integer aux2;
                if(str2.equals("\n")) {
                    aux2 = Integer.parseInt(str.substring(0, str.length() - 1));
                }else{
                    aux2 = Integer.parseInt(str);

                }
                if(ch.alert && aux2<100){
                    ma.nEmergencies--;
                    ch.alert=false;
                }else if(!ch.alert && aux2>100){
                    ma.nEmergencies++;
                    ch.alert=true;
                }
                ch.setLast_Entry(aux2);

            }



            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        mqttConnectOptions.setUserName("user");
        mqttConnectOptions.setPassword(ma.MQTTKEY.toCharArray());

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
                    Log.v("MQTTH", "Failed to connect to: " + ma.serverURI);
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
    public void disconnect() {
        try {
            mqttAndroidClient.unregisterResources();
            mqttAndroidClient.close();
            mqttAndroidClient.disconnect(5);
            mqttAndroidClient.setCallback(null);
            mqttAndroidClient = null;
            Thread.sleep(200);
        }catch (MqttException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public void unsubscribeToTopic(String subscriptionTopic) {
        try {
            IMqttToken unsubToken = mqttAndroidClient.unsubscribe(subscriptionTopic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.v("MQTTH", "UNSubscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Log.v("MQTTH", "Failed to UNsubscribe");
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
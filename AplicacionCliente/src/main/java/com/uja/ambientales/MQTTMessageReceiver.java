/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.uja.ambientales;

import java.util.ArrayList;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author iesdi
 */
public class MQTTMessageReceiver implements MqttCallback {

    private final Interfaz ui;
    private MqttClient mqttClient;
    private MqttConnectOptions connOpts;
    private final String broker;
    private final String clientId;
    private String mapTopic;
    private boolean recibido1;
    private String calculationsTopic;
    private boolean recibido2;
    private final ArrayList<Grid> grids;
    private final ArrayList<String> map;
    private final String odometryTopic;

    public MQTTMessageReceiver(Interfaz ui) {
        this.ui = ui;
        this.broker = "tcp://192.168.0.100:1883";
        this.clientId = "Ismael";
        this.recibido1 = false;
        this.recibido2 = false;
        this.grids = new ArrayList();
        this.map = new ArrayList();
        this.mapTopic = "/map";
        this.calculationsTopic = "/Grupo G/calculations";
        this.odometryTopic = "/Grupo G/odometry";
        try {
            connect();
            mqttClient.setCallback(this);
            mqttClient.subscribe(mapTopic);
            mqttClient.subscribe(calculationsTopic);
            mqttClient.subscribe(odometryTopic);
            System.out.println("Suscrito al tema: " + mapTopic);
            System.out.println("Suscrito al tema: " + calculationsTopic);
            System.out.println("Suscrito al tema: " + odometryTopic);
        } catch (MqttException ex) {
        }
    }

    private void connect() throws MqttException {
        mqttClient = new MqttClient(broker, clientId);
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        System.out.println("Conectándose al broker: " + broker);
        mqttClient.connect(connOpts);
        System.out.println("Conexión exitosa");
    }

    public void sendMessage(String message, String topic) throws MqttException {
        if (!mqttClient.isConnected()) {
            connect();
        }
        System.out.println("Enviando mensaje: " + message);
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttClient.publish(topic, mqttMessage);
        System.out.println("Mensaje enviado");
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Connection lost!");
        try {
            connect();
            mqttClient.setCallback(this);
            mqttClient.subscribe(mapTopic);
            mqttClient.subscribe(calculationsTopic);
            mqttClient.subscribe(odometryTopic);
        } catch (MqttException ex) {
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        if (topic.equals(mapTopic)) {
            String newMap = new String(mqttMessage.getPayload());
            System.out.println("Received map: " + newMap);
            processMap(newMap);
            mqttClient.unsubscribe(mapTopic);
            recibido1 = true;
            if (recibido2) {
                ui.setData(map, grids);
            }
        } else {
            if (topic.equals(calculationsTopic)) {
                String calculations = new String(mqttMessage.getPayload());
                System.out.println("Calculations: " + calculations);
                processCalculations(calculations);
                mqttClient.unsubscribe(calculationsTopic);
                recibido2 = true;
                if (recibido1) {
                    ui.setData(map, grids);
                }
            } else {
                String odometry = new String(mqttMessage.getPayload());
                System.out.println("Odometry: " + odometry);
                processOdometry(odometry);
            }
        }
    }

    private void processOdometry(String odometry) {
        String[] split = odometry.split(";");
        int x = Integer.parseInt(split[0].substring(0, 1));
        int y = Integer.parseInt(split[0].substring(1, 2));
        float angle = Float.parseFloat(split[1]);
        ui.moveRobot(x, y, angle);
    }

    private void processCalculations(String calculations) {
        for (int i = 0; i < calculations.length(); i += 2) {
            int x = Integer.parseInt(calculations.substring(i, i + 1));
            int y = Integer.parseInt(calculations.substring(i + 1, i + 2));
            grids.add(new Grid(x, y));
        }
    }

    private void processMap(String newMap) {
        for (int i = 0; i < newMap.length(); i += 2) {
            String substring = newMap.substring(i, i + 2);
            map.add(substring);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

}

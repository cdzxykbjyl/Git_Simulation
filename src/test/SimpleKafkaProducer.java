package test;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;

import java.util.Properties;

/**
 * @description:生产者
 * @author: LiPin
 * @time: 2021-12-29 21:54
 */

public class SimpleKafkaProducer {
    public KafkaProducer<String, String> producer;
    private String ip;
    /**
     *
     * @param ip
     */
    public SimpleKafkaProducer(String ip){
        this.ip=ip;
        Properties props = new Properties();
        props.put("bootstrap.servers", ip);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //设置分区类,根据key进行数据分区
        producer = new KafkaProducer<String, String>(props);
    }

    /**
     *
     * @param topic
     * @param data
     */
    public void send(String topic ,String data){
            producer.send(new ProducerRecord<String, String>( topic,data));
    }
    public static void main(String[] args){

        SimpleKafkaProducer simpleKafkaProducer=new SimpleKafkaProducer("114.67.252.43:39092");

        for (int i = 30;i<400;i++){
            String key = String.valueOf(i);
            String data ="mes:"+ key;
            simpleKafkaProducer.send("flight",data);
            System.out.println(data+"\n");
            try {
                Thread.sleep(1*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

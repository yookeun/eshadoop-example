package com.example;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


/**
 * 항공기 데이터를 추출해서 Mapper 데이터로 만든다.
 * @author ykkim
 *
 */
public class DelayCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    //맵출력값 
    private final static IntWritable outputValue = new IntWritable(1);
    //맵출력키
    private Text outputKey = new Text();
    @Override
    protected void map(LongWritable key, Text value,
            Mapper<LongWritable, Text, Text, IntWritable>.Context context)
            throws IOException, InterruptedException {
        
        AirlinePerformanceParser parser = new AirlinePerformanceParser(value);
        
        //출발 지연 데이터 출력
        if (parser.isDepartureDelayAvailable()) {
            if (parser.getDepartureDelayTime() > 0) {
                //출력키 설정
                outputKey.set("D," + parser.getYearMonth());
                //출력데이터 설정
                context.write(outputKey, outputValue);
            } else if (parser.getDepartureDelayTime() == 0) {
                context.getCounter(DelayCounters.SCHEDULED_DEPARTURE).increment(1);
            } else if (parser.getDepartureDelayTime() < 0) {
                context.getCounter(DelayCounters.EARLY_DEPARTURE).increment(1);
            }
        } else {
            context.getCounter(DelayCounters.NOT_AVAILABLE_DEPARTURE).increment(1);
        }
        
        //도착 지연 데이터 출력 
        if (parser.isArriveDelayAvailable()) {
            if (parser.getArriveDelayTime() > 0) {
                //출력키 설정
                outputKey.set("A," + parser.getYearMonth());
                //출력 데이터 설정
                context.write(outputKey, outputValue);
            } else if (parser.getArriveDelayTime() == 0) {
                context.getCounter(DelayCounters.SCHEDULED_ARRIVAL).increment(1);
            } else if (parser.getArriveDelayTime() < 0) {
                context.getCounter(DelayCounters.EARLY_ARRVIAL).increment(1);
            }
        } else {
            context.getCounter(DelayCounters.NOT_AVAILABLE_ARRIVAL).increment(1);
        }      
        
    }
    
    
    
}

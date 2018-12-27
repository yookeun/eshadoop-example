package com.example;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class DelayCountReducerToElastic extends Reducer<Text, IntWritable, Text, MapWritable> {
    //reduce 출력키
    private Text outputKey = new Text();    
    
    //엘라스틱에 JSON으로 전송하기 위해 출력값 타입은 MapWritable으로 한다.
    //redule 출력값
    private MapWritable result = new MapWritable();
    
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values,
            Reducer<Text, IntWritable, Text, MapWritable>.Context context)
            throws IOException, InterruptedException {
        //콤머 구분자 분리 
        String[] columns = key.toString().split(",");
        outputKey.set(columns[1]);
        
        //Departure 라면 
        if (columns[0].equals("D")) {
            int sum = 0;
            for (IntWritable value: values) {
                sum += value.get();
            }
            result.put(new Text("delayType"), new Text("departure"));
            result.put(new Text("yearMonth"), new Text(columns[1]));
            result.put(new Text("count"), new IntWritable(sum));         
        } else {
            int sum = 0;
            for (IntWritable value: values) {
                sum += value.get();
            }
            result.put(new Text("delayType"), new Text("arrival"));
            result.put(new Text("yearMonth"), new Text(columns[1]));
            result.put(new Text("count"), new IntWritable(sum));           
        }
        context.write(key, result);
    }
    
    
}

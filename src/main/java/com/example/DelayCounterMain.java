package com.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class DelayCounterMain extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        //Tool 인터페이스 실행 
        int res = ToolRunner.run(new Configuration(), new DelayCounterMain(), args);
        System.out.println("MR-Job Result : " + res);

    }
    
    @Override
    public int run(String[] args) throws Exception {
        String[] otherArgs = new GenericOptionsParser(getConf(), args).getRemainingArgs();
       
        //입출력 데이터 경로확인
        if (otherArgs.length != 2) {
            System.err.println("Usage: DelayCounterMain <in> <out>");
            System.exit(2);
        }
        
        //이미 있다면 삭제처리 해주자. 테스트를 위해서 여러번 실행하기 위해 
        FileSystem hdfs = FileSystem.get(getConf());
        if (hdfs.exists(new Path(otherArgs[1]))) {
            hdfs.delete(new Path(otherArgs[1]), true);
        }    
        
        Job job = Job.getInstance(getConf(),"DelayCounterMain");
        //입출력데이터 경로설정
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        
        //잡클래스 설정
        job.setJarByClass(DelayCounterMain.class);
        //매퍼 클래스 설정
        job.setMapperClass(DelayCountMapper.class);
        //리듀서 클래스 설정
        job.setReducerClass(DelayCountReducer.class);
        //입출력 데이터 포멧
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        
        //출력키 및 출력값 유형설정
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);               
        
        //MultipleOutput설정
        MultipleOutputs.addNamedOutput(job, "departure",TextOutputFormat.class, Text.class, IntWritable.class);
        MultipleOutputs.addNamedOutput(job, "arrival", TextOutputFormat.class, Text.class, IntWritable.class);
        return (job.waitForCompletion(true)? 0:1);
        
    }    

}

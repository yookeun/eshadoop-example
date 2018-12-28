package com.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.elasticsearch.hadoop.mr.EsOutputFormat;

public class DelayCounterElasticMain extends Configured implements Tool {
    
    public static void main(String[] args) throws Exception {
        // Start the WordCount MapReduce application
        int res = ToolRunner.run(new Configuration(), new DelayCounterElasticMain(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        String[] otherArgs = new GenericOptionsParser(getConf(), args).getRemainingArgs();

        if (otherArgs.length != 1) {
            System.err.println("Usage :  DelayCounterElasticMain <input>");
            System.exit(2);
        }

        // Configuration conf = new Configuration();
        getConf().setBoolean("mapreduce.map.speculative", false);
        getConf().setBoolean("mapreduce.reduce.speculative", false);
        getConf().set("es.nodes", "localhost:9200");
        getConf().set("es.resource", "eshadoop_delay/_doc");
        getConf().set("es.batch.size.entries", "1");

        Job job = Job.getInstance(getConf(), "DelayCounterElasticMain");
        // 입출력 데이터 경로 설정
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

        // 잡클래스설정
        job.setJarByClass(DelayCounterElasticMain.class);

        //컨바이너적용
        job.setCombinerClass(DelayCountReducerToElastic.class);   
        
        // 맵클래스설정(기존 그대로)
        job.setMapperClass(DelayCountMapper.class);

        // 리듀서클래스설정(엘라스틱에 맞게 수정)
        job.setReducerClass(DelayCountReducerToElastic.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        // output format
        job.setOutputFormatClass(EsOutputFormat.class);
        
        return (job.waitForCompletion(true) ? 0 : 1);   
    }
}

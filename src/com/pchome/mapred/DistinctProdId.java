package com.pchome.mapred;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.pchome.data.RakutenProdInfo;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * 濾重複
 * Created by Joan on 2017/7/20.
 */
public class DistinctProdId {
    public static class DistinctProdIdHdfsMapper extends Mapper<LongWritable, Text, Text, NullWritable> {
        NullWritable nullWritable = NullWritable.get();

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            context.write(value, nullWritable);
        }
    }

    public static class DistinctProdIdApiMapper extends Mapper<LongWritable, Text, Text, NullWritable> {
        NullWritable nullWritable = NullWritable.get();
        Text output_key = new Text();

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String json = value.toString();
            boolean isJsonArray = json.startsWith("[");
            ObjectMapper mapper = new ObjectMapper();
            if (isJsonArray) {
                RakutenProdInfo[] rakutenProdInfoList = mapper.readValue(json, TypeFactory.defaultInstance().constructArrayType(RakutenProdInfo.class));
                for (RakutenProdInfo rakutenProdInfo : rakutenProdInfoList) {
                    output_key.set(rakutenProdInfo.getItemCode());
                    context.write(output_key, nullWritable);
                }
            } else {
                RakutenProdInfo rakutenProdInfo = mapper.readValue(json, RakutenProdInfo.class);
                output_key.set(rakutenProdInfo.getItemCode());
                context.write(output_key, nullWritable);
            }
        }
    }

    public static class DistinctProdIdReducer extends Reducer<Text, NullWritable, Text, NullWritable> {
        NullWritable nullWritable = NullWritable.get();

        public void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            context.write(key, nullWritable);
        }
    }
}

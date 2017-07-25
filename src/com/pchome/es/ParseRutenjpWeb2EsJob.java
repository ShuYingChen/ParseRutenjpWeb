package com.pchome.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.gson.*;

import com.pchome.data.ParseData;
import com.pchome.mapred.DistinctProdId;
import com.pchome.data.RakutenProdInfo;
import com.pchome.parser.WebContentParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.elasticsearch.hadoop.mr.EsOutputFormat;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 執行post 至 elastic search
 * Created by Edward on 2016/12/15.
 */
public class ParseRutenjpWeb2EsJob extends Configured implements Tool {
    //不重複的 item_code
    final String prodIdListFile = "/projects/kdn/platform/rakutenjp/v1/prodidlist.tsv";
    final String prodIdListFile_bk = "/projects/kdn/platform/rakutenjp/v1/bk";

    /**
     * hadoop jar /home/shuying/parseRukutenjpWeb.jar com.pchome.es.ParseRutenjpWeb2EsJob
     * -input "/projects/kdn/crawler/rakutenjp/v1/waiting"
     * -done "/projects/kdn/crawler/rakutenjp/v1/done"
     * -index "rutenjpdemo"
     * -type "rakutenjpweb"
     * -esNodes "192.168.100.161:9200,192.168.100.162:9200,192.168.100.163:9200"
     */
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new ParseRutenjpWeb2EsJob(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        String input = "/projects/kdn/crawler/rakutenjp/v1/waiting", done = "/projects/kdn/crawler/rakutenjp/v1/done", _index = "rutenjpdemo", _type = "rakutenjpweb";
        String es_nodes = "192.168.32.51:9200";
        for (int i = 0; i < args.length; i++) {
            if ("-help".equals(args[i])) {
                System.out.println("hadoop jar /home/shuying/parseRukutenjpWeb.jar com.pchome.es.ParseRutenjpWeb2EsJob \"/projects/kdn/crawler/rakutenjp/v1/waiting\" \"/projects/kdn/crawler/rakutenjp/v1/done\" \"rutenjpdemo\" \"rakutenjpweb\"");
                System.out.println("-input /projects/kdn/crawler/rakutenjp/v1/waiting");
                System.out.println("-done /projects/kdn/crawler/rakutenjp/v1/done");
                System.out.println("-index rutenjpdemo");
                System.out.println("-type rakutenjpweb");
                System.out.println("-esNodes 192.168.32.51:9200");
                System.exit(0);//正常結束
            } else if ("-input".equals(args[i])) {
                input = args[++i];
            } else if ("-index".equals(args[i])) {
                _index = args[++i];
            } else if ("-type".equals(args[i])) {
                _type = args[++i];
            } else if ("-esNodes".equals(args[i])) {
                es_nodes = args[++i];
            }
        }

        if (args.length < 1) {
            return 0;
        }
        // When implementing tool
        Configuration conf = this.getConf();
        conf.setBoolean("mapred.map.tasks.speculative.execution", false);
        conf.setBoolean("mapred.reduce.tasks.speculative.execution", false);
        //conf.set("es.nodes", "10.10.30.59:9200, 10.10.30.61:9200, 10.10.30.63:9200, 10.10.30.65:9200, 10.10.30.67:9200, 10.10.30.69:9200, 10.10.30.71:9200, 10.10.30.73:9200");
        //conf.set("es.nodes", "192.168.32.51:9200");//測試機
//        conf.set("es.nodes", "192.168.100.161:9200,192.168.100.162:9200,192.168.100.163:9200");//外網
        conf.set("es.nodes", es_nodes);
        conf.set("es.resource", _index + "/" + _type);
        conf.set("es.mapping.id", "ID"); //set _id = ID
        conf.set("es.input.json", "yes");
        conf.set("es.write.operation", "upsert");//default:index, {index, create, update, upsert}
        conf.set("es.batch.write.retry.count", "10");
        conf.set("es.net.http.auth.user", "engineer");
        conf.set("es.net.http.auth.pass", "engineer!16606102");
        conf.setBoolean("dfs.support.append", true);

        Job job = new Job(conf, ParseRutenjpWeb2EsJob.class.getSimpleName() + "_" + _type);
        job.setJarByClass(ParseRutenjpWeb2EsJob.class);
        job.setMapperClass(ParseWeb2EsJobMapper.class);

        job.setMapOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputFormatClass(EsOutputFormat.class);

        FileInputFormat.addInputPaths(job, input);
        boolean isCompletion = job.waitForCompletion(true);
        if (isCompletion) {
            // 執行成功->1. 產生 item_code 去重複
            // 兩個 input:1. 第一支 job 的 input ; 2. prodidlist.tsv
            String job2_output = "/projects/kdn/platform/rakutenjp/v1/output";
            Path job2_outputPath = new Path(job2_output);
            FileSystem fs = FileSystem.get(conf);
            fs.delete(job2_outputPath, true);  // output path 使用前先刪除

            Job job2 = new Job(conf, DistinctProdId.class.getSimpleName());
            job2.setJarByClass(DistinctProdId.class);
            job2.setReducerClass(DistinctProdId.DistinctProdIdReducer.class);
            job2.setMapOutputKeyClass(Text.class);
            job2.setMapOutputValueClass(NullWritable.class);
            job2.setOutputKeyClass(Text.class);
            job2.setOutputValueClass(NullWritable.class);
            job2.setNumReduceTasks(1);

            MultipleInputs.addInputPath(job2, new Path(prodIdListFile), TextInputFormat.class, DistinctProdId.DistinctProdIdHdfsMapper.class);
            Path waitingPath = new Path(input);
            if (fs.isDirectory(waitingPath)) {
                FileStatus[] srcFiles = fs.listStatus(waitingPath);
                String filePath;
                for (int i = 0; i < srcFiles.length; i++) {
                    filePath = input + File.separator + srcFiles[i].getPath().getName();
                    MultipleInputs.addInputPath(job2, new Path(filePath), TextInputFormat.class, DistinctProdId.DistinctProdIdApiMapper.class);
                }
            } else {
                MultipleInputs.addInputPath(job2, waitingPath, TextInputFormat.class, DistinctProdId.DistinctProdIdApiMapper.class);
            }

            FileOutputFormat.setOutputPath(job2, job2_outputPath);
            isCompletion = job2.waitForCompletion(true);
            if (isCompletion) {
                // 執行成功->2. copy waiting 底下的檔案, 搬至解析當天目錄下
                DecimalFormat df = new DecimalFormat("##00");
                Calendar calendar = Calendar.getInstance();
                String year = df.format(calendar.get(Calendar.YEAR));
                String month = df.format(calendar.get(Calendar.MONTH) + 1);
                String day = df.format(calendar.get(Calendar.DAY_OF_MONTH));

                String doneString = done + File.separator + year + File.separator + month + File.separator + day;
                Path donePath = new Path(doneString);
                if (!fs.exists(donePath)) {
                    //2-1. 檢查備份目錄是否存在
                    fs.mkdirs(donePath);
                }
                //2-2. 把做完的檔案搬至 done
                if (fs.isDirectory(waitingPath)) {
                    //2-2-1. 如果是目錄
                    FileStatus[] srcFiles = fs.listStatus(waitingPath);
                    for (FileStatus srcFile : srcFiles) {
                        String src = input + File.separator + srcFile.getPath().getName();
                        String target = doneString + File.separator + srcFile.getPath().getName();
                        fs.rename(new Path(src), new Path(target));
                    }
                } else {
                    String target = doneString + input.substring(input.lastIndexOf(File.separator));
                    fs.rename(waitingPath, new Path(target));
                }

                //執行成功->3. 備份 prodidlist.tsv
                if (!fs.exists(new Path(prodIdListFile_bk))) {
                    //3-1. 目錄是否存在
                    fs.mkdirs(new Path(prodIdListFile_bk));
                }
                String bkProdidlistFileString = prodIdListFile_bk + File.separator + "prodidlist.tsv";
                Path bkProdidlistFilePath = new Path(bkProdidlistFileString);
                if (fs.exists(bkProdidlistFilePath)) {
                    //3-2. 備份目錄底下,檔案是否存在
                    fs.delete(bkProdidlistFilePath, true);
                }
                //3-3. 把 prodidlist.tsv 搬至 備份目錄
                fs.rename(new Path(prodIdListFile), bkProdidlistFilePath);
                //3-4. 把結果檔更換至正確目錄
                fs.rename(new Path(job2_output + File.separator + "part-r-00000"), new Path(prodIdListFile));
            }
        }
        return isCompletion ? 0 : 1;
    }

    public static class ParseWeb2EsJobMapper extends Mapper<LongWritable, Text, NullWritable, Text> {
        Gson gson = new Gson();
        NullWritable nullWritable = NullWritable.get();
        Text output_value = new Text();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        WebContentParser webContentParser = new WebContentParser();

        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String json = value.toString();//input
            boolean isJsonArray = json.startsWith("[");
            ObjectMapper mapper = new ObjectMapper();
            if (isJsonArray) {
                RakutenProdInfo[] rakutenProdInfoList = mapper.readValue(json, TypeFactory.defaultInstance().constructArrayType(RakutenProdInfo.class));
                for (RakutenProdInfo rakutenProdInfo : rakutenProdInfoList) {
                    parseEsDoc2Write(context, rakutenProdInfo);
                }
            } else {
                RakutenProdInfo rakutenProdInfo = mapper.readValue(json, RakutenProdInfo.class);
                parseEsDoc2Write(context, rakutenProdInfo);
            }
        }

        private String getEsPostData(String id, String parsedContent) throws UnsupportedEncodingException {
            //每筆資料要做的事情
            String create_time = df.format(new Date());
            Map<String, Object> doc = new HashMap<String, Object>();
            doc.put("ID", id);//露天品編
            doc.put("content", parsedContent);    //結構化的網頁結果
            doc.put("create_time", create_time);
            doc.put("update_time", create_time);
            return gson.toJson(doc);
        }

        private void parseEsDoc2Write(Context context, RakutenProdInfo rakutenProdInfo) throws IOException, InterruptedException {
            //解析頁面
            ParseData parseData = webContentParser.parse(rakutenProdInfo.getAwsDtm(), rakutenProdInfo.getContent(), Charset.forName("EUC-JP"));
            if (parseData != null && parseData.getId() != null) {
                output_value.set(getEsPostData(parseData.getId(), parseData.getResult())); //itemCode,content
                context.write(nullWritable, output_value);
            } else {
                System.err.println("ParseRutenjpWeb2EsJob => item_code: " + rakutenProdInfo.getItemCode());
                System.err.println("ParseRutenjpWeb2EsJob => content: " + rakutenProdInfo.getContent());
                System.err.println("ParseRutenjpWeb2EsJob => Url: " + rakutenProdInfo.getUrl());
                System.err.println("************************");
            }
        }
    }
}

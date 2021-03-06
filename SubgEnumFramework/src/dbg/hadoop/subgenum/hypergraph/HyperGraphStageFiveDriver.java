package dbg.hadoop.subgenum.hypergraph;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;  
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;  
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
//import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import com.hadoop.compression.lzo.LzoCodec;

import dbg.hadoop.subgraphs.io.HyperVertexSign;
import dbg.hadoop.subgraphs.io.HyperVertexSignComparator;
import dbg.hadoop.subgraphs.io.HyperVertexSignGroupComparator;

public class HyperGraphStageFiveDriver extends Configured implements Tool{

	public int run(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
		Configuration conf = getConf();
		// The parameters: <stageFourOutputDir> <stageThreeOutputDir> <outputDir> <numReducers> <jarFile>
		int numReducers = Integer.parseInt(args[3]);
		
		String stageFourOutput = args[0];
		String stageThreeOutput = args[1];
		conf.setBoolean("mapred.compress.map.output", true);
		conf.set("mapred.map.output.compression.codec", "com.hadoop.compression.lzo.LzoCodec");
		
		Job job = new Job(conf, "HyperGraphStageFive");
		((JobConf)job.getConfiguration()).setJar(args[4]);
		//JobConf job = new JobConf(getConf(), this.getClass());
		
		//job.setMapperClass(HyperGraphStageTwoMapper.class);
		job.setReducerClass(HyperGraphStageFiveReducer.class);
		
		job.setMapOutputKeyClass(HyperVertexSign.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(LongWritable.class);
		
		job.setSortComparatorClass(HyperVertexSignComparator.class);
		job.setGroupingComparatorClass(HyperVertexSignGroupComparator.class);

		job.setInputFormatClass(SequenceFileInputFormat.class);
		//if(enableOutputCompress){
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		SequenceFileOutputFormat.setOutputCompressionType
								(job, CompressionType.BLOCK);
		SequenceFileOutputFormat.setOutputCompressorClass(job, LzoCodec.class);
		job.setNumReduceTasks(numReducers);
		
		//FileInputFormat.setInputPaths(job, new Path(args[0]));
		MultipleInputs.addInputPath(job, 
				new Path(stageFourOutput),
				SequenceFileInputFormat.class,
				HyperGraphStageFiveMapper1.class);
		
		MultipleInputs.addInputPath(job, 
				new Path(stageThreeOutput),
				SequenceFileInputFormat.class,
				HyperGraphStageFiveMapper2.class);
		
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		job.waitForCompletion(true);
		return 0;
	}
}
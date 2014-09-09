package com.datatorrent.benchmark;

import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.contrib.couchbase.CouchBaseWindowStore;
import com.datatorrent.lib.testbench.RandomEventGenerator;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;

/**
 *
 * @author prerna
 */


public class CouchBaseApp implements StreamingApplication{

    @Override
    public void populateDAG(DAG dag, Configuration conf) {
        int maxValue = 1000;

    RandomEventGenerator rand = dag.addOperator("rand", new RandomEventGenerator());
    rand.setMinvalue(0);
    rand.setMaxvalue(maxValue);
    rand.setTuplesBlast(200);

    CouchBaseOutputOperator couchbaseOutput = dag.addOperator("couchbaseOuput", new CouchBaseOutputOperator());
    CouchBaseInputOperator couchbaseInput = dag.addOperator("couchbaseInput", new CouchBaseInputOperator());
    
   couchbaseOutput.getStore().setBucket("default");
   couchbaseOutput.getStore().setPassword("");
   couchbaseOutput.getStore().setKey("abc");
   couchbaseOutput.getStore().setValue("123");
        try {
            couchbaseOutput.getStore().connect();
        } catch (IOException ex) {
            Logger.getLogger(CouchBaseApp.class.getName()).log(Level.SEVERE, null, ex);
        }
  
    dag.addStream("ss", couchbaseInput.outputPort, couchbaseOutput.input);
    }

   

}
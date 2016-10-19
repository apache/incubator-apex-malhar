/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.datatorrent.contrib.hbase;

import java.io.InterruptedIOException;

import javax.validation.constraints.Min;

import com.datatorrent.api.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;

import com.datatorrent.lib.db.AbstractStoreOutputOperator;

import com.datatorrent.netlet.util.DTThrowable;

/**
 * A base implementation of a StoreOutputOperator operator that stores tuples in HBase rows and offers non-transactional put.Subclasses should provide implementation for put operation. <br>
 * <p>
 * <br>
 * This class provides a HBase output operator that can be used to store tuples
 * in rows in a HBase table. It should be extended by the end-operator
 * developer. The extending class should implement operationPut method and
 * provide a HBase Put metric object that specifies where and what to store for
 * the tuple in the table.<br>
 *
 * <br>
 * This class offers non-transactional put where tuples are put as they come in.
 * @displayName Abstract HBase Put Output
 * @category Output
 * @tags hbase, put
 * @param <T>
 *            The tuple type
 * @since 1.0.2
 */
public abstract class AbstractHBasePutOutputOperator<T> extends AbstractStoreOutputOperator<T, HBaseStore> implements Operator.CheckpointNotificationListener {
  private static final transient Logger logger = LoggerFactory.getLogger(AbstractHBasePutOutputOperator.class);

  public AbstractHBasePutOutputOperator()
  {
    store = new HBaseStore();
  }

  @Override
  public void processTuple(T tuple)
  {
    HTable table = store.getTable();
    Put put = operationPut(tuple);
    try {
      table.put(put);
    } catch (RetriesExhaustedWithDetailsException e) {
      logger.error("Could not output tuple", e);
      DTThrowable.rethrow(e);
    } catch (InterruptedIOException e) {
      logger.error("Could not output tuple", e);
      DTThrowable.rethrow(e);
    }
  }

  @Override
  public void beforeCheckpoint(long windowId)
  {
    try {
      store.getTable().flushCommits();
    } catch (InterruptedIOException e) {
      DTThrowable.rethrow(e);
    } catch (RetriesExhaustedWithDetailsException e) {
      DTThrowable.rethrow(e);
    }
  }

  @Override
  public void checkpointed(long l) {

  }

  @Override
  public void committed(long l) {

  }

  public abstract Put operationPut(T t);
  
}

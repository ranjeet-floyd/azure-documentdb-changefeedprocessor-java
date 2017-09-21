package com.microsoft.azure.documentdb.changefeedprocessor.services;

import com.microsoft.azure.documentdb.changefeedprocessor.IChangeFeedObserverFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class ResourcePartitionServices {
    private JobServices jobServices;
    private CheckpointServices checkpointSvcs;
    private Dictionary<String, ResourcePartition> resourcePartitions;
    private DocumentServices client;
    private IChangeFeedObserverFactory factory;
    private int pageSize;


    public ResourcePartitionServices(DocumentServices client, CheckpointServices checkpointSvcs, IChangeFeedObserverFactory factory, int pageSize) {

        resourcePartitions = new Hashtable<>();
        this.client = client;
        this.checkpointSvcs = checkpointSvcs;
        this.factory = factory;
        this.jobServices = new JobServices();
        this.pageSize = pageSize;
    }

    public ResourcePartition create(String partitionId) {
        Job job = new ChangeFeedJob(partitionId, client, checkpointSvcs, factory.createObserver(), pageSize);
        ResourcePartition resourcePartition = new ResourcePartition(partitionId, job);

        resourcePartitions.put(partitionId, resourcePartition);

        return resourcePartition;
    }

    private ResourcePartition get(String partitionId) {
        return resourcePartitions.get(partitionId);
    }

    public void start(String partitionId) {
        ResourcePartition resourcePartition = this.get(partitionId);
        Job job = resourcePartition.getJob();
        Object initialData = checkpointSvcs.getCheckpointData(partitionId);

        jobServices.runAsync(job, initialData);
    }

    public void stop(String partitionId) {
        // TODO: improve it
        ResourcePartition resourcePartition = this.get(partitionId);
        resourcePartition.stop();
    }
}
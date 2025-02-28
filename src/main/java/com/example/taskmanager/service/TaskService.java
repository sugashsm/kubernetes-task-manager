package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskExecution;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.lang.ProcessBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import java.io.FileReader;
import java.util.Collections;

@Service
@Slf4j
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    private final CoreV1Api api;
    
    public TaskService() throws IOException {
        try {
            // Use in-cluster config
            ApiClient client = ClientBuilder.cluster().build();
            Configuration.setDefaultApiClient(client);
            this.api = new CoreV1Api();
        } catch (Exception e) {
            log.error("Failed to initialize Kubernetes client", e);
            throw new RuntimeException("Failed to initialize Kubernetes client", e);
        }
    }
    
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    public Task getTaskById(String id) {
        return taskRepository.findById(id).orElse(null);
    }
    
    public List<Task> findTasksByName(String name) {
        return taskRepository.findByNameContaining(name);
    }
    
    public Task createTask(Task task) {
        validateCommand(task.getCommand());
        task.setTaskExecutions(new ArrayList<>());
        return taskRepository.save(task);
    }
    
    public void deleteTask(String id) {
        taskRepository.deleteById(id);
    }
    
    public TaskExecution executeTask(String id) throws Exception {
        log.debug("Attempting to execute task with id: {}", id);
        
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
            
            log.debug("Found task: {}", task);
            
            TaskExecution execution = new TaskExecution();
            execution.setStartTime(new Date());
            
            try {
                // Create pod specification
                V1Pod pod = new V1Pod();
                
                // Set metadata
                V1ObjectMeta metadata = new V1ObjectMeta();
                metadata.setGenerateName("task-execution-");
                metadata.setLabels(Collections.singletonMap("app", "task-executor"));
                pod.setMetadata(metadata);
                
                // Create container
                V1Container container = new V1Container();
                container.setName("busybox");
                container.setImage("busybox");
                container.setCommand(List.of("sh", "-c", task.getCommand()));
                
                // Set pod spec
                V1PodSpec spec = new V1PodSpec();
                spec.setContainers(Collections.singletonList(container));
                spec.setRestartPolicy("Never");
                pod.setSpec(spec);

                // Create the pod
                String namespace = "default";
                V1Pod createdPod = api.createNamespacedPod(namespace, pod, null, null, null, null);
                String podName = createdPod.getMetadata().getName();

                // Wait for pod completion
                boolean completed = false;
                int timeoutSeconds = 30;
                while (!completed && timeoutSeconds > 0) {
                    V1Pod runningPod = api.readNamespacedPod(podName, namespace, null);
                    if ("Succeeded".equals(runningPod.getStatus().getPhase()) || 
                        "Failed".equals(runningPod.getStatus().getPhase())) {
                        completed = true;
                    }
                    TimeUnit.SECONDS.sleep(1);
                    timeoutSeconds--;
                }

                // Get pod logs
                String logs = api.readNamespacedPodLog(podName, namespace, null, null, null, null, null, null, null, null, null);
                execution.setEndTime(new Date());
                execution.setOutput(logs);

                // Delete the pod
                api.deleteNamespacedPod(podName, namespace, null, null, null, null, null, null);

                if (task.getTaskExecutions() == null) {
                    task.setTaskExecutions(new ArrayList<>());
                }
                task.getTaskExecutions().add(execution);
                taskRepository.save(task);
                
                return execution;
            } catch (ApiException e) {
                log.error("Kubernetes API error: ", e);
                throw new RuntimeException("Failed to execute task in Kubernetes: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error executing task: ", e);
            throw new RuntimeException("Failed to execute task: " + e.getMessage());
        }
    }
    
    private void validateCommand(String command) {
        // Add your command validation logic here
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("Command cannot be empty");
        }
        // Add more validation rules as needed
        if (command.contains(";") || command.contains("&&") || command.contains("|")) {
            throw new IllegalArgumentException("Command contains unsafe characters");
        }
    }
} 
package am.hhovhann.travel.ai.core.config;

import io.a2a.server.events.EventQueue;
import io.a2a.server.events.QueueManager;
import io.a2a.server.tasks.PushNotificationConfigStore;
import io.a2a.server.tasks.PushNotificationSender;
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class SpringA2AInfrastructureConfig {

    @Bean
    public TaskStore taskStore() {
        // Simple in-memory implementation
        return new SimpleTaskStore();
    }

    @Bean
    public QueueManager queueManager() {
        // Simple in-memory implementation
        return new SimpleQueueManager();
    }

    @Bean
    public PushNotificationConfigStore pushNotificationConfigStore() {
        // Simple in-memory implementation - can be null if not using push notifications
        return new SimplePushNotificationConfigStore();
    }

    @Bean
    public PushNotificationSender pushNotificationSender() {
        // Simple no-op implementation - can be null if not using push notifications
        return new NoOpPushNotificationSender();
    }

    @Bean
    public Executor a2aExecutor() {
        return Executors.newCachedThreadPool();
    }

    // Simple implementations for the required interfaces
    private static class SimpleTaskStore implements TaskStore {
        private final ConcurrentHashMap<String, io.a2a.spec.Task> tasks = new ConcurrentHashMap<>();

        @Override
        public void save(Task task) {
            tasks.put(task.getId(), task);
        }

        @Override
        public io.a2a.spec.Task get(String taskId) {
            return tasks.get(taskId);
        }

        @Override
        public void delete(String taskId) {
            tasks.remove(taskId);
        }
    }

    private static class SimpleQueueManager implements QueueManager {
        private final ConcurrentHashMap<String, io.a2a.server.events.EventQueue> queues = new ConcurrentHashMap<>();

        @Override
        public io.a2a.server.events.EventQueue createOrTap(String taskId) {
            return queues.computeIfAbsent(taskId, k -> io.a2a.server.events.EventQueue.create());
        }

        @Override
        public io.a2a.server.events.EventQueue tap(String taskId) {
            return queues.get(taskId);
        }

        @Override
        public void add(String taskId, io.a2a.server.events.EventQueue queue) throws io.a2a.server.events.TaskQueueExistsException {
            if (queues.putIfAbsent(taskId, queue) != null) {
                throw new io.a2a.server.events.TaskQueueExistsException("Queue already exists for task: " + taskId);
            }
        }

        @Override
        public EventQueue get(String taskId) {
            return queues.get(taskId);
        }

        @Override
        public void close(String taskId) {
            io.a2a.server.events.EventQueue queue = queues.remove(taskId);
            if (queue != null) {
                queue.close();
            }
        }

        @Override
        public void awaitQueuePollerStart(io.a2a.server.events.EventQueue queue) throws InterruptedException {
            // No-op for simple implementation
        }
    }

    private static class SimplePushNotificationConfigStore implements PushNotificationConfigStore {
        private final ConcurrentHashMap<String, java.util.List<io.a2a.spec.PushNotificationConfig>> configs = new ConcurrentHashMap<>();

        @Override
        public io.a2a.spec.PushNotificationConfig setInfo(String taskId, io.a2a.spec.PushNotificationConfig config) {
            configs.computeIfAbsent(taskId, k -> new java.util.ArrayList<>()).add(config);
            return config;
        }

        @Override
        public java.util.List<io.a2a.spec.PushNotificationConfig> getInfo(String taskId) {
            return configs.get(taskId);
        }

        @Override
        public void deleteInfo(String taskId, String configId) {
            java.util.List<io.a2a.spec.PushNotificationConfig> taskConfigs = configs.get(taskId);
            if (taskConfigs != null) {
                taskConfigs.removeIf(config -> config.id().equals(configId));
            }
        }
    }

    private static class NoOpPushNotificationSender implements PushNotificationSender {
        @Override
        public void sendNotification(io.a2a.spec.Task task) {
            // No-op implementation
            System.out.println("NoOpPushNotificationSender");
        }
    }
}

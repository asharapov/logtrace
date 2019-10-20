## Интеграция с java.util.logging 

### Подключение и конфигурация

Для использования API LogTrace совместно с фреймворком `java.util.logging`, в проект следует добавить зависимость: 

```xml
<dependency>
  <groupId>io.github.asharapov.logtrace</groupId>
  <artifactId>logtrace-jul</artifactId>
  <version>${logtrace.version}</version>
</dependency>
```

Для использования LogTrace необходимо в настройках корневого логгера добавить один из стандартных аппендеров
с указанным в нем форматировщиком `io.github.asharapov.logtrace.jul.LogTraceJsonFormatter`, представляющим все записи 
в определенном json формате, адаптированном для немедленной загрузки в ElasticSearch.  
 пр:
```java
public class Configuration {
    private void initLog() {
        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");
        logger.setLevel(Level.ALL);
        for (Handler oldHandler : logger.getHandlers()) {
            logger.removeHandler(oldHandler);
        }
        final Handler handler = new FileHandler("./logs/test-elastic.log");
        handler.setFormatter(new LogTraceJsonFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
    }
}
```

### Известные ограничения

- отсутствует поддержка LogTrace при асинхронной публикации сообщений в логах
- отсутствует поддержка маркеров в API slf4j

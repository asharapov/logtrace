## Интеграция с log4j 1.2

### Подключение и конфигурация

Для использования API LogTrace совместно с библиотекой log4j 1.2, в проект следует добавить зависимость: 

```xml
<dependency>
  <groupId>io.github.asharapov.logtrace</groupId>
  <artifactId>logtrace-log4j</artifactId>
  <version>${logtrace.version}</version>
</dependency>
```

Для использования LogTrace необходимо в конфигурационном файле log4j.properties добавить 
один из стандартных аппендеров для вывода логов в файл или в сокет и указать для него специализированный 
для LogTrace кодировщик, который для каждой записи лога добавляет сведения о ее контексте вызова 
и сохраняет ее в json формате.  
 пр:
```properties
log4j.appender.logtrace-file=org.apache.log4j.FileAppender
log4j.appender.logtrace-file.File=./logs/test-elastic.log
log4j.appender.logtrace-file.layout=io.github.asharapov.logtrace.log4j.LogTraceJsonLayout

log4j.rootLogger=trace, logtrace-file
```

#### Дополнительные параметры кодировщика

   имя    |   тип    | описание
----------|----------|--------- 
formatted | boolean  | Используется в целях визуальной отладки кодировщика для вывода записей в многострочном формате с отступами. По умолчанию имеет значение `false`.
 
пр:

```properties
log4j.appender.logtrace-file=org.apache.log4j.FileAppender
log4j.appender.logtrace-file.File=./logs/test-elastic.log
log4j.appender.logtrace-file.layout=io.github.asharapov.logtrace.log4j.LogTraceJsonLayout
log4j.appender.logtrace-file.layout.formatted=true

log4j.rootLogger=trace, logtrace-file
```

### Известные ограничения

- отсутствует поддержка LogTrace при асинхронной публикации сообщений в логах
- отсутствует поддержка маркеров в API slf4j

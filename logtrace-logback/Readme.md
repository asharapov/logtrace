## Интеграция с logback

### Установка и конфигурация

Logtrace обеспечивает совместимость с logback-classic версии `1.2.*`.  
Для использования API LogTrace совместно с библиотекой logback-classic, в проект следует добавить зависимость: 

```xml
<dependency>
  <groupId>io.github.asharapov.logtrace</groupId>
  <artifactId>logtrace-logback</artifactId>
  <version>${logtrace.version}</version>
</dependency>
```

Для использования LogTrace необходимо в конфигурационном файле `logback.xml` использовать любой стандартный аппендер
со специфическим для LogTrace кодировщиком, представляющим все записи в определенном json формате, адаптированном
для немедленной загрузки в ElasticSearch.  
пр:
```xml
<configuration>
  <appender name="logtrace-file" class="ch.qos.logback.core.FileAppender">
    <file>./logs/test-elastic.log</file>
    <encoder class="io.github.asharapov.logtrace.logback.LogTraceJsonEncoder"/>
  </appender>
  <root level="trace">
    <appender-ref ref="logtrace-file"/>
  </root>
</configuration>
```

#### Дополнительные параметры кодировщика

| имя       | тип     | описание                               |
|-----------|---------|----------------------------------------|
| formatted | boolean | Используется в целях визуальной отладки кодировщика для вывода записей в многострочном формате с отступами.<br> Значение по умолчанию: `false`. |
 
пр:
```xml
<configuration debug="true">
  <appender name="logtrace-file" class="ch.qos.logback.core.FileAppender">
    <file>./logs/test-elastic.log</file>
    <encoder class="io.github.asharapov.logtrace.logback.LogTraceJsonEncoder" formatted="true"/>
  </appender>
  <root level="trace">
    <appender-ref ref="logtrace-file"/>
  </root>
</configuration>
```

### Известные ограничения

- отсутствует поддержка LogTrace при асинхронной публикации сообщений в логах

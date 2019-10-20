## Интеграция с log4j2

### Подключение и конфигурация

Logtrace обеспечивает совместимость с log4j2 версий `2.8.1` и выше.  
Для использования API LogTrace совместно с библиотекой log4j2, в проект следует добавить зависимость: 

```xml
<dependency>
  <groupId>io.github.asharapov.logtrace</groupId>
  <artifactId>logtrace-log4j2</artifactId>
  <version>${logtrace.version}</version>
</dependency>
```

Для использования LogTrace необходимо в конфигурационном файле log4j2.xml добавить 
один из стандартных аппендеров для вывода логов в файл или в сокет и указать для него специализированный 
для LogTrace кодировщик, который для каждой записи лога добавляет сведения о ее контексте вызова 
и сохраняет ее в json формате.  
пр:
```xml
<Configuration name="demo" packages="io.github.asharapov.logtrace.log4j2">
  <Appenders>
    <File name="logtrace-file" fileName="./logs/test-elastic.log">
      <LogTraceJsonLayout />
    </File>
  </Appenders>
  <Loggers>
    <Root level="trace">
        <AppenderRef ref="logtrace-file" />
    </Root>
  </Loggers>
</Configuration>
```

#### Дополнительные параметры кодировщика

   имя    |   тип    | описание
----------|----------|--------- 
formatted | boolean  | Используется в целях визуальной отладки кодировщика для вывода записей в многострочном формате с отступами. По умолчанию имеет значение `false`.
 
пр:

```xml
<Configuration name="demo" status="warn" verbose="true" packages="io.github.asharapov.logtrace.log4j2">
  <Appenders>
    <File name="logtrace-file" fileName="./logs/test-elastic.log">
      <LogTraceJsonLayout formatted="true" />
    </File>
  </Appenders>
  <Loggers>
    <Root level="trace">
        <AppenderRef ref="logtrace-file" />
    </Root>
  </Loggers>
</Configuration>
```

### Известные ограничения

Каких-либо ограничений при использовании log4j2 не выявлено.

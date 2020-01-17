[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.asharapov.logtrace/logtrace-core/badge.png)](https://search.maven.org/artifact/io.github.asharapov.logtrace/logtrace-core/)

# LogTrace

Данная библиотека представляет собой дополнение для популярных java-фреймворков логирования и позволяет:
1. Форматировать записи в логах в формате JSON максимально пригодном для последующей их прямой загрузки в ElasticSearch без необходимости выполнения 
каких-либо дополнительных их преобразований.
2. Включать в записи логов расширенные (по сравнению с т.н. _mapped diagnostic context_, предлагаемым популярными фреймворками логирования) 
сведения о контексте, в рамках которого выполняется блок кода программы, породивший эти записи в лог. Внедряемая таким образом в записи логов 
контекстная информация будет служить базой для формирования по ним различных аналитических запросов и метрик в ElasticSearch и Kibana. 

#### Содержание
1. [Интеграция с фреймворками для логирования](#Интеграция-с-фреймворками-для-логирования)
2. [Простейший пример использования](#Простейший-пример-использования)
3. [Внедрение контекстных данных](#Внедрение-контекстных-данных)
4. [Отправка логов в ElasticSearch](#Отправка-логов-в-ElasticSearch)


### Интеграция с фреймворками для логирования

1. [log4j 2.8+ (рекомендуется)](./logtrace-log4j2/Readme.md)
2. [logback-classic 1.2.*](./logtrace-logback/Readme.md)
3. [log4j 1.2.*](./logtrace-log4j/Readme.md)
4. [java.util.logging](./logtrace-jul/Readme.md)


### Простейший пример использования 
После подключения в демонстрационный проект одного из вышеприведенных
фреймворков для логирования совместно с данной библиотекой для кодирования в 
JSON записей в логах, мы можем продемонстрировать простейший пример ее 
использования в нижеприведенном методе `Demo.example1`:
```java
package io.github.asharapov.logtrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);
    // ...
    private static void example1() {
        log.info("Hello, world!");
        try {
            throw new IllegalStateException("some error");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
```
По итогам его работы в логах будут сформированы две записи (приведены в отформатированном виде):
```text
{
  "@timestamp" : "2019-11-02T10:37:14.837+04:00",
  "thread" : "main",
  "lvl" : "INFO",
  "logger" : "io.github.asharapov.logtrace.Demo",
  "msg" : "Hello, world!"
}
{
  "@timestamp" : "2019-11-02T10:37:14.854+04:00",
  "thread" : "main",
  "lvl" : "ERROR",
  "logger" : "io.github.asharapov.logtrace.Demo",
  "msg" : "An error handled: some error",
  "thrown" : {
    "cls" : "java.lang.IllegalStateException",
    "msg" : "some error",
    "stack" : "java.lang.IllegalStateException: some error\n\tat io.github.asharapov.logtrace.Demo.example1(Demo.java:19)\n\tat io.github.asharapov.logtrace.Demo.main(Demo.java:10)\n",
    "hash" : "824acdb707d3612db81c4bbf177f7738"
  }
}
```
Здесь в атрибуте `thrown.hash` содержится MD5 хэш от стека вызова где произошла ошибка. Данное значение удобно использовать для поиска уникальных ошибок в программе.


### Внедрение контекстных данных

API управления контекстом используется для представления всего прикладного кода в виде композиции из множества логически целостных единиц работ 
и сопоставления с каждой такой единицей работы тегов в виде `ключ = значение`, призванных помочь как с анализом ключевых моментов работы программы 
так и построения при помощи этих тегов разного рода аналитики и метрик в таких инструментах как ElasticSearch и Kibana.   

Пример:  
Допустим в нашей программе вышеупомянутый метод `Demo.example1()` является частью процедуры обработки некоторой операции `test-action`, 
с параметрами `count`, `enabled` и `name`, которые могут нам оказаться полезными для понимания происходящего в данной операции в ходе 
последующего анализа журнала логов.  
Пример исходного кода для обработки подобной операции представлен в виде метода `Demo.example2`: 
```java
public class Demo {
    // ...
    public static void main(String[] args) {
        example2("item-23", 1, true);
    }
    // ...
    private static void example2(String name, int count, boolean enabled) {
        LogSpan span = LogTracer.getDefault().buildSpan("test-action")
                .withTag("name", name)
                .withTag("count", count)
                .withTag("enabled", enabled)
                .activate();
        try {
            // ...
            example1();
            // ...
        } finally {
            span.close();
        }
    }
    // ...
}
```
По итогам его работы в логах будут сформированы следующие 4 записи (приведены в отформатированном виде):
```text
{
  "@timestamp" : "2019-11-02T10:37:14.877+04:00",
  "@trace" : "tid:50696@pluto:1",
  "thread" : "main",
  "lvl" : "INFO",
  "logger" : "tracer.ctx.test-action",
  "msg" : "Span \"test-action\" started",
  "marker" : "SPAN ENTER",
  "ctx" : {
    "test-action" : {
      "name" : "item-23",
      "count" : 1,
      "enabled" : true
    }
  }
}
{
  "@timestamp" : "2019-11-02T10:37:14.879+04:00",
  "@trace" : "tid:50696@pluto:1",
  "thread" : "main",
  "lvl" : "INFO",
  "logger" : "io.github.asharapov.logtrace.Demo",
  "msg" : "Hello, world!",
  "ctx" : {
    "test-action" : {
      "name" : "item-23",
      "count" : 1,
      "enabled" : true
    }
  }
}
{
  "@timestamp" : "2019-11-02T10:37:14.880+04:00",
  "@trace" : "tid:50696@pluto:1",
  "thread" : "main",
  "lvl" : "ERROR",
  "logger" : "io.github.asharapov.logtrace.Demo",
  "msg" : "An error handled: some error",
  "thrown" : {
    "cls" : "java.lang.IllegalStateException",
    "msg" : "some error",
    "stack" : "java.lang.IllegalStateException: some error\n\tat io.github.asharapov.logtrace.Demo.example1(Demo.java:19)\n\tat io.github.asharapov.logtrace.Demo.example2(Demo.java:33)\n\tat io.github.asharapov.logtrace.Demo.main(Demo.java:11)\n",
    "hash" : "a2fd56d8fce75c116e323d6680fc5269"
  },
  "ctx" : {
    "test-action" : {
      "name" : "item-23",
      "count" : 1,
      "enabled" : true
    }
  }
}
{
  "@timestamp" : "2019-11-02T10:37:14.881+04:00",
  "@trace" : "tid:50696@pluto:1",
  "thread" : "main",
  "lvl" : "INFO",
  "logger" : "tracer.ctx.test-action",
  "msg" : "Span \"test-action\" completed successfully within 5 ms",
  "marker" : "SPAN FINISH",
  "ctx" : {
    "test-action" : {
      "name" : "item-23",
      "count" : 1,
      "enabled" : true,
      "@time" : 5
    }
  }
}
```
Как можно увидеть, в логе появились две дополнительные записи, фиксирующие начало и завершения работы над интересующим нас блоком кода (единицей работы). 
В записи о завершении при этом появляется служебный атрибут `ctx.<op-name>.@time` содержащий время выполнения данной единицы работы в милисекундах. 
Данный атрибут удобно использовать в метриках kibana для анализа времени выполнения тех или иных видов работ в коде программы.  
Библиотека позволяет не только фиксировать время выполнения некоторых единиц работы в коде но и статус их завершения (успешно/не успешно).
Пример единицы работы завершенной с ошибкой представлен в методе `Demo.example3()`:
```java
public class Demo {
    // ...
    private static void example3(String name, int count, boolean enabled) {
        LogSpan span = LogTracer.getDefault().buildSpan("test-action")
                .withTag("name", name)
                .withTag("count", count)
                .withTag("enabled", enabled)
                .activate();
        try {
            // ...
            log.info("Hello, {}!", name);
            throw new IllegalArgumentException("Test error");
            // ...
        } catch (Exception e) {
            span.markAsFailed(e);
        } finally {
            span.close();
        }
    }
}
```
Логи сформированные в ходе работы данного метода (приведены в отформатированном виде):
```text
{
  "@timestamp" : "2019-11-02T10:37:14.882+04:00",
  "@trace" : "tid:50696@pluto:2",
  "thread" : "main",
  "lvl" : "INFO",
  "logger" : "tracer.ctx.test-action",
  "msg" : "Span \"test-action\" started",
  "marker" : "SPAN ENTER",
  "ctx" : {
    "test-action" : {
      "name" : "item-23",
      "count" : 1,
      "enabled" : true
    }
  }
}
{
  "@timestamp" : "2019-11-02T10:37:14.883+04:00",
  "@trace" : "tid:50696@pluto:2",
  "thread" : "main",
  "lvl" : "INFO",
  "logger" : "io.github.asharapov.logtrace.Demo",
  "msg" : "Hello, item-23!",
  "ctx" : {
    "test-action" : {
      "name" : "item-23",
      "count" : 1,
      "enabled" : true
    }
  }
}
{
  "@timestamp" : "2019-11-02T10:37:14.883+04:00",
  "@trace" : "tid:50696@pluto:2",
  "thread" : "main",
  "lvl" : "ERROR",
  "logger" : "tracer.ctx.test-action",
  "msg" : "Span \"test-action\" completed with error within 1 ms",
  "marker" : "SPAN FINISH",
  "thrown" : {
    "cls" : "java.lang.IllegalArgumentException",
    "msg" : "Test error",
    "stack" : "java.lang.IllegalArgumentException: Test error\n\tat io.github.asharapov.logtrace.Demo.example3(Demo.java:49)\n\tat io.github.asharapov.logtrace.Demo.main(Demo.java:12)\n",
    "hash" : "b4dcc489a1f267d67426a1e3ea43955b"
  },
  "ctx" : {
    "test-action" : {
      "name" : "item-23",
      "count" : 1,
      "enabled" : true,
      "@time" : 1
    }
  }
}
```
При конструировании контекста - дескриптора единицы работы нашего прикладного кода (см. интерфейсы LogSpan и SpanBuilder) можно управлять:
- Тегами и их значениями ассоциированными с данным контекстом - методы `SpanBuilder.withTag(...)` и `LogSpan.withTag(...)`.
- Какой логгер следует использовать для вывода в лог служебных записей отмечающих границы единцы работы - методы `SpanBuilder.withLogger(...)`.
- Условиями при выполнении которых в логах будут печататься записи отмечающие границы единицы работы:   
  - либо отключить их вовсе, используя метод `SpanBuilder.withoutEvents()`;    
  - либо оставить только запись отмечающую момент завершения единицы работы, используя метод `SpanBuilder.withClosingEvent()`;  
  - либо принимать решения по факту, зарегистрировав свою функцию-предикат для принятия решения о печати каждой записи, используя метод `SpanBuilder.withEventFilter(EventFilter)`.  
пример: чтобы выводить в лог записи только о завершении единицы работы и только если время затраченное на ее выполнение превышает 100ms:
следует использовать предикат: `.withEventFilter(s -> s.getDuration() > 100)`.
  
Каждая выделяемая в коде при помощи интерфейса `LogSpan` единица работы может быть как сама состоять из иных более мелких единиц работ 
так и быть частью некоторой более масштабной единицы работы.   
В следующем примере представлены две единицы работы: 
- первая из них `auth` в методе `Demo.example4()` - олицетворяет обработку поступившего запроса от имени некоторого аутентифицированного пользователя;
- вложенная в него вторая единица работы `test-action` - выполнение некоторой бизнес-операции  

пример:
```java
public class Demo {
    // ...
    private static void example4() {
        LogSpan span = LogTracer.getDefault().buildSpan("auth")
                .withTag("user", "demouser")
                .withTag("role", "guest")
                .withoutEvents()
                .activate();
        try {
            example2("item-6", 2, false);
            log.debug("completed");
        } finally {
            span.close();
        }
    }
}
```
Логи сформированные в ходе работы данного метода (приведены в отформатированном виде):
```text
{
  "@timestamp" : "2019-11-02T10:37:14.884+04:00",
  "@trace" : "tid:50696@pluto:3",
  "thread" : "main",
  "lvl" : "INFO",
  "logger" : "tracer.ctx.test-action",
  "msg" : "Span \"test-action\" started",
  "marker" : "SPAN ENTER",
  "ctx" : {
    "auth" : {
      "role" : "guest",
      "user" : "demouser"
    },
    "test-action" : {
      "name" : "item-6",
      "count" : 2,
      "enabled" : false
    }
  }
}
{
  "@timestamp" : "2019-11-02T10:37:14.885+04:00",
  "@trace" : "tid:50696@pluto:3",
  "thread" : "main",
  "lvl" : "INFO",
  "logger" : "io.github.asharapov.logtrace.Demo",
  "msg" : "Hello, world!",
  "ctx" : {
    "auth" : {
      "role" : "guest",
      "user" : "demouser"
    },
    "test-action" : {
      "name" : "item-6",
      "count" : 2,
      "enabled" : false
    }
  }
}
{
  "@timestamp" : "2019-11-02T10:37:14.885+04:00",
  "@trace" : "tid:50696@pluto:3",
  "thread" : "main",
  "lvl" : "ERROR",
  "logger" : "io.github.asharapov.logtrace.Demo",
  "msg" : "An error handled: some error",
  "thrown" : {
    "cls" : "java.lang.IllegalStateException",
    "msg" : "some error",
    "stack" : "java.lang.IllegalStateException: some error\n\tat io.github.asharapov.logtrace.Demo.example1(Demo.java:19)\n\tat io.github.asharapov.logtrace.Demo.example2(Demo.java:33)\n\tat io.github.asharapov.logtrace.Demo.example4(Demo.java:65)\n\tat io.github.asharapov.logtrace.Demo.main(Demo.java:13)\n",
    "hash" : "488879f5a58feb78c580a78aa1f0038c"
  },
  "ctx" : {
    "auth" : {
      "role" : "guest",
      "user" : "demouser"
    },
    "test-action" : {
      "name" : "item-6",
      "count" : 2,
      "enabled" : false
    }
  }
}
{
  "@timestamp" : "2019-11-02T10:37:14.886+04:00",
  "@trace" : "tid:50696@pluto:3",
  "thread" : "main",
  "lvl" : "INFO",
  "logger" : "tracer.ctx.test-action",
  "msg" : "Span \"test-action\" completed successfully within 2 ms",
  "marker" : "SPAN FINISH",
  "ctx" : {
    "auth" : {
      "role" : "guest",
      "user" : "demouser"
    },
    "test-action" : {
      "name" : "item-6",
      "count" : 2,
      "enabled" : false,
      "@time" : 2
    }
  }
}
{
  "@timestamp" : "2019-11-02T10:37:14.886+04:00",
  "@trace" : "tid:50696@pluto:3",
  "thread" : "main",
  "lvl" : "DEBUG",
  "logger" : "io.github.asharapov.logtrace.Demo",
  "msg" : "completed",
  "ctx" : {
    "auth" : {
      "role" : "guest",
      "user" : "demouser"
    }
  }
}
```

### Отправка логов в ElasticSearch

Для демонстрации возможностей по обработке логов в ElasticSearch и Kibana
можно использовать логи формируемые в ходе прогона модульных тестов библиотеки.
Тесты можно выполнить командой:
```bash
$ mvn clean test
```
Отправку логов в ElasticSearch можно осуществлять различными способами. 
В каталоге `./docker` приведены скрипты и конфигурационные файлы для отправки логов при помощи сервисов:
1. [filebeat](https://www.elastic.co/downloads/beats/filebeat)  
Для запуска соответствующих docker контейнеров elasticsearch, kibana, filebeat требуется либо запустить скрипт `./efk.sh up filebeat`
либо напрямую выполнить команду:  
`docker-compose -f ./docker/docker-compose.yml up -d filebeat`
2. [fluent-bit](https://fluentbit.io/)  
Для запуска соответствующих docker контейнеров elasticsearch, kibana, fluentbit требуется либо запустить скрипт `./efk.sh up fluentbit`
либо напрямую выполнить команду:  
`docker-compose -f ./docker/docker-compose.yml up -d fluentbit`


После того как контейнеры с перечисленными процессами были запущены и инициализированы,
можно будет проверить доступность логов в [kibana](http://localhost:5601/app/kibana).
Все логи будут отправлены либо в индексы `logs-logtrace-*` (filebeat)
либо в индексы `logs-logtrace-fluent-*` (fluent-bit).
 
Остановку сервисов и удаление всех связанных с ними данных можно выполнить командой `./efk.sh down`.



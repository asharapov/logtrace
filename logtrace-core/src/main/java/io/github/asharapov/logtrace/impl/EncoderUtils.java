package io.github.asharapov.logtrace.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.BufferRecycler;
import io.github.asharapov.logtrace.LogSpan;
import io.github.asharapov.logtrace.Tag;

/**
 * Код общий для всех реализаций кодировщиков логов.
 *
 * @author Anton Sharapov
 */
public class EncoderUtils {

    public static final String DEFAULT_EOL = "\r\n";
    private static final char[] EMPTY_CHARS = {};
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final Comparator<LogSpan> LOG_SPAN_COMPARATOR =
            Comparator.comparing(LogSpan::getNamespace)
                    .thenComparing(LogSpan::getOperation)
                    .thenComparingLong(LogSpan::getStartTime);

    private static final ThreadLocal<State> tld = ThreadLocal.withInitial(State::new);


    public static State getState() {
        return tld.get();
    }


    public static final class State {
        private final Calendar calendar;
        private final StringBuilder tsbuf;
        private final MessageDigest digester;
        private volatile SoftReference<BufferRecycler> bufref;

        private State() {
            this.calendar = Calendar.getInstance();
            this.tsbuf = new StringBuilder(30);
            try {
                this.digester = MessageDigest.getInstance("md5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            this.bufref = new SoftReference<>(new BufferRecycler());
        }

        public BufferRecycler getBufferRecycler() {
            BufferRecycler br = bufref.get();
            if (br == null) {
                br = new BufferRecycler();
                bufref = new SoftReference<>(br);
            }
            return br;
        }

        /**
         * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-ddTHH:mm:ss.SX</code>.
         * Если исходная дата равна <code>null</code> то метод не делает ничего.
         *
         * @param timestamp дата которую требуется преобразовать в строку.
         */
        public String formatISODateTime(final long timestamp) {
            final Calendar cal = calendar;
            final StringBuilder buf = tsbuf;
            buf.setLength(0);
            cal.setTimeInMillis(timestamp);
            buf.append(cal.get(Calendar.YEAR));
            buf.append('-');
            int p = cal.get(Calendar.MONTH) + 1;
            if (p < 10)
                buf.append('0');
            buf.append(p);
            buf.append('-');
            p = cal.get(Calendar.DAY_OF_MONTH);
            if (p < 10)
                buf.append('0');
            buf.append(p);
            buf.append('T');
            p = cal.get(Calendar.HOUR_OF_DAY);
            if (p < 10)
                buf.append('0');
            buf.append(p);
            buf.append(':');
            p = cal.get(Calendar.MINUTE);
            if (p < 10)
                buf.append('0');
            buf.append(p);
            buf.append(':');
            p = cal.get(Calendar.SECOND);
            if (p < 10)
                buf.append('0');
            buf.append(p);
            buf.append('.');
            p = cal.get(Calendar.MILLISECOND);
            if (p < 10) {
                buf.append("00");
            } else if (p < 100) {
                buf.append('0');
            }
            buf.append(p);
            p = cal.get(Calendar.ZONE_OFFSET);
            if (p == 0) {
                buf.append('Z');
                return buf.toString();
            }
            if (p > 0) {
                buf.append('+');
            } else {
                buf.append('-');
                p = -p;
            }
            long hour = TimeUnit.MILLISECONDS.toHours(p);
            if (hour < 10)
                buf.append('0');
            buf.append(hour);
            buf.append(":00");
            return buf.toString();
        }

        /**
         * Вычисляет md5 хэш от указанного массива байт и возвращает результат в виде
         * массива символов с 16-ричным представлением полученного хэша.
         *
         * @param srcData массив байт для которого требуется вернуть подпись.
         * @return строка с шестнадцатиричным представлением содержимого исходного массива.
         */
        public char[] digest(final byte[] srcData) {
            if (srcData == null || srcData.length == 0)
                return EMPTY_CHARS;
            final byte[] dstData = digester.digest(srcData);
            final char[] chars = new char[dstData.length * 2];
            for (int i = 0, p = 0; i < dstData.length; i++) {
                final byte b = dstData[i];
                chars[p++] = HEX_DIGITS[(0xF0 & b) >>> 4];
                chars[p++] = HEX_DIGITS[0x0F & b];
            }
            return chars;
        }

        /**
         * Запись в поток сведений об ошибке (если она есть).
         *
         * @param jg    поток для записи используемый для представления одной записи в логе в JSON формате.
         * @param cause сведения об ошибке или <code>null</code>.
         */
        public void writeThrowable(final JsonGenerator jg, final Throwable cause) throws IOException {
            if (cause == null)
                return;

            final StringWriter buf = new StringWriter(512);
            cause.printStackTrace(new PrintWriter(buf, false));
            final String stack = buf.toString();
            final char[] hash = digest(stack.getBytes(StandardCharsets.UTF_8));

            jg.writeObjectFieldStart("thrown");
            jg.writeStringField("cls", cause.getClass().getName());
            jg.writeStringField("msg", cause.getMessage());
            jg.writeStringField("stack", stack);
            //jg.writeStringField("hash", new String(hash));
            jg.writeFieldName("hash");
            jg.writeString(hash, 0, hash.length);
            jg.writeEndObject();
        }

        public void writeMessageContext(final JsonGenerator jg, final Map<String, ?> mdc) throws IOException {
            if (mdc != null && !mdc.isEmpty()) {
                jg.writeObjectFieldStart("mdc");
                for (Map.Entry<String, ?> entry : mdc.entrySet()) {
                    final Object v = entry.getValue();
                    if (v != null)
                        jg.writeStringField(entry.getKey(), String.valueOf(v));
                }
                jg.writeEndObject();
            }
        }

        /**
         * Запись в поток сведений о контексте в рамках которого была порождена обрабатываемая запись в лог.
         *
         * @param jg         поток для записи используемый для представления одной записи в логе в JSON формате.
         * @param activeSpan текущий контекст или <code>null</code>.
         */
        public void writeSpanContext(final JsonGenerator jg, final LogSpan activeSpan) throws IOException {
            if (activeSpan == null)
                return;

            // переупорядочим список дескрипторов работ по используемому ими пространству имен ...
            final ArrayList<LogSpan> list = new ArrayList<>();
            for (LogSpan span = activeSpan; span != null; span = span.getParentSpan()) {
                list.add(span);
            }
            list.sort(LOG_SPAN_COMPARATOR);

            String ns = null, op = null;
            int deep = 0;
            for (int i = 0, cnt = list.size(); i < cnt; i++) {
                final LogSpan span = list.get(i);
                if (ns == null || !ns.equals(span.getNamespace())) {
                    if (ns != null) {
                        if (deep > 0)
                            jg.writeEndArray();
                        jg.writeEndObject();
                    }
                    ns = span.getNamespace();
                    jg.writeObjectFieldStart(ns);
                    op = null;
                    deep = 0;
                }

                if (!span.getOperation().equals(op)) {
                    if (deep > 0) {
                        jg.writeEndArray();
                        deep = 0;
                    }
                    op = span.getOperation();
                    jg.writeFieldName(op);
                    if (i + 1 < cnt && op.equals(list.get(i + 1).getOperation())) {
                        jg.writeStartArray();
                        deep = 1;
                    }
                } else {
                    if (deep > 3) {
                        // TODO: подумать что нам правильнее делать с длинными рекурсиями однотипных работ
                        continue;
                    }
                    deep++;
                }
                writeSpanInternals(jg, span);
            }
            jg.writeEndObject();
        }

        private void writeSpanInternals(final JsonGenerator jg, final LogSpan span) throws IOException {
            jg.writeStartObject();
            for (Tag tag : span.getTags()) {
                final Object v = tag.getValue();
                if (v == null)
                    continue;
                switch (tag.getType()) {
                    case STRING:
                        jg.writeStringField(tag.getName(), (String) v);
                        break;
                    case BOOLEAN:
                        jg.writeBooleanField(tag.getName(), (boolean) v);
                        break;
                    case INT:
                        jg.writeNumberField(tag.getName(), (int) v);
                        break;
                    case LONG:
                        jg.writeNumberField(tag.getName(), (long) v);
                        break;
                    case DOUBLE:
                        jg.writeNumberField(tag.getName(), (double) v);
                        break;
                    case FLOAT:
                        jg.writeNumberField(tag.getName(), (float) v);
                        break;
                    case BIG_DECIMAL:
                        jg.writeNumberField(tag.getName(), (BigDecimal) v);
                        break;
                    case BIG_INTEGER:
                        jg.writeFieldName(tag.getName());
                        jg.writeNumber((BigInteger) v);
                        break;
                    case DATE:
                        jg.writeStringField(tag.getName(), formatISODateTime(((Date) v).getTime()));
                        break;
                }
            }
            if (!span.isActive()) {
                jg.writeNumberField("@time", span.getDuration());
            }
            jg.writeEndObject();
        }

    }

}
package com.redislabs.riot;

import com.redislabs.riot.convert.RegexNamedGroupsExtractor;
import com.redislabs.riot.processor.MapProcessor;
import com.redislabs.riot.processor.SpelProcessor;
import io.lettuce.core.api.StatefulConnection;
import lombok.Data;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.core.convert.converter.Converter;
import picocli.CommandLine.Option;

import java.text.SimpleDateFormat;
import java.util.*;

@Data
public class KeyValueProcessingOptions {

    @Option(arity = "1..*", names = "--spel", description = "SpEL expression to produce a field", paramLabel = "<f=exp>")
    private Map<String, String> spelFields = new HashMap<>();
    @Option(arity = "1..*", names = "--var", description = "Register a variable in the SpEL processor context", paramLabel = "<v=exp>")
    private Map<String, String> variables = new HashMap<>();
    @Option(names = "--date", description = "Processor date format (default: ${DEFAULT-VALUE})", paramLabel = "<string>")
    private String dateFormat = new SimpleDateFormat().toPattern();
    @Option(arity = "1..*", names = "--regex", description = "Extract named values from source field using regex", paramLabel = "<f=exp>")
    private Map<String, String> regexes = new HashMap<>();

    public ItemProcessor<Map<String, Object>, Map<String, Object>> processor(StatefulConnection<String, String> connection) {
        List<ItemProcessor<Map<String, Object>, Map<String, Object>>> processors = new ArrayList<>();
        if (!spelFields.isEmpty()) {
            processors.add(new SpelProcessor(connection, new SimpleDateFormat(dateFormat), variables, spelFields));
        }
        if (!regexes.isEmpty()) {
            Map<String, Converter<String, Map<String, String>>> fields = new LinkedHashMap<>();
            regexes.forEach((f, r) -> fields.put(f, RegexNamedGroupsExtractor.builder().regex(r).build()));
            processors.add(new MapProcessor(fields));
        }
        if (processors.isEmpty()) {
            return null;
        }
        if (processors.size() == 1) {
            return processors.get(0);
        }
        CompositeItemProcessor<Map<String, Object>, Map<String, Object>> compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(processors);
        return compositeItemProcessor;
    }

}

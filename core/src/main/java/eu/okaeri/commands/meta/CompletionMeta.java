package eu.okaeri.commands.meta;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.annotation.Completion;
import eu.okaeri.commands.annotation.CompletionData;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
public class CompletionMeta {

    private Map<String, List<String>> completions = new LinkedHashMap<>();
    private Map<String, Map<String, String>> data = new LinkedHashMap<>();

    public static CompletionMeta of(@NonNull Commands commands, @NonNull Method method) {

        Completion[] completions = method.getAnnotationsByType(Completion.class);
        if (completions.length == 0) {
            return new CompletionMeta();
        }

        CompletionMeta meta = new CompletionMeta();
        for (Completion completion : completions) {

            List<String> values = Arrays.stream(completion.value())
                .map(commands::resolveText)
                .collect(Collectors.toList());

            Map<String, String> dataMap = new LinkedHashMap<>();
            CompletionData[] completionData = completion.data();

            for (CompletionData data : completionData) {
                String name = commands.resolveText(data.name());
                String value = commands.resolveText(data.value());
                dataMap.put(name, value);
            }

            for (String _arg : completion.arg()) {
                String key = commands.resolveText(_arg);
                meta.completions.put(key, values);
                meta.data.put(key, dataMap);
            }
        }

        return meta;
    }

    public List<String> getCompletions(@NonNull String argumentName) {
        List<String> list = this.completions.get(argumentName);
        return (list == null) ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    public Map<String, String> getData(@NonNull String argumentName) {
        Map<String, String> data = this.data.get(argumentName);
        return (data == null) ? Collections.emptyMap() : Collections.unmodifiableMap(data);
    }
}

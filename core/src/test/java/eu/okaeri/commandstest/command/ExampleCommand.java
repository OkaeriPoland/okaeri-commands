package eu.okaeri.commandstest.command;

import eu.okaeri.commands.annotation.*;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commands.service.Option;

import java.util.Arrays;

@Command(label = "cmd", description = "Example command service")
public class ExampleCommand implements CommandService {

    // cmd
    //
    // empty pattern represents command without
    // arguments (similar to @Default in other frameworks)
    //
    @Executor(pattern = "")
    public String def() {
        return "called default";
    }

    // cmd woah
    //
    // simple commands with no additional effort
    // using method name is same as pattern = "woah"
    //
    @Executor(description = "Prints woah message")
    public String woah() {
        return "woah!";
    }

    // cmd hello
    // cmd hello everyone
    //
    // two patterns are possible in single method, they should
    // however match (e.g. have all the same non-static arguments)
    //
    @Executor(pattern = {"hello", "hello everyone"}, description = "Prints hello message")
    public String hello_everyone() {
        return "hello!";
    }

    // cmd hi
    // cmd hii
    //
    // overwrite usage/description with CommandsAdapter#resolveText()
    // available out-of-the-box in OkaeriPoland/okaeri-platform i18n integration
    //
    @Executor(pattern = {"hi", "hii"}, description = "!command-cmd-hi-description", usage = "!command-cmd-hi-usage")
    public String hi() {
        return "hi!";
    }

    // cmd hello <name>
    //
    // required arguments can be specified using '<PARAMETER_NAME>'
    // or '*' (recommended with -parameters compiler option, uses in-order param name)
    //
    @Executor(pattern = "hello *", description = "Prints hello message with name")
    @Completions(@Completion(arg = "name", value = "@allplayers"))
    public String hello_name(@Arg("name") String name) {
        return "hello " + name;
    }

    // cmd hi [name]
    //
    // optional arguments can be specified using '[PARAMETER_NAME]'
    // or '?' (recommended with -parameters compiler option, uses in-order param name)
    //
    // optional arguments require param to be an Optional or preferably
    // in-house eu.okaeri.commands.service.Option with additional utilities
    //
    @Executor(pattern = "hi ?", description = "Prints hi message with optional name")
    public String hi_name(@Arg("name") Option<String> name) {
        return "hi, " + name.getOr("guest");
    }

    // cmd salute [nameAndSurname]
    // cmd salute John Doe
    //
    // accepts specific number of arguments into one parameter
    // example: *:2 same as <name:2>, ?:2 same as [name:2]
    //
    // optional arguments require param to be an Optional or preferably
    // in-house eu.okaeri.commands.service.Option with additional utilities
    //
    @Executor(pattern = "salute ?:2" , description = "Prints salute message with optional name and surname")
    public String salute_nameAndSurname(@Arg("nameAndSurname") Option<String> name) {
        return "salute " + name.getOr("some guest") + "!";
    }

    // cmd print <message>
    // cmd print Some message with unspecified length
    //
    // accepts all sequential arguments into one parameter
    //
    @Executor(pattern = "print *..." , description = "Prints system out message")
    public void log_message(@Arg("message") String message) {
        System.out.println(message);
    }

    // cmd player <player> set <perm> <value> [flag]
    //
    // mix and match pattern elements, return values to be processed by adapter
    // see below for recommended and simplified pattern variant (for -parameters compiler flag)
    //
    @Executor(pattern = "player <player> set <perm> <value> [flag]", description = "Complex command test")
    public String complex1(@Arg("player") String name, @Arg("perm") String perm, @Arg("value") String value, @Arg("flag") Option<String> flag) {
        return name + " " + perm + " " + value + " " + flag;
    }

    // recommended usage
    //
    // mix param types and resolve unknown values by overriding CommandsAdapter#resolveMissingArgument (e.g. DI)
    // preserve param names using javac -g:vars or specify them manually @Arg("name")
    //
    @Executor(pattern = "player * set2 * * ?", description = "Complex command test")
    @Completions({
            @Completion(arg = "name", value = "@allplayers", meta = @CompletionMeta(name = "limit", value = "10")), // same as @allplayers;limit=10
            @Completion(arg = "perm", value = {"build", "break", "place"}),
            @Completion(arg = "value", value = {"allow", "deny"}),
            @Completion(arg = "flag", value = {"-silent"}),
    })
    public String complex2(@Arg String name, int huh, @Arg String perm, @RawArgs String[] args, @Arg String value, String randomElement, @Arg Option<String> flag) {
        return (">> " + name + " " + perm + " " + value + " " + flag + "\n" + Arrays.toString(args));
    }
}
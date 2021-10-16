# Okaeri Commands (WIP)

![License](https://img.shields.io/github/license/OkaeriPoland/okaeri-commands)
![Total lines](https://img.shields.io/tokei/lines/github/OkaeriPoland/okaeri-commands)
![Repo size](https://img.shields.io/github/repo-size/OkaeriPoland/okaeri-commands)
![Contributors](https://img.shields.io/github/contributors/OkaeriPoland/okaeri-commands)
[![Discord](https://img.shields.io/discord/589089838200913930)](https://discord.gg/hASN5eX)

Simple command framework with yet powerful features and ability to adapt. Part of the [okaeri-platform](https://github.com/OkaeriPoland/okaeri-platform).

## Example Usage

```java
Commands commands = new OkaeriCommands();
commands.registerCommand(ExampleCommand.class); // built for DI (accepts empty constructor by default)
// commands.registerCommand(new ExampleCommand()); // pass own instance (e.g. with custom constructor parameters)

// call manually
commands.call("cmd hello");

// call and get result
Object result = commands.call("cmd woah");

// TODO
```

## Performance

```console
# single thread performance on ryzen 3600, about 1,000,000 invocations per second
Benchmark                           Mode  Cnt        Score       Error  Units
BenchmarkCommands.command_complex  thrpt    5  1109842.766 ± 47168.401  ops/s
BenchmarkCommands.command_medium   thrpt    5  1112048.204 ± 46700.652  ops/s
BenchmarkCommands.command_simple   thrpt    5  1253307.288 ± 31533.820  ops/s
```

## Example CommandService

```java
@Command(label = "cmd", description = "Example command service")
public class ExampleCommand implements CommandService {

    // cmd
    //
    // empty pattern represents command without
    // arguments (similar to @Default in other frameworks)
    //
    // no pattern + method name starting with underscore 
    // is the same as using @Executor(pattern = "")
    //
    @Executor
    public String _def() {
        return "called default";
    }

    // cmd woah
    //
    // simple commands with no additional effort
    // using method name is same as pattern = "woah"
    //
    // it also works for simple arguments:
    // @Executor
    // String woah(@Arg String name) -> cmd woah <name>
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
    // overwrite usage/description with Commands#resolveText()
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
    @Completion(arg = "name", value = "@allplayers")
    @Executor(pattern = "hello *", description = "Prints hello message with name")
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
    // mix param types and resolve unknown values by overriding Commands#resolveMissingArgument (e.g. DI)
    // preserve param names using javac -g:vars or specify them manually @Arg("name")
    //
    // specify inline string completions or use previously registered named completion from Commands#registerCompletion
    // you can also pass @CompletionData and read it in your custom completion implementation as you like
    //
    @Completion(arg = "name", value = "@allplayers", data = @CompletionData(name = "limit", value = "10"))
    @Completion(arg = "perm", value = {"build", "break", "place"})
    @Completion(arg = "value", value = {"allow", "deny"})
    @Completion(arg = "flag", value = {"-silent"})
    @Executor(pattern = "player * set2 * * ?", description = "Complex command test")
    public String complex2(@Arg String name, int huh, @Arg String perm, @RawArgs String[] args, @Arg String value, String randomElement, @Arg Option<String> flag) {
        return (">> " + name + " " + perm + " " + value + " " + flag + "\n" + Arrays.toString(args));
    }
}
```

## Recommendations
It is highly recommended to use `-parameters` compiler flag for better overall feature support.

### Maven (Java)
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.8.1</version>
      <configuration>
        <compilerArgs>
          <arg>-parameters</arg>
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>
```
### Maven (Kotlin)
```xml
 <build>
  <plugins>
    <plugin>
      <groupId>org.jetbrains.kotlin</groupId>
      <artifactId>kotlin-maven-plugin</artifactId>
      <version>${kotlin.version}</version>
      <!-- ... -->
      <configuration>
        <!-- ... -->
        <args>
          <arg>-java-parameters</arg>
        </args>
      </configuration>
    </plugin>
  </plugins>
</build>
```

### Gradle (Java)
```groovy
compileJava {
    options.compilerArgs << '-parameters' 
}
```
### Gradle (Kotlin)
```groovy
compileKotlin {
    kotlinOptions {
        javaParameters = true
    }
}
```

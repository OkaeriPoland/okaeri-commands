# Okaeri Commands (WIP)

![License](https://img.shields.io/github/license/OkaeriPoland/okaeri-commands)
![Total lines](https://img.shields.io/tokei/lines/github/OkaeriPoland/okaeri-commands)
![Repo size](https://img.shields.io/github/repo-size/OkaeriPoland/okaeri-commands)
![Contributors](https://img.shields.io/github/contributors/OkaeriPoland/okaeri-commands)
[![Discord](https://img.shields.io/discord/589089838200913930)](https://discord.gg/hASN5eX)

Simple command framework with yet powerful features and ability to adapt.

## Example Usage

```java
OkaeriCommands commands=CommandsManager.create(new CommandsCli());
commands.register(ExampleCommand.class); // built for DI (accepts empty constructor by default)
// commands.register(new ExampleCommand()); // pass own instance (e.g. with custom constructor parameters)

// call manually
commands.call("cmd hello");

// call and get result
Object result = commands.call("cmd woah");

// TODO
```

## Example CommandService

```java

@ServiceDescriptor(label = "cmd", description = "Example command service")
public class ExampleCommand implements CommandService {

    @Executor(fallback = true)
    private void defaultExecutor(@RawArgs List<String> args) {
        System.out.println("called default method: " + args);
    }

    // cmd woah
    // simple commands with no additional effort
    @Executor(description = "Prints woah message")
    public void woah() {
        return "woah!";
    }

    // cmd hello
    // cmd hello everyone
    @Executor(pattern = {"hello", "hello everyone"}, description = "Prints hello message")
    public void testHello() {
        System.out.println("HELLO!");
    }

    // cmd hi
    // cmd hii
    // overwrite usage/description with CommandsAdapter#resolveText()
    @Executor(pattern = {"hi", "hii"}, description = "!command-cmd-hi-description", usage = "!command-cmd-hi-usage")
    public void testHi() {
        System.out.println("Hi!");
    }

    // cmd hello <name>: use <param> or * for required arguments
    @Executor(pattern = "hello <name>" /* or with unnamed parameter "hmm *" */, description = "Prints hello message with name")
    public void testHelloParam(@Arg("name") String name) {
        System.out.println("HELLO " + name + "!");
    }

    // cmd hmm [name]
    @Executor(pattern = "hmm [name]" /* or with unnamed parameter "hmm ?" */, description = "Prints hmm message with optional name")
    public void testOptionalParam(@Arg("name") Option<String> name) {
        System.out.println("hmm, " + name.getOr("guest") + "!");
    }

    // cmd player <player> set <perm> <value> [flag]
    // mix and match pattern elements, return values to be processed by adapter
    @Executor(pattern = "player <player> set <perm> <value> [flag]", description = "Complex command test")
    public String testPermExample(@Arg("player") String name, @Arg("perm") String perm, @Arg("value") String value, @Arg("flag") Option<String> flag) {
        return (name + " " + perm + " " + value + " " + flag);
    }

    // advanced usage
    // cmd player * xx * * ?: use unnamed parameters
    // mix param types and resolve unknown values by overriding CommandsAdapter#resolveMissingArgument (e.g. DI)
    // preserve param names using javac -g:vars or specify them manually @Arg("name")
    @Executor(pattern = "player * set2 * * ?", description = "Complex command test")
    public String testPermExample3(@Arg String name, int huh, @Arg String perm, @RawArgs String[] args, @Arg String value, String randomElement, @Arg Option<String> flag) {
        return (">> " + name + " " + perm + " " + value + " " + flag + "\n" + Arrays.toString(args));
    }
}
```

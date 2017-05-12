# REPLACING AND PATCHING JAVA APPLICATION AND CORE CLASSES

## Why would you ever need that?

Say you get a jar file. After using the jar for a while you realise that there is a bug in a class in the jar file. Unfortunately you also find out that the jar is no longer supported and there is no way you will get a fix from the author (who is long gone fishing).

In order to solve this issue, you first need to get the source of the class. If you are lucky enough and the author did not obfuscate the class file you can decompile it with a decompiler (my favourite one is JD-GUI).

After you get the source, you modify it, recompile and tell the java interpreter to load your new class first before the old one. Easy right?

## Walk through

Let me give you an example

Say we have this outdated Spider class

`alex@tractorash:~/work/java$ pwd`

_ /home/alex/work/java_

`alex@tractorash:~/work/java$ cat sai/Spider.java`

```java
package sai;
 
class Spider{
 
    private static final String URL = "http://www.google.com/search?q=best+software+2009";
 
    public String getURL(){
        return URL;
    }

}
```

Now, we have the Main class:

```java
package sai;
 
class Main{
 
    public static void main(String[] args){
        Spider spider = new Spider();
        System.out.println(spider.getURL());
    }
 
}
```

Now, if we compile and run the Main class we will get:

`alex@tractorash:~/work/java$ javac sai/Main.java`

`alex@tractorash:~/work/java$ java sai.Main`

_http://www.google.com/search?q=best+software+2009_

As we expected. Suppose we want to search for http://www.google.com/search?q=best+software+2010. The only way to do that is create another sai.Spider class and return the needed string.

So, here we go

`alex@tractorash:~/h$ pwd`

_/home/alex/h_

`alex@tractorash:~/h$ cat sai/Spider.java`

```java
package sai;
 
class Spider{
 
    private static final String URL = "http://www.google.com/search?q=best+software+2010";
 
    public String getURL(){
        return URL;
    }
}
```

We compile our new class

`alex@tractorash:~/h$ javac sai/Spider.java`

Then, switch to previous path:

`alex@tractorash:~/h$ cd ~/work/java/`

`alex@tractorash:~/work/java$ java -cp /home/alex/h:/home/alex/work/java sai.Main`

_http://www.google.com/search?q=best+software+2010_

So what did we do? We modified the classpath (http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/classpath.html) and told the JVM to load our new class first, since it’s first in our list of directories we specified as classpath.

This way we “tricked” the JVM in loading our new class first so we could get the desired output.

What we’ve done so far is called replacing and patching application classes.

## Classes in rt.jar

You will not be able to modify/replace system classes like java.lang.Integer using this method.

You are probably wondering why would you ever want to replace java.lang.Integer. Let me give you an example. As you might know the Integer class is immutable, in other words once the object is initialised you cannot modify it’s value. So, let’s add a new method called setValue(int newVal) which will modify the current value of the Integer object.

As you can imagine, applying the method used in the Spider class example will not work. The reason is: to load system classes and libraries, the JVM does not use the CLASSPATH variable, but the BOOTCLASSPATH. The boot strap class loader uses the boot class path to load the main/system classes.

What’s interesting is that you can specify/modify the BOOTCLASSPATH as well. In order to to that we have to use the -X argument for the java launcher:

`alex@tractorash:~$ java -X`
`-Xbootclasspath:<directories and zip/jar files separated by :>`
`set search path for bootstrap classes and resources`
`-Xbootclasspath/a:<directories and zip/jar files separated by :>`
`append to end of bootstrap class path`
`-Xbootclasspath/p:<directories and zip/jar files separated by :>`
`prepend in front of bootstrap class path`

We are interested in the -Xbootclasspath/p: option since we want to prepend the directory that contains the patched Integer class.

First, let’s add that new method. Get the source of java.lang.Integer. You can get it either from the src.zip file that comes with the JDK, or download it from online.

Switch to our working directory:

`alex@tractorash:/$ cd /home/alex/work/java/`

Add the method:

```java
public void setValue(int value){
    this.value = value;
}
```

If you are modifying a Java 6 version of Integer class you have to remove the final keyword from the value variable.

Was this:

`private final int value;`

Has to be this:

`private int value;`

Now we need a test class where we will test the new functionality. Let’s create the IntegerTest class:

```java
public class IntegerTest{
    public static void main(String[] args){
        Integer tInt = new Integer(1);
        System.out.println("Initial value: " + tInt);
        tInt.setValue(25);
        System.out.println("New value: " + tInt);
    }
}
```

Compiling patched versions of system classes can be a little tricky.

First let’s compile our Integer class:

`alex@tractorash:~/work/java$ javac java/lang/Integer.java`
_java/lang/Integer.java:571: warning: sun.misc.VM is Sun proprietary API and may be removed in a future release_

_if (!sun.misc.VM.isBooted()) {_

_^_

_Note: java/lang/Integer.java uses unchecked or unsafe operations._

_Note: Recompile with -Xlint:unchecked for details._

_1 warning_

We get the warning because we use a sun.* class in our new class. Since sun.* packages and classes are proprietary to Sun, it is strongly advised not to use them, but in our case we just reuse them (so we can ignore the warning).

Now, we have our Integer.class file. We have to use it when we execute our test class.

Now compile IntegerTest.java:

`alex@tractorash:~/work/java$ javac -Xbootclasspath/p:/home/alex/work/java/lang/:/usr/lib/jvm/java-6-sun-1.6.0.15/jre/lib/rt.jar IntegerTest.java`

_./java/lang/Integer.java:571: warning: sun.misc.VM is Sun proprietary API and may be removed in a future release_

_if (!sun.misc.VM.isBooted()) {_

_^_

_Note: ./java/lang/Integer.java uses unchecked or unsafe operations._

_Note: Recompile with -Xlint:unchecked for details._

_1 warning_

Copy Integer.class to the directory where your IntegerTest class is:

`alex@tractorash:~/work/java$ cp ./java/lang/Integer.class .`

So IntegerTest.class and Integer.class are in the same directory.

Finally run IntegerTest:

`alex@tractorash:~/work/java$ java -Xbootclasspath/p:/home/alex/work/java:/usr/lib/jvm/java-6-sun-1.6.0.15/jre/lib/rt.jar IntegerTest`

_Initial value: 1_

_New value: 25_

Success! Now you have a mutable Integer class.

I recommend you read the Java Covert book.

However, I have to warn you that this post is for academic purposes only. Patching/removing/replacing Java core classes can be against the Java license agreement. Please consult the license before going down this path.

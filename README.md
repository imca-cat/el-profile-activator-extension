[//]: # (Modifications copyright 2018 J. Lewis Muir)

# Profile Activation Advanced

Profile Activation Advanced is a [Maven][1] extension enabling a profile
to be activated with an [MVEL][2] expression.

This extension works by overriding Maven's profile property activation
mechanism.  If an MVEL expression is specified and does not evaluate to
true, control is passed to the normal property activation mechanism.

This is a fork of [EL Profile Activator Extension][3].  Profile
Activation Advanced contains the following improvements over EL Profile
Activator Extension:

* Supports specifying the identifier of the properties map for the MVEL
  expression.  This enables support for property names containing invalid
  MVEL identifier characters (e.g., a dot as in `foo.env`).

* Published to the Central Repository.

## Prerequisites

* [Maven][4] 3.3.1 or later
* [MVEL][6] 2.4.2.Final

## Install

Sadly, Profile Activation Advanced cannot be loaded via Maven's
`.mvn/extensions.xml` core extension mechanism because it would be
loaded too late thus preventing it from overriding Maven's built-in
profile property activation mechanism.

To install Profile Activation Advanced, install the following JARs to an
appropriate location and arrange for them to be found on Maven's class
search path:

* [profile-activation-advanced-0.1.0.jar][7]
* [mvel2-2.4.2.Final.jar][8]

There are various ways to do this:

* Place the JARs in Maven's `lib/ext` directory.

* Place the JARs under your project's `lib-ext` directory (or wherever
  you like) and add to the `MAVEN_OPTS` environment variable the
  option `-Dmaven.ext.class.path=<JAR-paths>`, where `<JAR-paths>` is
  a colon-separated list of absolute paths to the JARs placed in your
  project's `lib-ext` directory.  The difficulty here is that the paths
  need to be absolute so that they will work even when the current working
  directory is not the project's root directory.

* Do the same as the preceding approach, but instead of adding to
  the `MAVEN_OPTS` environment variable, add to your project's
  `.mvn/maven.config`.  This has the same problem of needing absolute
  paths.

* Use [Maven Wrapper][9] for your project and patch it to download the
  needed JARs, install them to your project's `lib-ext` directory, and
  add the `-Dmaven.ext.class.path=<JAR-paths>` option from the preceding
  approach to the final Maven execution.  (Even better would be to
  submit a PR to the Maven Wrapper project to add support for loading an
  optional Maven Wrapper extension script from the project's root (e.g.,
  `mvnw.local` and `mvnw.local.cmd`) so that the extra behavior could
  go in the local extension scripts, and you wouldn't need to patch the
  Maven Wrapper scripts.)  Since the wrapper knows the absolute path to
  the project's root, the absolute paths to the JARs can be determined
  correctly every time it is run.

## Use

In a profile activation `property` element (e.g., at the XPath
`/project/profiles/profile/activation/property` in a POM), set
the `name` element to `paa:mvel` and the `value` element to an [MVEL
expression][5].  System and user properties are accessible as
identifiers in the MVEL expression.  If the MVEL expression needs
to access a property that has a character in its name that is not a
valid MVEL identifier (e.g., a dot as in `foo.env`), a properties map
identifier can be specified in parentheses after `paa:mvel` in the `name`
element.  For example, to specify the identifier `p` for the properties
map, set the `name` element to `paa:mvel(p)`; the identifier `p` can then be
used in the MVEL expression to access any of the available properties
(e.g., `p["foo.env"]`).

## Examples

Activate the POM's `foo_env-development` profile if the `foo_env`
property is either not set or is set to `development`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
        http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.example.foo</groupId>
  <artifactId>foo</artifactId>
  <version>1.0.0</version>
  <packaging>pom</packaging>
  <profiles>
    <profile>
      <id>foo_env-development</id>
      <activation>
        <property>
          <name>paa:mvel</name>
          <value>(!isdef foo_env) || foo_env == "development"</value>
        </property>
      </activation>
    </profile>
  </profiles>
</project>
```

The following examples are given as MVEL expressions, not as MVEL
expressions encoded as XML content.  All of these can be included
without modification in XML if they appear inside a CDATA section
(e.g., `<![CDATA[isdef foo && foo == "abc"]]>`).  If they do not
appear inside a CDATA section, but rather appear as regular markup
(i.e., element content), then reserved XML characters must be replaced
with their appropriate XML entities (e.g., `&` must be replaced with
`&amp;`).

`foo` and `bar` are defined and have the same value:

```
isdef foo && isdef bar && foo == bar
```
		
`foo` is defined while `bar` is not:

```
isdef foo && !isdef bar
```

`foo` starts with `abc`, or `baz` contains `xyz`:

```
(isdef foo && foo.startsWith("abc")) || (isdef baz && baz.contains("xyz"))
```

With the properties map identifier set to `p`, `foo.env` equals `test`:

```
isdef foo.env && p["foo.env"] == "test"
```

[1]: https://maven.apache.org/
[2]: https://github.com/mvel/mvel
[3]: https://github.com/kpiwko/el-profile-activator-extension
[4]: https://maven.apache.org/docs/history.html
[5]: http://mvel.documentnode.com/
[6]: https://github.com/mvel/mvel
[7]: https://search.maven.org/artifact/org.imca-cat.maven/profile-activation-advanced/0.1.0/jar
[8]: https://search.maven.org/artifact/org.mvel/mvel2/2.4.2.Final/jar
[9]: https://github.com/takari/maven-wrapper

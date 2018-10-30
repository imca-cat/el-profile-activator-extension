[//]: # (Modifications copyright 2018 J. Lewis Muir)

# Profile Activation Advanced

Profile Activation Advanced is a [Maven][1] extension enabling a profile
to be activated with an [MVEL][2] expression.

This extension works by hijacking Maven's profile property activation
mechanism.  If an MVEL expression is specified and does not evaluate to
true, control is passed to the normal property activator.

This is a reluctant fork of [EL Profile Activator Extension][3] because
there has been no response to communication attempts (i.e., GitHub
issues and pull requests) and no artifact has been published to the
Maven Central repository.

Profile Activation Advanced contains the following features not found in
EL Profile Activator Extension:

* Supports specifying the identifier of the properties map for the MVEL
  expression.  This enables support for property names containing invalid
  MVEL identifier characters (e.g., a dot as in `foo.env`).

# Prerequisites

* [Maven 3.3.1][4] or later

# Install

Add the following to your project's `.mvn/extensions.xml` file:

```xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0
        http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
  <extension>
    <groupId>org.imca-cat.maven</groupId>
    <artifactId>profile-activation-advanced</artifactId>
    <version>0.1.0</version>
  </extension>
</extensions>
```

# Use

In a profile activation `property` element (e.g., at the XPath
`/project/profiles/profile/activation/property` in a POM), set
the `name` element to `mvel` and the `value` element to an [MVEL
expression][5].  System and user properties are accessible as
identifiers in the MVEL expression.  If the MVEL expression needs
to access a property that has a character in its name that is not a
valid MVEL identifier (e.g., a dot as in `foo.env`), a properties map
identifier can be specified in parentheses after `mvel` in the `name`
element.  For example, to specify the identifier `p` for the properties
map, set the `name` element to `mvel(p)`; the identifier `p` can then be
used in the MVEL expression to access any of the available properties
(e.g., `p["foo.env"]`).

# Examples

These examples are given as MVEL expressions, not as MVEL expressions
encoded as XML content.  All of these examples can be included
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
[4]: https://maven.apache.org/docs/3.3.1/release-notes.html
[5]: http://mvel.documentnode.com/

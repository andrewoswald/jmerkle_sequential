jmerkle_sequential
====================

The jmerkle_sequential library offers an extremely simple api to build, compare, and inspect Merkle tree structures.  In addition to these operations,
jmerkle_sequential offers capability to marshal and unmarshal binary representations that can be consumed (or produced) in a technology agnostic
manner (assuming consumers and producers understand the established binary protocol).  <i>Sequential</i> is emphasized in the library's title because 
there are obvious opportunities for utilizing concurrency when altering as well as navigating Merkle tree structures (or any tree structure, for that 
matter).  Those opportunities shall be explored at some point in the future and perhaps implemented in languages other than Java.

While the virtues of the Merkle tree data structure itself are beyond the scope of this readme, the reader is encouraged to partake in research of their own.

For exploratory purposes, here is a very brief list of Merkle tree data structure usage (please contact me if I'm egregiously missing something):
<ul>
<li/> <a href="http://www.allthingsdistributed.com/2007/10/amazons_dynamo.html" target="_blank">Amazon's Dynamo</a>
<li/> <a href="https://github.com/basho/luwak" target="_blank">Basho's Luwak</a>
<li/> <a href="http://wiki.apache.org/cassandra/AntiEntropy" target="_blank">Apache Cassandra</a>
</ul>

The (English) wikipedia entry:
<ul><li/> <a href="http://en.wikipedia.org/wiki/Hash_tree">Hash_tree</a></ul>

Other interesting uses are limited only to one's imagination.

Getting Started
---------------

The jmerkle_sequential library currently has no dependencies, but makes use of apache maven to produce its jar.

To build from source, assuming maven has been installed, from the directory where jmerkle_sequential's pom.xml is located, simply invoke

    mvn package
    

Usage
-----
The build, compare, and inspect operations are each made available through public static methods on the JMerkle class:

* Building and altering a merkle tree is done via 

```java
JMerkle.alter(JMerkle t1, List<JMerkleAlterable> alterations);
```  
In the case of creating a new structure, the <b>t1</b> parameter must be null.  The alter method produces either an abstract JMerkle object or null (in the case that there are no leaves).

* Inspecting a structure's contents is done via 

```java
JMerkle.allkeys(JMerkle t1);
```
The allkeys method produces a List&lt;String&gt; of t1's keys.

* Comparing two merkle tree structures is done via

```java
JMerkle.diff(JMerkle t1, JMerkle t2);
```
The diff function produces a List&lt;String&gt; of keys representing the values (leaves) that are different between the two trees.

In addition to the above operations, the jmerkle_sequential library offers the capability to marshal and unmarshal JMerkle structures to and
from the Java space.  Marshaling results in a byte[] that can be utilized by other languages so long as their implementation understands
the protocol (for example, <a href="https://github.com/andrewoswald/merkle_parser" target="_blank">merkle_parser</a> provides an Erlang example that performs
allkeys and diff operations on the raw binary).

The marshal and unmarshal operations are each made available through public static methods on the JMerkleMarshaler class:

* Marshaling a JMerkle is done via
 
```java
JMerkleMarshaler.marshal(JMerkle t1);
```
The marshal method produces a byte[].

* Unmarshaling a marshaled value (originally from JMerkleMarshaler or elsewhere, assuming proper implementation) is done via 

```java
JMerkleMarshaler.unmarshal(byte[] treeBytes);
```
The unmarshal method produces an abstract JMerkle object.

Examples
--------

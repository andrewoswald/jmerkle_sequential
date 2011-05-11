jmerkle_sequential
====================

The jmerkle_sequential library offers an extremely simple api to build, compare, and inspect merkle tree structures.  In addition to these operations,
jmerkle_sequential offers capability to marshal and unmarshal binary representations that can be consumed (or produced) in a technology agnostic
manner (assuming consumers and producers understand the established binary protocol). 

Getting Started
---------------

The jmerkle_sequential library currently has no dependencies, but makes use of apache maven to produce its jar.

To build from source, assuming maven has been installed, simply invoke

    mvn package
    

Usage
-----
The build, compare, and inspect operations are each made available through public static methods on the JMerkle class:

1. Building a merkle tree is done via JMerkle.alter(JMerkle t1, List&lt;JMerkleAlterable&gt; alterations);  The alter method produces either an abstract JMerkle object or null (in the case that there are no leaves).

2. Inspecting a structure's contents is done via JMerkle.allkeys(JMerkle t1);  The allkeys method produces a List&gt;String&lt; of t1's keys.

3. Comparing two merkle tree structures is done via JMerkle.diff(JMerkle t1, JMerkle t2);  The diff function produces a List&gt;String&lt; of keys that differ between the two trees.

In addition to the above operations, the jmerkle_sequential library offers the capability to marshal and unmarshal JMerkle structures to and
from the Java space.  Marshaling results in a byte[] that can be utilized by other languages so long as their implementation understands
the protocol (https://github.com/andrewoswald/merkle_parser provides an Erlang example).

The marshal and unmarshal operations are each made available through public static methods on the JMerkleMarshaler class:

1. Marshaling a JMerkle is done via JMerkleMarshaler.marshal(JMerkle t1);  The marshal method produces a byte[].

2. Unmarshaling a marshaled value (originally from JMerkleMarshaler or elsewhere, assuming proper implementation) is done via JMerkleMarshaler.unmarshal(byte[] treeBytes);  The unmarshal method produces an abstract JMerkle object.

Examples
--------

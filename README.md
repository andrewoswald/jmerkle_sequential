jmerkle_sequential
====================

The jmerkle_sequential library offers an extremely simple api to build, compare, and inspect merkle tree structures.

Getting Started
---------------

The jmerkle_sequential library currently has no dependencies, but makes use of apache maven to produce its jar.

To build from source, assuming maven has been installed, simply invoke

    mvn package
    

Usage
-----
The build, compare, and inspect operations are each made available through public static methods on the JMerkle class:

1. Building a merkle tree is done via JMerkle.alter(JMerkle t1, List&lt;JMerkleAlterable&gt; alterations);

2. Inspecting a structure's contents is done via JMerkle.allkeys(JMerkle t1);

3. Comparing two merkle tree structures is done via JMerkle.diff(JMerkle t1, JMerkle t2);

In addition to the above operations, the jmerkle_sequential library offers the capability to marshal and unmarshal JMerkle structures to and
from the Java space.  Marshaling results in a byte[] that can be utilized by other languages so long as their implementation understands
the protocol (https://github.com/andrewoswald/merkle_parser provides an Erlang example).

The marshal and unmarshal operations are each made available through public static methods on the JMerkleMarshaler class:

1. Marshaling a JMerkle is done via JMerkleMarshaler.marshal(JMerkle t1);

2. Unmarshaling a marshaled value (originally from JMerkleMarshaler or elsewhere, assuming proper implementation) is done via JMerkleMarshaler.unmarshal(byte[] treeBytes);

Examples
--------

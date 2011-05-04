/*
 * Copyright 2011, Andrew Oswald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jmerkle.sequential;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class JMerkleMarshaler {

    public static JMerkle unmarshal(byte[] treeBytes) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(treeBytes);
        DataInputStream dis = new DataInputStream(bais);

        boolean isBranch = dis.readBoolean();
        // get the uppermost node's hashVal:
        byte[] hashVal = new byte[20];
        dis.read(hashVal);

        JMerkle jMerkle = null;

        if (isBranch) {
            int offset = dis.readInt();
            short numberOfChildren = dis.readShort();
            jMerkle = new Branch();
            jMerkle.hashVal = hashVal;
            ((Branch) jMerkle).offset = offset;
            unmarshalChildren(dis, (Branch) jMerkle, numberOfChildren);
        } else {
            jMerkle = unmarshalLeaf(dis, hashVal);
        }
        return jMerkle;
    }

    private static JMerkle unmarshalChildren(DataInput in, Branch parent, int childCount) throws Exception {
        for (short i = 0; i < childCount; i++) {
            // next byte is the key the following unmarshaled
            // child should reside under in the parent's HashMap:
            byte key = in.readByte();
            // next boolean is the type
            boolean childIsBranch = in.readBoolean();
            // next 20 Bytes are the child's hashVal:
            byte[] hashVal = new byte[20];
            in.readFully(hashVal);

            if (childIsBranch) {
                // next int is the inclusive byte size of the branch
                int offset = in.readInt();
                // next short is the total number of children
                // (not to exceed 256):
                short numberOfChildren = in.readShort();
                Branch childBranch = new Branch();
                childBranch.offset = offset;
                childBranch.hashVal = hashVal;
                parent.children.put(key, unmarshalChildren(in, childBranch, numberOfChildren));
            } else {
                parent.children.put(key, unmarshalLeaf(in, hashVal));
            }
        }

        return parent;
    }

    private static JMerkle unmarshalLeaf(DataInput in, byte[] hashVal) throws Exception {
        int userKeySize = in.readInt();
        byte[] userKeyBytes = new byte[userKeySize];
        in.readFully(userKeyBytes);
        return new Leaf(userKeyBytes, hashVal);
    }

    public static byte[] marshal(JMerkle jMerkle) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        if (jMerkle != null) {
            // first bit toggles the type:
            boolean isBranch = jMerkle.isBranch();
            dos.writeBoolean(isBranch);

            // next byte[20] is its hashVal
            dos.write(jMerkle.hashVal);

            // marshal respective of the type:
            if (isBranch) {
                marshalChildren(dos, (Branch) jMerkle);
            } else {
                marshalLeaf(dos, (Leaf) jMerkle);
            }
            dos.flush();
        }

        return baos.toByteArray();
    }

    private static void marshalLeaf(DataOutput out, Leaf leaf) throws Exception {
        byte[] userKeyBytes = leaf.userKey;
        out.writeInt(userKeyBytes.length);
        out.write(userKeyBytes);
    }

    private static void marshalChildren(DataOutput out, Branch branch) throws Exception {
        // next int is the branch's offset
        out.writeInt(branch.offset());

        HashMap<Byte, JMerkle> branchChildren = branch.children;
        // next short is the number of children this branch
        out.writeShort(branchChildren.size());
        Set<Byte> keys = new TreeSet<Byte>(branchChildren.keySet());
        // for each of the children,
        for (Byte key : keys) {
            // next byte is its key:
            out.writeByte(key);
            JMerkle child = branchChildren.get(key);
            boolean childIsBranch = child.isBranch();
            // next boolean is the child's type:
            out.writeBoolean(childIsBranch);
            // next byte[20] is the child's hashVal:
            out.write(child.hashVal);
            // toggle branch/leaf differences:
            if (child.isBranch()) {
                marshalChildren(out, (Branch) child);
            } else {
                marshalLeaf(out, (Leaf) child);
            }
        }
    }
}

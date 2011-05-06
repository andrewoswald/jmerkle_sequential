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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Leaf extends JMerkle {

    private static final long serialVersionUID = 7479498736319778107L;

    public byte[] userKey;

    public Leaf() {}

    /*default*/ Leaf(byte[] userKey, byte[] hashVal) {
        this.hashVal = hashVal;
        this.userKey = userKey;
    }

    @Override
    JMerkle alterInternal(int offset, List<JMerkleAlterable> alterations) {

        JMerkle context = this;

        if (alterations != null) {

            int alterationsSize = alterations.size();

            for (int i = 0; i < alterationsSize; i++) {
                JMerkleAlterable alteration = alterations.get(i);

                Serializable value = alteration.getValue();
                boolean isUpdate = value != null;
                String key = alteration.getKey();
                byte[] alterationHashVal = JMerkle.hash(value);

                if (this.userKey == null) {
                    if (isUpdate) {
                        // new tree:
                        this.hashVal = JMerkle.hash(value);
                        this.userKey = key.getBytes();
                    }
                } else {
                    if (Arrays.equals(this.userKey, key.getBytes())) {
                        // alteration to _this_ leaf:
                        this.hashVal = isUpdate ? alterationHashVal : null;
                    } else {
                        // create a new Branch:
                        Branch branch = new Branch();
                        // put the initial leaf (this one):
                        byte offsetKey = JMerkle.hash(this.userKey)[offset];
                        branch.children.put(offsetKey, this);
                        // insert the remaining alterations
                        // and switch the context to the result:
                        context = branch.alterInternal(offset, alterations.subList(i, alterationsSize));
                        // inserting on the branch took care of everything;
                        // break out of the loop:
                        break;
                    }
                }
            }
        }

        return context == null || context.hashVal == null ? null : context;
    }

    @Override
    boolean isBranch() {
        return false;
    }

    @Override
    List<UserKeyWrapper> allKeysInternal() {
        if (this.userKey == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(new UserKeyWrapper(userKey));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Leaf) {
            Leaf thatLeaf = (Leaf) obj;
            return Arrays.equals(userKey, thatLeaf.userKey) && Arrays.equals(hashVal, thatLeaf.hashVal);
        }
        return false;
    }

    @Override
    int offset() {
        // type (1) + hashVal (20) + userKey offset (4) + userKey.length
        return 25 + userKey.length;
    }
}
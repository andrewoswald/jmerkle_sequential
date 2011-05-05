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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Branch extends JMerkle {

    private static final long serialVersionUID = 240333586693350332L;

    transient int offset;

    HashMap<Byte, JMerkle> children = new HashMap<Byte, JMerkle>();

    /*default*/ Branch() {}

    @Override
    JMerkle alterInternal(int offset, List<Alteration> alterations) {

        if (alterations != null) {

            Map<Byte, List<Alteration>> collisions = new HashMap<Byte, List<Alteration>>();
            
            for(Alteration alteration : alterations) {
                byte[] keyBytes = alteration.key.getBytes();
                byte offsetKey = JMerkle.hash(keyBytes)[offset];

                if (children.containsKey(offsetKey)) {

                    List<Alteration> collisionAlterations = collisions.get(offsetKey);

                    if (collisionAlterations == null) {
                        collisionAlterations = new ArrayList<Alteration>();
                        collisions.put(offsetKey, collisionAlterations);
                    }
                    collisionAlterations.add(alteration);
                
                } else if (alteration.value != null) {
                    // we're in accordance w/ our balance rules...
                    // create and insert the new Leaf:
                    Leaf leaf = new Leaf(keyBytes, JMerkle.hash(alteration.value));
                    children.put(offsetKey, leaf);
                }
            }

            for (Entry<Byte, List<Alteration>> collisionEntry : collisions.entrySet()) {

                Byte collisionKey = collisionEntry.getKey();

                List<Alteration> pendingAlterations = collisionEntry.getValue();

                JMerkle child = children.get(collisionKey);

                if (child.isBranch()) {
                    child.alterInternal(offset + 1, pendingAlterations);
                } else {
                    // the alteration insert result on a leaf can result in 
                    // 1) an update to that leaf,
                    // 2) deletion of that leaf, or 
                    // 3) conversion of that leaf into a branch:
                    JMerkle result = child.alterInternal(offset + 1, pendingAlterations);
                    if (result != null) {
                        // we don't care whether the leaf was just updated or
                        // converted:
                        children.put(collisionKey, result);
                    } else {
                        // if null, the leaf alteration was a 'delete':
                        children.remove(collisionKey);
                    }
                }
            }
        }

        if (children.isEmpty()) {
            return null;
        } else {
            int childBytes = 0;
            for (JMerkle jMerkle : children.values()) {
                childBytes += jMerkle.offset();
            }
            this.offset = 27 + children.size() + childBytes;
            this.hashVal = null;
            this.hashVal = JMerkle.hash(this);
            return this;
        }
    }

    @Override
    List<UserKeyWrapper> allKeysInternal() {
        List<UserKeyWrapper> allkeys = new ArrayList<UserKeyWrapper>();

        Collection<JMerkle> childValues = children.values();
        for (JMerkle jMerkle : childValues) {
            allkeys.addAll(jMerkle.allKeysInternal());
        }

        return allkeys;
    }

    @Override
    boolean isBranch() {
        return true;
    }

    /**
     * Returns whether the provided <code>Leaf</code> is a child of this
     * <code>Branch</code>. Recursively exhausts child <code>Branch</code>es,
     * but short-circuits once a match has been found.
     * 
     * @param leaf a non-null <code>Leaf</code>.
     * @return If a <code>Leaf</code> with an identical key is found while
     *         traversing the <code>Branch</code>, the <code>Leaf</code>
     *         parameter's hash value is compared to the <code>Branch</code>'s
     *         <code>Leaf</code>'s hash value. <code>true</code> is returned if
     *         the respective hash values are identical; <code>false</code> 
     *         otherwise. If a <code>Leaf</code> of identical key is not found 
     *         and all of the <code>Branch</code>'s children have been exhausted, 
     *         returns <code>null</code>.
     */
    /*default*/ Boolean contains(Leaf leaf) {
        // cycle through all the children:
        Collection<JMerkle> childValues = children.values();
        for (JMerkle jMerkle : childValues) {
            if (jMerkle.isBranch()) {
                // if branch, rinse and repeat:
                Boolean contains = ((Branch) jMerkle).contains(leaf);
                if (contains != null)
                    return contains;
            } else {
                Leaf childLeaf = (Leaf) jMerkle;
                // if leaf, check its userKey:
                if (Arrays.equals(childLeaf.userKey, leaf.userKey)) {
                    // if userKeys are equal, enable
                    // upstream short-circuit on hashVal:
                    return Arrays.equals(childLeaf.hashVal, leaf.hashVal);
                }
            }
        }
        return null;
    }

    @Override
    int offset() {
        return offset;
    }
}
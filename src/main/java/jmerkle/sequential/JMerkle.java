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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class JMerkle implements Serializable {

    private static final long serialVersionUID = 7487888709693360107L;

    /*default*/ byte[] hashVal;

    /*
     * Used simply for its equals method; specifically,
     * utilizing <code>Arrays.equals(byte[] a, byte[] a2)</code>.
     */
    /*default*/ static class UserKeyWrapper {
        public byte[] bytes;

        UserKeyWrapper(byte[] bytes) {
            this.bytes = bytes;
        }
        
        @Override
        public int hashCode() {
            if(bytes == null) {
                return 0;
            } else {
                return Arrays.hashCode(bytes);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            }
            if(obj instanceof UserKeyWrapper) {
                return Arrays.equals(bytes, ((UserKeyWrapper) obj).bytes);
            }
            return false;
        }
    }
    
    /**
     * Provides the unique leaves between the two JMerkle parameters.
     * Either or both of the values may actually be null.
     */
    public static List<String> diff(JMerkle t1, JMerkle t2) {
        if(t1 != null) {
            if(t2 != null) {
                return t1.diff(t2);
            } else {
                return t1.allKeys();
            }
        } else {
            if(t2 != null) {
                return t2.allKeys();
            } else {
                return Collections.emptyList();
            }
        }
    }
    
    /**
     * Provides all the leaves on the provided JMerkle.
     * The parameter may be null, in which case, the
     * empty list is returned.
     */
    public static List<String> allkeys(JMerkle t1) {
        if(t1 != null) {
            return t1.allKeys();
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Alters the leaf values of the provided JMerkle using the provided
     * list of JMerkleAlterable values.  If the t1 parameter is null,
     * creates a new JMerkle and applies the alterations against it.
     */
    public static JMerkle alter(JMerkle t1, List<JMerkleAlterable> alterations) {
        if(t1 == null) {
            t1 = new Leaf();
        }
        return t1.alterInternal(0, alterations);
    }

    private List<String> diff(JMerkle jMerkle) {
        List<UserKeyWrapper> internalDiff = diffInternal(jMerkle);
        List<String> diff = unwrapKeys(internalDiff);
        return diff;
    }

    private List<String> allKeys() {
        List<UserKeyWrapper> allKeysInternal = allKeysInternal();
        List<String> allKeys = unwrapKeys(allKeysInternal);
        return allKeys;
    }

    /*default*/ abstract boolean isBranch();

    /*default*/ abstract JMerkle alterInternal(int offset, List<JMerkleAlterable> alterations);

    /*default*/ abstract List<UserKeyWrapper> allKeysInternal();

    /*default*/ abstract int offset();

    /**
     * This method returns a byte[] of size 20. <br/>
     * <b>Sha-1 is used to create the byte[]; its usage is <i>not</i> for
     * cryptographic purposes.</b>
     * 
     * @param obj
     * @return
     */
    /*default*/ static final byte[] hash(Serializable obj) {
        byte[] hash = null;
        if(obj != null) {
            byte[] bytes = JMerkle.getBytes(obj);
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            hash = digest.digest(bytes);
        }
        return hash;
    }

    private static byte[] getBytes(Serializable obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            oos.close();
            bos.close();
        } catch (IOException e) {
            // nothing recoverable; throw a runtime exception:
            throw new RuntimeException(e);
        }
        byte[] data = bos.toByteArray();
        return data;
    }

    private List<String> unwrapKeys(List<UserKeyWrapper> wrappedKeys) {
        int wrappedKeysSize = wrappedKeys.size();
        List<String> unwrappedKeys = new ArrayList<String>(wrappedKeysSize);
        for(UserKeyWrapper ukw : wrappedKeys) {
            unwrappedKeys.add(new String(ukw.bytes));
        }
        return unwrappedKeys;
    }

    private List<UserKeyWrapper> diffInternal(JMerkle that) {
        boolean thatIsBranch = that.isBranch();
        if (this.isBranch()) {
            if (thatIsBranch) {
                return diff((Branch) this, (Branch) that);
            } else {
                return diff((Leaf) that, (Branch) this);
            }
        } else {
            if (thatIsBranch) {
                return diff((Leaf) this, (Branch) that);
            } else {
                return diff((Leaf) this, (Leaf) that);
            }
        }
    }

    private List<UserKeyWrapper> diff(Leaf thisLeaf, Leaf thatLeaf) {

        byte[] thisLeafUserKey = thisLeaf.userKey;
        byte[] thatLeafUserKey = thatLeaf.userKey;

        if (thisLeafUserKey == null) {
            if (thatLeafUserKey != null) {
                return Collections.singletonList(new UserKeyWrapper(thatLeafUserKey));
            } else {
                return Collections.emptyList();
            }
        } else {
            if (thatLeafUserKey == null) {
                return Collections.singletonList(new UserKeyWrapper(thisLeafUserKey));
            } else {

                List<UserKeyWrapper> keys = null;

                if (Arrays.equals(thisLeafUserKey, thatLeafUserKey)) {
                    if (Arrays.equals(thisLeaf.hashVal, thatLeaf.hashVal)) {
                        keys = Collections.emptyList();
                    } else {
                        keys = Collections.singletonList(new UserKeyWrapper(thisLeafUserKey));
                    }
                } else {
                    // if they're different, return both:
                    keys = new ArrayList<UserKeyWrapper>(2);
                    keys.add(new UserKeyWrapper(thatLeafUserKey));
                    keys.add(new UserKeyWrapper(thatLeafUserKey));
                }

                return keys;
            }
        }
    }

    private List<UserKeyWrapper> diff(Leaf leaf, Branch branch) {

        List<UserKeyWrapper> diffKeys = branch.allKeysInternal();

        Boolean contains = branch.contains(leaf);

        UserKeyWrapper leafUserKeyWrapper = new UserKeyWrapper(leaf.userKey);

        if (contains != null && contains) {
            diffKeys.remove(leafUserKeyWrapper);
        } else {
            // it might already be there, but we need to be sure:
            // (to clarify, it might already be there by the virtue
            // that the branch's leaf and the provided leaf's hash
            // vals are indeed different.)
            diffKeys.add(leafUserKeyWrapper);
        }

        return diffKeys;
    }

    private List<UserKeyWrapper> diff(Branch b1, Branch b2) {
        if (Arrays.equals(b1.hashVal, b2.hashVal)) {
            return Collections.emptyList();
        } else {

            List<UserKeyWrapper> diffKeys = new ArrayList<UserKeyWrapper>();

            Map<Byte, JMerkle> b1Children = b1.children;
            Map<Byte, JMerkle> b2Children = b2.children;

            // recursive diff on keys in common:
            Set<Byte> commonKeys = new HashSet<Byte>(b1Children.keySet());
            commonKeys.retainAll(new HashSet<Byte>(b2Children.keySet()));

            for (Byte commonKey : commonKeys) {
                JMerkle b1Child = b1Children.get(commonKey);
                JMerkle b2Child = b2Children.get(commonKey);
                diffKeys.addAll(b1Child.diffInternal(b2Child));
            }

            // all user keys on keys unique to b1:
            Set<Byte> b1UniqueKeys = new HashSet<Byte>(b1Children.keySet());
            b1UniqueKeys.removeAll(new HashSet<Byte>(b2Children.keySet()));

            for (Byte b1UniqueKey : b1UniqueKeys) {
                JMerkle b1Child = b1Children.get(b1UniqueKey);
                diffKeys.addAll(b1Child.allKeysInternal());
            }

            // all user keys on keys unique to b2:
            Set<Byte> b2UniqueKeys = new HashSet<Byte>(b2Children.keySet());
            b2UniqueKeys.removeAll(new HashSet<Byte>(b1Children.keySet()));

            for (Byte b2UniqueKey : b2UniqueKeys) {
                JMerkle b2Child = b2Children.get(b2UniqueKey);
                diffKeys.addAll(b2Child.allKeysInternal());
            }

            return diffKeys;
        }
    }
}
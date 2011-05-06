package jmerkle.sequential;

import java.io.Serializable;

/**
 * Encapsulates an identifiable leaf value that is used to alter the contents of
 * a <code>JMerkle</code> structure. If the intent of the alteration is to
 * <i>remove</i> a leaf from the tree, the <code>Alteration</code>'s value
 * should be null (the leaf identified by the key will be removed). <br/>
 * Alter operations are generally idempotent. For example, deleting a value that
 * does not exist has no effect and similarly, adding or updating an existing
 * value to its current value also has no outward effect.
 * 
 * @author andrew oswald
 * 
 */
public interface JMerkleAlterable {

    /**
     * @return String representing the identity of a <code>Leaf</code> within a
     *         JMerkle structure.
     */
    abstract String getKey();

    /**
     * @return Serializable <code>Leaf</code> value that the JMerkle structure
     *         uses when comparing trees. Its contents get hashed into 20 bytes.
     */
    abstract Serializable getValue();
}

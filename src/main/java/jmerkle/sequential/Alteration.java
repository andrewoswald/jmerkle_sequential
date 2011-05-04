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

/**
 * Encapsulates an identifiable leaf value that is used to alter the
 * contents of a <code>JMerkle</code> structure.  If the intent
 * of the alteration is to <i>remove</i> a leaf from the tree, the 
 * <code>Alteration</code>'s value should be null (the leaf identified
 * by the key will be removed).  Alter operations are generally idempotent,
 * therefore deleting a value that does not exist has no effect and 
 * similarly, adding or updating an existing value to its current value 
 * also has no outward effect.
 * 
 * @author andrew oswald
 *
 */
public class Alteration {
	
	/*default*/ String key;
	/*default*/ Serializable value;
	
	public Alteration(String key, Serializable value) {
		this.key = key;
		this.value = value;
	}
}
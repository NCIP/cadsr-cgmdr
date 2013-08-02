/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.storage.analysis;


public interface Tokenizer {
	
	public void setText(CharSequence text);
    public void setText(CharSequence text, int offset);
    public TextToken nextToken();
	public TextToken nextToken(boolean allowWildcards);
	public void setStemming(boolean stem);
}
